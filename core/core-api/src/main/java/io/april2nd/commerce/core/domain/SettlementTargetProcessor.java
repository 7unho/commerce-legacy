package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CancelType;
import io.april2nd.commerce.core.enums.PaymentState;
import io.april2nd.commerce.core.enums.TransactionType;
import io.april2nd.commerce.storage.db.core.CancelRepository;
import io.april2nd.commerce.storage.db.core.MerchantProductMappingEntity;
import io.april2nd.commerce.storage.db.core.MerchantProductMappingRepository;
import io.april2nd.commerce.storage.db.core.OrderItemEntity;
import io.april2nd.commerce.storage.db.core.OrderItemRepository;
import io.april2nd.commerce.storage.db.core.PaymentRepository;
import io.april2nd.commerce.storage.db.core.SettlementCancelTarget;
import io.april2nd.commerce.storage.db.core.SettlementPaymentTarget;
import io.april2nd.commerce.storage.db.core.SettlementTargetEntity;
import io.april2nd.commerce.storage.db.core.SettlementTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SettlementTargetProcessor {
    private static final int PAGE_SIZE = 1000;

    private final PaymentRepository paymentRepository;
    private final CancelRepository cancelRepository;
    private final OrderItemRepository orderItemRepository;
    private final MerchantProductMappingRepository merchantProductMappingRepository;
    private final SettlementTargetRepository settlementTargetRepository;

    @Transactional
    public int load(LocalDate settleDate, LocalDateTime from, LocalDateTime to) {
        return loadPaymentTargets(settleDate, from, to) + loadCancelTargets(settleDate, from, to);
    }

    private int loadPaymentTargets(LocalDate settleDate, LocalDateTime from, LocalDateTime to) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"));
        Slice<SettlementPaymentTarget> payments;
        int count = 0;

        do {
            payments = paymentRepository.findSettlementTargetsByStateAndPaidAtBetween(
                    PaymentState.SUCCESS,
                    from,
                    to,
                    pageable
            );

            Map<Long, Long> transactionIdsByOrder = payments.getContent()
                    .stream()
                    .collect(Collectors.toMap(
                            SettlementPaymentTarget::orderId,
                            SettlementPaymentTarget::paymentId
                    ));

            count += saveTargets(
                    settleDate,
                    findPaymentTargetItems(transactionIdsByOrder)
            );

            pageable = payments.nextPageable();
        } while (payments.hasNext());

        return count;
    }

    private int loadCancelTargets(LocalDate settleDate, LocalDateTime from, LocalDateTime to) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"));
        Slice<SettlementCancelTarget> cancels;
        int count = 0;

        do {
            cancels = cancelRepository.findSettlementTargetsByCanceledAtBetween(from, to, pageable);

            count += saveTargets(
                    settleDate,
                    findCancelTargetItems(cancels.getContent())
            );

            pageable = cancels.nextPageable();
        } while (cancels.hasNext());

        return count;
    }

    private List<SettlementTargetItem> findPaymentTargetItems(Map<Long, Long> transactionIdsByOrder) {
        if (transactionIdsByOrder.isEmpty()) {
            return List.of();
        }

        return orderItemRepository.findByOrderIdIn(transactionIdsByOrder.keySet())
                .stream()
                .map(item -> new SettlementTargetItem(
                        TransactionType.PAYMENT,
                        transactionIdsByOrder.get(item.getOrderId()),
                        item.getOrderId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .toList();
    }

    private List<SettlementTargetItem> findCancelTargetItems(List<SettlementCancelTarget> cancels) {
        if (cancels.isEmpty()) {
            return List.of();
        }

        Set<Long> allCancelOrderIds = cancels.stream()
                .filter(cancel -> cancel.cancelType() == CancelType.ALL)
                .map(SettlementCancelTarget::orderId)
                .collect(Collectors.toSet());
        Set<Long> partialCancelOrderItemIds = cancels.stream()
                .filter(cancel -> cancel.cancelType() == CancelType.PARTIAL)
                .map(SettlementCancelTarget::orderItemId)
                .collect(Collectors.toSet());

        Map<Long, OrderItemEntity> partialItemsById = orderItemRepository.findAllById(partialCancelOrderItemIds)
                .stream()
                .collect(Collectors.toMap(OrderItemEntity::getId, Function.identity()));

        List<SettlementTargetItem> allCancelItems = orderItemRepository.findByOrderIdIn(allCancelOrderIds)
                .stream()
                .map(item -> new SettlementTargetItem(
                        TransactionType.CANCEL,
                        findAllCancelId(cancels, item.getOrderId()),
                        item.getOrderId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice().negate()
                ))
                .toList();

        List<SettlementTargetItem> partialCancelItems = cancels.stream()
                .filter(cancel -> cancel.cancelType() == CancelType.PARTIAL)
                .map(cancel -> {
                    OrderItemEntity item = partialItemsById.get(cancel.orderItemId());

                    if (item == null) {
                        throw new IllegalStateException("취소 대상 주문 상품 " + cancel.orderItemId() + " 이 존재하지 않음");
                    }

                    BigDecimal canceledTotalPrice = item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(cancel.canceledQuantity()))
                            .negate();

                    return new SettlementTargetItem(
                            TransactionType.PARTIAL_CANCELED,
                            cancel.cancelId(),
                            item.getOrderId(),
                            item.getProductId(),
                            cancel.canceledQuantity(),
                            item.getUnitPrice(),
                            canceledTotalPrice
                    );
                })
                .toList();

        return combine(allCancelItems, partialCancelItems);
    }

    private int saveTargets(
            LocalDate settleDate,
            List<SettlementTargetItem> items
    ) {
        if (items.isEmpty()) {
            return 0;
        }

        Map<Long, MerchantProductMappingEntity> merchantMappingsByProduct = merchantProductMappingRepository.findByProductIdIn(
                        items.stream()
                                .map(SettlementTargetItem::productId)
                                .collect(Collectors.toSet())
                )
                .stream()
                .collect(Collectors.toMap(
                        MerchantProductMappingEntity::getProductId,
                        Function.identity()
                ));

        List<SettlementTargetEntity> targets = items.stream()
                .map(item -> toEntity(settleDate, merchantMappingsByProduct, item))
                .toList();

        settlementTargetRepository.saveAll(targets);
        return targets.size();
    }

    private SettlementTargetEntity toEntity(
            LocalDate settleDate,
            Map<Long, MerchantProductMappingEntity> merchantMappingsByProduct,
            SettlementTargetItem item
    ) {
        MerchantProductMappingEntity mapping = merchantMappingsByProduct.get(item.productId());
        if (mapping == null) {
            throw new IllegalStateException("상품 " + item.productId() + " 의 가맹점 매핑이 존재하지 않음");
        }

        if (item.transactionId() == null) {
            throw new IllegalStateException("주문 " + item.orderId() + " 의 거래 ID 매핑이 존재하지 않음");
        }

        return new SettlementTargetEntity(
                mapping.getMerchantId(),
                settleDate,
                item.totalPrice(),
                item.transactionType(),
                item.transactionId(),
                item.orderId(),
                item.productId(),
                item.quantity(),
                item.unitPrice(),
                item.totalPrice()
        );
    }

    private List<SettlementTargetItem> combine(List<SettlementTargetItem> first, List<SettlementTargetItem> second) {
        return Stream.concat(first.stream(), second.stream())
                .toList();
    }

    private Long findAllCancelId(List<SettlementCancelTarget> cancels, Long orderId) {
        return cancels.stream()
                .filter(cancel -> cancel.cancelType() == CancelType.ALL)
                .filter(cancel -> orderId.equals(cancel.orderId()))
                .map(SettlementCancelTarget::cancelId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("주문 " + orderId + " 의 취소 거래 ID 매핑이 존재하지 않음"));
    }
}

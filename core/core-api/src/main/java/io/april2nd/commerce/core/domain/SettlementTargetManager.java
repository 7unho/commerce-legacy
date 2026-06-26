package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.CancelType;
import io.april2nd.commerce.core.enums.TransactionType;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SettlementTargetManager {
    private final SettlementTargetRepository settlementTargetRepository;
    private final OrderItemRepository orderItemRepository;
    private final MerchantProductMappingRepository merchantProductMappingRepository;

    @Transactional
    public void processPayments(LocalDate settleDate, List<SettlementPayment> payments) {
        Map<Long, Long> transactionIdMap = payments.stream()
                .collect(Collectors.toMap(
                        SettlementPayment::orderId,
                        SettlementPayment::id
                ));
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderIdIn(transactionIdMap.keySet());
        Set<Long> orderItemIds = orderItems.stream()
                .map(OrderItemEntity::getProductId)
                .collect(Collectors.toSet());

        Map<Long, MerchantProductMappingEntity> merchantMappingMap = merchantProductMappingRepository.findByProductIdIn(orderItemIds)
                .stream()
                .collect(Collectors.toMap(
                        MerchantProductMappingEntity::getProductId,
                        Function.identity()
                ));

        List<SettlementTargetEntity> targets = orderItems.stream()
                .filter(it -> merchantMappingMap.containsKey(it.getProductId()))
                .map(item -> toSettlementTarget(settleDate, merchantMappingMap, transactionIdMap, item))
                .collect(Collectors.toList());

        settlementTargetRepository.saveAll(targets);
    }

    @Transactional
    public void processCancels(LocalDate settleDate, List<SettlementCancel> cancels) {
        List<SettlementCancel> allCancels = cancels.stream()
                .filter(cancel -> cancel.type() == CancelType.ALL)
                .collect(Collectors.toList());

        List<SettlementTargetEntity> cancelTargets = processAllCancels(settleDate, allCancels);

        List<SettlementCancel> partialCancels = cancels.stream()
                .filter(cancel -> cancel.type() == CancelType.PARTIAL)
                .collect(Collectors.toList());

        List<SettlementTargetEntity> partialCancelTargets = processPartialCancels(settleDate, partialCancels);

        List<SettlementTargetEntity> targets = Stream.concat(
                cancelTargets.stream(),
                partialCancelTargets.stream()
        ).collect(Collectors.toList());

        settlementTargetRepository.saveAll(targets);
    }

    private List<SettlementTargetEntity> processAllCancels(
            LocalDate settleDate,
            List<SettlementCancel> cancels
    ) {
        Map<Long, Long> transactionIdMap = cancels.stream()
                .collect(Collectors.toMap(
                        SettlementCancel::orderId,
                        SettlementCancel::id
                ));

        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderIdIn(transactionIdMap.keySet());

        Set<Long> productIds = orderItems.stream()
                .map(OrderItemEntity::getProductId)
                .collect(Collectors.toSet());

        Map<Long, MerchantProductMappingEntity> merchantMappingMap = merchantProductMappingRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        MerchantProductMappingEntity::getProductId,
                        Function.identity()
                ));

        return orderItems.stream()
                .filter(item -> merchantMappingMap.containsKey(item.getProductId()))
                .map(item -> toCancelSettlementTarget(settleDate, merchantMappingMap, transactionIdMap, item))
                .collect(Collectors.toList());
    }

    private List<SettlementTargetEntity> processPartialCancels(
            LocalDate settleDate,
            List<SettlementCancel> cancels
    ) {
        if (cancels.isEmpty()) {
            return List.of();
        }

        Set<Long> orderItemIds = cancels.stream()
                .map(SettlementCancel::orderItemId)
                .collect(Collectors.toSet());

        List<OrderItemEntity> orderItems = orderItemRepository.findAllById(orderItemIds);

        Map<Long, OrderItemEntity> orderItemsById = orderItems.stream()
                .collect(Collectors.toMap(
                        OrderItemEntity::getId,
                        Function.identity()
                ));

        Set<Long> productIds = orderItems.stream()
                .map(OrderItemEntity::getProductId)
                .collect(Collectors.toSet());

        Map<Long, MerchantProductMappingEntity> merchantMappingMap = merchantProductMappingRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(
                        MerchantProductMappingEntity::getProductId,
                        Function.identity()
                ));

        return cancels.stream()
                .filter(cancel -> {
                    OrderItemEntity item = orderItemsById.get(cancel.orderItemId());
                    return item != null && merchantMappingMap.containsKey(item.getProductId());
                })
                .map(cancel -> toPartialCancelSettlementTarget(
                        settleDate,
                        merchantMappingMap,
                        orderItemsById,
                        cancel
                ))
                .collect(Collectors.toList());
    }

    private SettlementTargetEntity toSettlementTarget(
            LocalDate settleDate,
            Map<Long, MerchantProductMappingEntity> merchantMappingMap,
            Map<Long, Long> transactionIdMap,
            OrderItemEntity item) {
        MerchantProductMappingEntity merchantProductMapping = Optional.ofNullable(merchantMappingMap.get(item.getProductId()))
                .orElseThrow(() -> new IllegalStateException(String.format("[SettlementTargetManager.toSettlementTarget] 상품 {%s}의 가맹점 매핑이 존재하지 않음", item.getProductId())));

        Long transactionId = Optional.ofNullable(transactionIdMap.get(item.getOrderId()))
                .orElseThrow(() -> new IllegalStateException(String.format("[SettlementTargetManager.toSettlementTarget] 주문 {%s}의 거래 ID 매핑이 존재하지 않음", item.getOrderId())));

        return new SettlementTargetEntity(
                merchantProductMapping.getMerchantId(),
                settleDate,
                item.getTotalPrice(),
                TransactionType.PAYMENT,
                transactionId,
                item.getOrderId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }

    private SettlementTargetEntity toCancelSettlementTarget(
            LocalDate settleDate,
            Map<Long, MerchantProductMappingEntity> merchantMappingMap,
            Map<Long, Long> transactionIdMap,
            OrderItemEntity item
    ) {
        MerchantProductMappingEntity merchantProductMapping = Optional.ofNullable(merchantMappingMap.get(item.getProductId()))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "[SettlementTargetManager.toCancelSettlementTarget] 상품 {%s}의 가맹점 매핑이 존재하지 않음",
                        item.getProductId()
                )));

        Long transactionId = Optional.ofNullable(transactionIdMap.get(item.getOrderId()))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "[SettlementTargetManager.toCancelSettlementTarget] 주문 {%s}의 거래 ID 매핑이 존재하지 않음",
                        item.getOrderId()
                )));

        return new SettlementTargetEntity(
                merchantProductMapping.getMerchantId(),
                settleDate,
                item.getTotalPrice().negate(),
                TransactionType.CANCEL,
                transactionId,
                item.getOrderId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }

    private SettlementTargetEntity toPartialCancelSettlementTarget(
            LocalDate settleDate,
            Map<Long, MerchantProductMappingEntity> merchantMappingMap,
            Map<Long, OrderItemEntity> orderItemsById,
            SettlementCancel cancel
    ) {
        OrderItemEntity item = Optional.ofNullable(orderItemsById.get(cancel.orderItemId()))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "[SettlementTargetManager.toPartialCancelSettlementTarget] 취소 대상 주문 상품 {%s}이 존재하지 않음",
                        cancel.orderItemId()
                )));

        MerchantProductMappingEntity merchantProductMapping = Optional.ofNullable(merchantMappingMap.get(item.getProductId()))
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "[SettlementTargetManager.toPartialCancelSettlementTarget] 상품 {%s}의 가맹점 매핑이 존재하지 않음",
                        item.getProductId()
                )));

        BigDecimal totalPrice = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(cancel.canceledQuantity()));

        return new SettlementTargetEntity(
                merchantProductMapping.getMerchantId(),
                settleDate,
                totalPrice.negate(),
                TransactionType.PARTIAL_CANCEL,
                cancel.id(),
                item.getOrderId(),
                item.getProductId(),
                cancel.canceledQuantity(),
                item.getUnitPrice(),
                totalPrice
        );
    }
}

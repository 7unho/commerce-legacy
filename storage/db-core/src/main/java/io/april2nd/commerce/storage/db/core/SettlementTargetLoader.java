package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementTargetLoader {
    private SettlementTargetRepository settlementTargetRepository;
    private OrderItemRepository orderItemRepository;
    private MerchantProductMappingRepository merchantProductMappingRepository;

    @Transactional
    public void process(LocalDate settleDate, TransactionType transactionType, Map<Long, Long> transactionIdMap) {
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderIdIn(transactionIdMap.keySet());

        Map<Long, MerchantProductMappingEntity> merchantMappingMap = merchantProductMappingRepository.findByProductIdIn(
                orderItems.stream()
                                .map(OrderItemEntity::getProductId)
                                .collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(
                        MerchantProductMappingEntity::getProductId,
                        Function.identity()
                ));

        List<SettlementTargetEntity> targets = orderItems.stream()
                .map(item -> {
                    MerchantProductMappingEntity mapping = merchantMappingMap.get(item.getProductId());
                    if (mapping == null) {
                        throw new IllegalStateException("상품 " + item.getProductId() + " 의 가맹점 매핑이 존재하지 않음");
                    }

                    Long transactionId = transactionIdMap.get(item.getOrderId());
                    if (transactionId == null) {
                        throw new IllegalStateException("주문 " + item.getOrderId() + " 의 거래 ID 매핑이 존재하지 않음");
                    }

                    BigDecimal targetAmount;
                    switch (transactionType) {
                        case PAYMENT:
                            targetAmount = item.getTotalPrice();
                            break;
                        case CANCEL:
                            targetAmount = item.getTotalPrice().negate();
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }

                    return new SettlementTargetEntity(
                            mapping.getMerchantId(),
                            settleDate,
                            targetAmount,
                            transactionType,
                            transactionId,
                            item.getOrderId(),
                            item.getProductId(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotalPrice()
                    );
                })
                .collect(Collectors.toList());

        settlementTargetRepository.saveAll(targets);
    }
}

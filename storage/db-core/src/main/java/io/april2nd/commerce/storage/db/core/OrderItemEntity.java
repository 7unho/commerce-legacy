package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.OrderState;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity extends BaseEntity {
    private Long orderId;
    private Long productId;
    private Long productOptionId;
    private String productName;
    private String productOptionName;
    private String thumbnailUrl;
    private String shortDescription;
    private String productOptionDescription;
    private Long quantity;
    private Long canceledQuantity;

    @Enumerated(EnumType.STRING)
    private OrderState state;

    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public OrderItemEntity(
            Long orderId,
            Long productId,
            Long productOptionId,
            String productName,
            String productOptionName,
            String thumbnailUrl,
            String shortDescription,
            String productOptionDescription,
            Long quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
        this.orderId = orderId;
        this.productId = productId;
        this.productOptionId = productOptionId;
        this.productName = productName;
        this.productOptionName = productOptionName;
        this.thumbnailUrl = thumbnailUrl;
        this.shortDescription = shortDescription;
        this.productOptionDescription = productOptionDescription;
        this.quantity = quantity;
        this.canceledQuantity = 0L;
        this.state = OrderState.CREATED;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public void paid() {
        this.state = OrderState.PAID;
    }

    public Long cancellableQuantity() {
        return quantity - canceledQuantity;
    }

    public void cancel(Long cancelQuantity) {
        this.canceledQuantity += cancelQuantity;
        this.state = this.canceledQuantity >= this.quantity ? OrderState.CANCELED : OrderState.PARTIAL_CANCELED;
    }

    public boolean isAllCanceled() {
        return Objects.equals(quantity, canceledQuantity);
    }
}

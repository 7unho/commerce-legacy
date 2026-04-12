package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
public class OrderItemEntity extends BaseEntity {
    private Long orderId;
    private Long productId;
    private String productName;
    private String thumbnailUrl;
    private String shortDescription;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@AllArgsConstructor
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
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

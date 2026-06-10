package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "cart_item",
        indexes = @Index(name = "udx_cart_item_product", columnList = "cartId, productId", unique = true)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CartItemEntity extends BaseEntity {
    private Long cartId;

    private Long productId;

    private Long quantity;

    public void applyQuantity(Long value) {
        this.quantity = (value < 1) ? 1 : value;
    }

    public void addQuantity(Long value) {
        this.quantity += value;
        if (this.quantity < 1) this.quantity = 1L;
    }
}

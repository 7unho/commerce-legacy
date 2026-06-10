package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "cart_item")
public class CartItemEntity extends BaseEntity {
    private Long userId;
    private Long cartId;
    private Long productId;
    private Long productOptionId;
    private Long quantity;

    public void applyQuantity(Long value) {
        this.quantity = value < 1 ? 1L : value;
    }
}

package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CartItemEntity extends BaseEntity {
    private Long userId;

    private Long productId;

    private Long quantity;

    public void applyQuantity(Long value) {
        this.quantity = (value < 1) ? 1 : value;
    }
}
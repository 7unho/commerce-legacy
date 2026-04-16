package io.april2nd.commerce.core.api.controller.v1.response;

import io.april2nd.commerce.core.domain.CartItem;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items
) {
    public record CartItemResponse(
            Long id,
            Long productId,
            String productName,
            String thumbnailUrl,
            String description,
            String shortDescription,
            BigDecimal costPrice,
            BigDecimal salesPrice,
            BigDecimal discountPrice,
            Long quantity
    ) {
        public static CartItemResponse of(CartItem cartItem) {
            return new CartItemResponse(
                    cartItem.id(),
                    cartItem.product().id(),
                    cartItem.product().name(),
                    cartItem.product().thumbnailUrl(),
                    cartItem.product().description(),
                    cartItem.product().shortDescription(),
                    cartItem.product().price().costPrice(),
                    cartItem.product().price().salesPrice(),
                    cartItem.product().price().discountedPrice(),
                    cartItem.quantity()
            );
        }
    }
}

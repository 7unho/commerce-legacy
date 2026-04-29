package io.april2nd.commerce.core.api.assembler;

import io.april2nd.commerce.core.api.controller.v1.request.AddCartItemRequest;
import io.april2nd.commerce.core.api.controller.v1.request.ModifyCartItemRequest;
import io.april2nd.commerce.core.domain.Cart;
import io.april2nd.commerce.core.domain.CartService;
import io.april2nd.commerce.core.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartAssembler {
    private final CartService cartService;

    public Cart getCart(User user) {
        return cartService.getCart(user);
    }

    public void addCartItem(User user, AddCartItemRequest request) {
        cartService.addCartItem(user, request.toAddCartItem());
    }

    public void modifyCartItem(User user, Long cartItemId, ModifyCartItemRequest request) {
        cartService.modifyCartItem(user, request.toModifyCartItem(cartItemId));
    }

    public void deleteCartItem(User user, Long cartItemId) {
        cartService.deleteCartItem(user, cartItemId);
    }
}

package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartReader cartReader;
    private final CartManager cartManager;

    public Cart getCart(User user) {
        return cartReader.getCart(user);
    }

    public Long addCartItem(User user, AddCartItem item) {
        return cartManager.add(user, item);
    }

    public Long modifyCartItem(User user, ModifyCartItem item) {
        return cartManager.modify(user, item);
    }

    public void deleteCartItem(User user, Long cartItemId) {
        cartManager.delete(user, cartItemId);
    }
}

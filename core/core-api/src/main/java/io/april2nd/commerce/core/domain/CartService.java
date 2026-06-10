package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        cartManager.deleteItem(user, cartItemId);
    }

    public CreatedSharedCart createSharedCart(User user, CreateSharedCart command) {
        return cartManager.createSharedCart(user, command);
    }

    public List<SharedCartSummary> getSharedCarts(User user) {
        return cartReader.getSharedCarts(user);
    }

    public SharedCart getSharedCart(User user, Long cartId) {
        return cartReader.getSharedCart(user, cartId);
    }

    public Long acceptSharedCart(User user, String accessKey) {
        return cartManager.acceptSharedCart(user, accessKey);
    }

    public void deleteSharedCart(User user, Long cartId) {
        cartManager.deleteSharedCart(user, cartId);
    }
}

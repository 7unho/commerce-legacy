package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartReader cartReader;
    private final CartItemManager cartItemManager;
    private final CartHandler cartHandler;
    private final CartVerifier cartVerifier;

    public Cart getCart(User user) {
        return cartReader.getCart(user.id());
    }

    public List<CartAccess> getAccessibleCart(User user) {
        return cartReader.getCartAccessList(user.id());
    }

    public Cart getSharedCart(User user, Long cartId) {
        return cartReader.getSharedCart(user.id(), cartId);
    }

    public CartAccess createSharedCarts(User user) {
        return cartHandler.createSharedCart(user.id());
    }

    public void deleteCart(User user, Long cartId) {
        cartHandler.remove(user.id(), cartId);
    }

    public void access(User user, String accessKey) {
        cartHandler.access(user.id(), accessKey);
    }

    public Long addCartItem(User user, AddCartItem item) {
        CartOwner owner = cartVerifier.verifyAccess(user.id(), item.cartId());
        return cartItemManager.addItem(owner, item);
    }

    public Long modifyCartItem(User user, ModifyCartItem item) {
        return cartItemManager.modifyItem(user.id(), item);
    }

    public void deleteCartItem(User user, Long cartItemId) {
        cartItemManager.deleteItem(user.id(), cartItemId);
    }
}

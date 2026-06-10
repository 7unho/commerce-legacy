package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.CartType;
import io.april2nd.commerce.core.enums.SharedCartRole;
import io.april2nd.commerce.core.enums.SharedCartState;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CartEntity;
import io.april2nd.commerce.storage.db.core.CartItemEntity;
import io.april2nd.commerce.storage.db.core.CartItemRepository;
import io.april2nd.commerce.storage.db.core.CartMemberEntity;
import io.april2nd.commerce.storage.db.core.CartMemberRepository;
import io.april2nd.commerce.storage.db.core.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartReader {
    private final CartRepository cartRepository;
    private final CartMemberRepository cartMemberRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductFinder productFinder;

    public Cart getCart(User user) {
        CartEntity cart = cartRepository.findByOwnerIdAndTypeAndStatus(user.id(), CartType.PERSONAL, EntityStatus.ACTIVE)
                .orElse(null);
        if (cart == null) return new Cart(user.id(), List.of());

        List<CartItemEntity> items = cartItemRepository.findByCartIdAndStatus(cart.getId(), EntityStatus.ACTIVE);
        Map<Long, Product> productMap = productFinder.findAll(
                        items.stream()
                                .map(CartItemEntity::getProductId)
                                .collect(Collectors.toList())
                ).stream()
                .collect(Collectors.toMap(
                        Product::id,
                        it -> it
                ));

        return new Cart(
                user.id(),
                items.stream()
                        .filter(it -> productMap.containsKey(it.getProductId()))
                        .map(it ->
                                new CartItem(
                                        it.getId(),
                                        productMap.get(it.getProductId()),
                                        it.getQuantity()
                                )
                        )
                        .collect(Collectors.toList())
        );
    }

    public List<SharedCartSummary> getSharedCarts(User user) {
        List<CartEntity> owned = cartRepository.findByOwnerIdAndTypeAndStatusOrderByCreatedAtDesc(
                user.id(), CartType.SHARED, EntityStatus.ACTIVE
        );
        List<Long> memberCartIds = cartMemberRepository.findByUserIdAndStatus(user.id(), EntityStatus.ACTIVE)
                .stream()
                .map(CartMemberEntity::getCartId)
                .toList();
        List<CartEntity> joined = memberCartIds.isEmpty()
                ? List.of()
                : cartRepository.findByIdInAndTypeAndStatus(memberCartIds, CartType.SHARED, EntityStatus.ACTIVE);

        Map<Long, CartEntity> carts = new LinkedHashMap<>();
        owned.forEach(cart -> carts.put(cart.getId(), cart));
        joined.forEach(cart -> carts.putIfAbsent(cart.getId(), cart));

        List<Long> cartIds = new ArrayList<>(carts.keySet());
        Map<Long, Long> itemCounts = cartIds.isEmpty()
                ? Map.of()
                : cartItemRepository.findByCartIdInAndStatus(cartIds, EntityStatus.ACTIVE).stream()
                        .collect(Collectors.groupingBy(CartItemEntity::getCartId, Collectors.counting()));
        LocalDateTime now = LocalDateTime.now();

        return carts.values().stream()
                .sorted(Comparator.comparing(CartEntity::getCreatedAt).reversed())
                .map(cart -> new SharedCartSummary(
                        cart.getId(),
                        cart.getName(),
                        cart.isOwner(user.id()) ? SharedCartRole.OWNER : SharedCartRole.MEMBER,
                        cart.isExpired(now) ? SharedCartState.EXPIRED : SharedCartState.ACTIVE,
                        itemCounts.getOrDefault(cart.getId(), 0L),
                        cart.getCreatedAt(),
                        cart.getExpiredAt(),
                        cart.getShareToken()
                ))
                .toList();
    }

    public SharedCart getSharedCart(User user, Long cartId) {
        CartEntity cart = cartRepository.findByIdAndTypeAndStatus(cartId, CartType.SHARED, EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        CartAccess access = getAccess(user, cart);
        access.validate(user, LocalDateTime.now());

        List<CartItemEntity> entities = cartItemRepository.findByCartIdAndStatus(cartId, EntityStatus.ACTIVE);
        Map<Long, Product> productMap = productFinder.findAll(
                        entities.stream().map(CartItemEntity::getProductId).toList()
                ).stream()
                .collect(Collectors.toMap(Product::id, Function.identity()));
        List<CartItem> items = entities.stream()
                .filter(entity -> productMap.containsKey(entity.getProductId()))
                .map(entity -> new CartItem(
                        entity.getId(),
                        productMap.get(entity.getProductId()),
                        entity.getQuantity()
                ))
                .toList();

        return new SharedCart(
                cart.getId(), cart.getName(), getRole(cart, access), SharedCartState.ACTIVE,
                cart.getCreatedAt(), cart.getExpiredAt(), items
        );
    }

    private CartAccess getAccess(User user, CartEntity cart) {
        if (cart.isOwner(user.id())) {
            return new CartAccess(
                    cart.getShareToken(), cart.getId(), cart.getType(), cart.getOwnerId(), cart.getExpiredAt(),
                    cart.getCreatedAt(), cart.getUpdatedAt()
            );
        }

        return cartMemberRepository.findByCartIdAndUserId(cart.getId(), user.id())
                .filter(CartMemberEntity::isActive)
                .map(member -> new CartAccess(
                        cart.getShareToken(), cart.getId(), cart.getType(), member.getUserId(), cart.getExpiredAt(),
                        member.getCreatedAt(), member.getUpdatedAt()
                ))
                .orElseThrow(() -> new CoreException(ErrorType.CART_ACCESS_DENIED));
    }

    private SharedCartRole getRole(CartEntity cart, CartAccess access) {
        return cart.isOwner(access.userId()) ? SharedCartRole.OWNER : SharedCartRole.MEMBER;
    }
}

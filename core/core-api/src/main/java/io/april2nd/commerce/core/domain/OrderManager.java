package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.OrderItemRepository;
import io.april2nd.commerce.storage.db.core.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderManager {
    private final OrderKeyGenerator orderKeyGenerator;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductFinder productFinder;

    @Transactional
    public String create(Long userId, NewOrder newOrder, List<Product> products) {
        return null;
    }

    private BigDecimal calculateTotalPrice(List<NewOrderItem> items, Map<Long, Product> productMap) {
        return items.stream()
                .map(item -> {
                    Product product = productMap.get(item.productId());

                    if (product == null) throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);

                    return product.price().discountedPrice()
                            .multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String createOrderName(List<NewOrderItem> items, Map<Long, Product> productMap) {
        NewOrderItem first = items.stream().findFirst().orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        Product product = productMap.get(first.productId());

        if (product == null) throw new CoreException(ErrorType.PRODUCT_MISMATCH_IN_ORDER);

        String name = product.name();

        return items.size() > 1 ? name + (" 외 " + (items.size() - 1) + "개") : name;
    }
}

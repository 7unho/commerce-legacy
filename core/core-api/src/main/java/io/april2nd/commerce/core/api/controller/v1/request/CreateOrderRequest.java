package io.april2nd.commerce.core.api.controller.v1.request;

import io.april2nd.commerce.core.domain.NewOrder;
import io.april2nd.commerce.core.domain.NewOrderItem;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;

import java.util.List;

public record CreateOrderRequest(
        List<OrderProductRequest> products
) {
    public NewOrder toNewOrder(User user) {
        if (products == null || products.isEmpty()) {
            throw new CoreException(ErrorType.INVALID_REQUEST);
        }

        return new NewOrder(
                user.id(),
                products.stream()
                        .flatMap(product -> {
                            if (product == null) throw new CoreException(ErrorType.INVALID_REQUEST);
                            return product.toNewOrderItems().stream();
                        })
                        .toList()
        );
    }

    public record OrderProductRequest(
            Long productId,
            List<OrderOptionRequest> options
    ) {
        private List<NewOrderItem> toNewOrderItems() {
            if (productId == null || options == null || options.isEmpty()) {
                throw new CoreException(ErrorType.INVALID_REQUEST);
            }

            return options.stream()
                    .map(option -> {
                        if (option == null) throw new CoreException(ErrorType.INVALID_REQUEST);
                        return option.toNewOrderItem(productId);
                    })
                    .toList();
        }
    }

    public record OrderOptionRequest(
            Long productOptionId,
            Long quantity
    ) {
        private NewOrderItem toNewOrderItem(Long productId) {
            if (productOptionId == null || quantity == null || quantity <= 0) {
                throw new CoreException(ErrorType.INVALID_REQUEST);
            }

            return new NewOrderItem(productId, productOptionId, quantity);
        }
    }
}

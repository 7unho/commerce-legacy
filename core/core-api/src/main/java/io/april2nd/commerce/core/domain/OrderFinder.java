package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.storage.db.core.OrderItemRepository;
import io.april2nd.commerce.storage.db.core.TargetCountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderFinder {
    private final OrderItemRepository orderItemRepository;

    public Map<Long, Long> countOrdersByProductIds(Collection<Long> productIds, LocalDateTime from) {
        return orderItemRepository.findCountsByProductIdsAndOrderStateAndStatusAndCreatedAtAfter(
                        productIds,
                        OrderState.PAID,
                        EntityStatus.ACTIVE,
                        from
                ).stream()
                .collect(Collectors.toMap(
                        TargetCountProjection::getTargetId,
                        TargetCountProjection::getCount
                ));
    }
}

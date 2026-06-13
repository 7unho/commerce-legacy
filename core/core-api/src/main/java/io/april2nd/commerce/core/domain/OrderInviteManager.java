package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.OrderInviteEntity;
import io.april2nd.commerce.storage.db.core.OrderInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderInviteManager {
    private final OrderInviteRepository orderInviteRepository;
    private final OrderKeyGenerator orderKeyGenerator;

    @Transactional
    public String create(Long orderId) {
        String inviteKey = orderKeyGenerator.generate();
        OrderInviteEntity orderInvite = new OrderInviteEntity(orderId, inviteKey);
        orderInviteRepository.save(orderInvite);
        return inviteKey;
    }
}

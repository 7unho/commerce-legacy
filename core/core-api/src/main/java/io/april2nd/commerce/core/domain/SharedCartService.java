package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedCartService {
    private final SharedCartReader sharedCartReader;
    private final SharedCartManager sharedCartManager;

    public CreatedSharedCart create(User user, CreateSharedCart command) {
        return sharedCartManager.create(user, command);
    }

    public List<SharedCartSummary> getSharedCarts(User user) {
        return sharedCartReader.getSharedCarts(user);
    }

    public SharedCart getSharedCart(User user, Long cartId) {
        return sharedCartReader.getSharedCart(user, cartId);
    }

    public Long accept(User user, String shareToken) {
        return sharedCartManager.accept(user, shareToken);
    }

    public void delete(User user, Long cartId) {
        sharedCartManager.delete(user, cartId);
    }
}

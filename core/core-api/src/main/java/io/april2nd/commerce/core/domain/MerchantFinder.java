package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MerchantFinder {
    private final MerchantRepository merchantRepository;

    public List<Merchant> find(Collection<Long> ids) {
        if (ids.isEmpty()) return Collections.emptyList();

        return merchantRepository.findAllById(ids).stream()
                .map(it -> new Merchant(it.getId(), it.getName(), it.getSettlementCycle()))
                .collect(Collectors.toList());
    }
}

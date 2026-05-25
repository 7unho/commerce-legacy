package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MerchantFinder {
    private final MerchantRepository merchantRepository;

    public List<Merchant> findAll(List<Long> merchantIds) {
        return merchantRepository.findAllById(merchantIds).stream()
                .map(it -> new Merchant(it.getId(), it.getName()))
                .collect(Collectors.toList());
    }
}

package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {
    private final MerchantFinder merchantFinder;

    public List<Merchant> findMerchants(List<Long> merchantIds) {
        return merchantFinder.findAll(merchantIds);
    }
}

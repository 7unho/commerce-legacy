package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandFinder brandFinder;

    public List<Brand> find(List<Long> brandIds) {
        return brandFinder.findByIds(brandIds);
    }
}

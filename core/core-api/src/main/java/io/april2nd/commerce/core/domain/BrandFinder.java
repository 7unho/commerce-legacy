package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BrandFinder {
    private final BrandRepository brandRepository;

    public List<Brand> findAll(List<Long> brandIds) {
        return brandRepository.findAllById(brandIds).stream()
                .map(it -> new Brand(it.getId(), it.getName(), it.getImageUrl()))
                .collect(Collectors.toList());
    }
}

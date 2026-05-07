package io.april2nd.commerce.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionRepository extends JpaRepository<ProductOptionEntity, Long> {
    List<ProductOptionEntity> findByProductId(Long productId);
}

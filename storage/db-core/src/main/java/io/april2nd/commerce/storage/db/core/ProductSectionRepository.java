package io.april2nd.commerce.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSectionRepository extends JpaRepository<ProductSectionEntity, Long> {
    List<ProductSectionEntity> findByProductId(Long productId);
}

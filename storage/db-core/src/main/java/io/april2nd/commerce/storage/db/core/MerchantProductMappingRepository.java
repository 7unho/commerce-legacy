package io.april2nd.commerce.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface MerchantProductMappingRepository extends JpaRepository<MerchantProductMappingEntity, Long> {
    List<MerchantProductMappingEntity> findByProductIdIn(Set<Long> productIds);
}

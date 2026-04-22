package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findByIdInAndStatus(Collection<Long> ids, EntityStatus status);
}

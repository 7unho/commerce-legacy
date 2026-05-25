package io.april2nd.commerce.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByUserIdAndIdIn(Long userId, List<Long> ids);
}

package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private EntityStatus status = EntityStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.MIN;

    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.MIN;

    void active() {
        status = EntityStatus.ACTIVE;
    }

    public Boolean isActive() {
        return status == EntityStatus.ACTIVE;
    }

    public void delete() {
        status = EntityStatus.DELETED;
    }

    public Boolean isDeleted() {
        return status == EntityStatus.DELETED;
    }
}

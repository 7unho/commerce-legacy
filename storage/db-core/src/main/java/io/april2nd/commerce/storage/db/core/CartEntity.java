package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CartType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cart",
        indexes = {
                @Index(name = "idx_cart_owner_type", columnList = "ownerId, type"),
                @Index(name = "udx_cart_share_token", columnList = "shareToken", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartEntity extends BaseEntity {
    @Column(nullable = false, updatable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private CartType type;

    private String name;

    @Column(unique = true, updatable = false)
    private String shareToken;

    private LocalDateTime expiredAt;

    public CartEntity(Long ownerId, CartType type, String name, String shareToken, LocalDateTime expiredAt) {
        this.ownerId = ownerId;
        this.type = type;
        this.name = name;
        this.shareToken = shareToken;
        this.expiredAt = expiredAt;
    }

    public boolean isOwner(Long userId) {
        return ownerId.equals(userId);
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt != null && !now.isBefore(expiredAt);
    }
}

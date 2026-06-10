package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cart_member",
        indexes = {
                @Index(name = "udx_cart_member", columnList = "cartId, userId", unique = true),
                @Index(name = "idx_cart_member_user", columnList = "userId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartMemberEntity extends BaseEntity {
    @Column(nullable = false, updatable = false)
    private Long cartId;

    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime acceptedAt;

    public CartMemberEntity(Long cartId, Long userId, LocalDateTime acceptedAt) {
        this.cartId = cartId;
        this.userId = userId;
        this.acceptedAt = acceptedAt;
    }

    public void accept(LocalDateTime acceptedAt) {
        active();
        this.acceptedAt = acceptedAt;
    }
}

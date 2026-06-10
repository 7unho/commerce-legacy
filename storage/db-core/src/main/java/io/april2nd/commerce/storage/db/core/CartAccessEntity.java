package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.CartType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "cart_access",
        indexes = {
                @Index(
                        name = "udx_cart_access_key",
                        columnList = "accessKey",
                        unique = true
                ),
                @Index(
                        name = "udx_cart_access_user",
                        columnList = "cartId, accessUserId",
                        unique = true
                )
        }
)
public class CartAccessEntity extends BaseEntity {

    private String accessKey;
    private Long cartId;

    @Enumerated(EnumType.STRING)
    private CartType type;

    private Long userId;
    private Long accessUserId;
    private LocalDateTime expiredAt;

    public boolean isExpired() {
        return !LocalDateTime.now().isBefore(expiredAt);
    }

    public boolean isNotExpired() {
        return !isExpired();
    }
}

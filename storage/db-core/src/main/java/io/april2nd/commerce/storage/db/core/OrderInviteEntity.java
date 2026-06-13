package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "order_invite",
        indexes = {
                @Index(name = "udx_order_invite_key", columnList = "inviteKey", unique = true),
                @Index(name = "idx_order_id", columnList = "orderId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderInviteEntity extends BaseEntity {
    private Long orderId;
    private String inviteKey;
}

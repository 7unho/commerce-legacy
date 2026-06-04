package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.FavoriteTargetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorite",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_user_target",
                columnNames = {"userId", "targetType", "targetId"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteEntity extends BaseEntity {
    private Long userId;

    @Enumerated(EnumType.STRING)
    private FavoriteTargetType targetType;

    private Long targetId;

    private LocalDateTime favoritedAt;

    public FavoriteEntity(Long userId, FavoriteTargetType targetType, Long targetId, LocalDateTime favoritedAt) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.favoritedAt = favoritedAt;
    }

    public void favorite() {
        this.active();
        this.favoritedAt = LocalDateTime.now();
    }
}
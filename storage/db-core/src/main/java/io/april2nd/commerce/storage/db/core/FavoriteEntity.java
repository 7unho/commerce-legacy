package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FavoriteEntity extends BaseEntity {
    private Long userId;

    private Long productId;

    private LocalDateTime favoritedAt;

    public void favorite() {
        this.active();
        this.favoritedAt = LocalDateTime.now();
    }
}
package io.april2nd.commerce.storage.db.core;

import io.april2nd.commerce.core.enums.ReviewTargetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "review",
        indexes = {
                @Index(name = "udx_user_review", columnList = "userId, reviewKey", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewEntity extends BaseEntity {

    private Long userId;
    private String reviewKey;

    @Enumerated(EnumType.STRING)
    private ReviewTargetType targetType;

    private Long targetId;

    private BigDecimal rate;

    @Column(columnDefinition = "TEXT")
    private String content;

    public void updateContent(BigDecimal rate, String content) {
        this.rate = rate;
        this.content = content;
    }
}
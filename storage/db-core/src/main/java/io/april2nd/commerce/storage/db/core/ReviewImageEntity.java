package io.april2nd.commerce.storage.db.core;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "review_image",
        indexes = {
                @Index(name = "idx_review_id", columnList = "reviewId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewImageEntity extends BaseEntity {

    private Long userId;

    private Long reviewId;

    private Long imageId;

    private String imageUrl;
}

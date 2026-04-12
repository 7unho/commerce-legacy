package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.ReviewEntity;
import io.april2nd.commerce.storage.db.core.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewManager {
    private ReviewRepository reviewRepository;

    public Long add(ReviewKey reviewKey, ReviewTarget target, ReviewContent content) {
        ReviewEntity saved = reviewRepository.save(
                new ReviewEntity(
                        reviewKey.user().id(),
                        reviewKey.key(),
                        target.type(),
                        target.id(),
                        content.rate(),
                        content.content()
                )
        );
        return saved.getId();
    }
}

package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.ReviewEntity;
import io.april2nd.commerce.storage.db.core.ReviewRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public Long update(User user, Long reviewId, ReviewContent content) {
        ReviewEntity found = reviewRepository.findByIdAndUserId(reviewId, user.id())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        found.updateContent(content.rate(), content.content());
        return found.getId();
    }

    @Transactional
    public Long delete(User user, Long reviewId) {
        ReviewEntity found = reviewRepository.findByIdAndUserId(reviewId, user.id())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        found.deleted();
        return found.getId();
    }
}

package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PointType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.file.ImageHandle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewFinder reviewFinder;
    private final ReviewManager reviewManager;
    private final ReviewPolicyValidator reviewPolicyValidator;
    private final PointHandler pointHandler;

    public RateSummary findRateSummary(ReviewTarget target) {
        return reviewFinder.findRateSummary(target);
    }

    public Page<Review> findReviews(ReviewTarget target, OffsetLimit offsetLimit, boolean imageOnly) {
        return reviewFinder.find(target, offsetLimit, imageOnly);
    }

    public Long addReview(User user, ReviewTarget target, ReviewContent content, ImageHandle imageHandle) {
        ReviewKey reviewKey = reviewPolicyValidator.validateNew(user, target);
        ReviewProcessResult result = reviewManager.add(reviewKey, target, content, imageHandle);
        pointHandler.earn(user, PointType.REVIEW, result.id(), result.pointAmount());
        return result.id();
    }

    public Long updateReview(User user, Long reviewId, ReviewContent content, ImageHandle imageHandle) {
        reviewPolicyValidator.validateUpdate(user, reviewId);
        return reviewManager.update(user, reviewId, content, imageHandle);
    }

    public Long removeReview(User user, Long reviewId) {
        ReviewProcessResult result = reviewManager.delete(user, reviewId);
        pointHandler.deduct(user, PointType.REVIEW, result.id(), result.pointAmount());
        return result.id();
    }
}

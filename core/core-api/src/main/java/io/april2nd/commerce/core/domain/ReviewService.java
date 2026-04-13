package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PointType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private ReviewFinder reviewFinder;
    private ReviewManager reviewManager;
    private ReviewPolicyValidator reviewPolicyValidator;
    private PointHandler pointHandler;

    public RateSummary findRateSummary(ReviewTarget target) {
        return reviewFinder.findRateSummary(target);
    }

    public Page<Review> findReviews(ReviewTarget target, OffsetLimit offsetLimit) {
        return reviewFinder.find(target, offsetLimit);
    }

    public Long addReview(User user, ReviewTarget target, ReviewContent content) {
        ReviewKey reviewKey = reviewPolicyValidator.validateNew(user, target);
        Long reviewId = reviewManager.add(reviewKey, target, content);
        pointHandler.earn(user, PointType.REVIEW, reviewId, PointAmount.REVIEW);
        return reviewId;
    }

    public Long updateReview(User user, Long reviewId, ReviewContent content) {
        reviewPolicyValidator.validateUpdate(user, reviewId);
        return reviewManager.update(user, reviewId, content);
    }

    public Long removeReview(User user, Long reviewId) {
        Long deletedReviewId = reviewManager.delete(user, reviewId);
        pointHandler.deduct(user, PointType.REVIEW, deletedReviewId, PointAmount.REVIEW);
        return deletedReviewId;
    }
}

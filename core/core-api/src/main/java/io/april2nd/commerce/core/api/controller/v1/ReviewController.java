package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.request.AddReviewRequest;
import io.april2nd.commerce.core.api.controller.v1.request.UpdateReviewRequest;
import io.april2nd.commerce.core.domain.Review;
import io.april2nd.commerce.core.domain.ReviewService;
import io.april2nd.commerce.core.domain.ReviewTarget;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.enums.ReviewTargetType;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.response.ApiResponse;
import io.april2nd.commerce.core.support.response.PageResponse;
import io.april2nd.commerce.core.api.controller.v1.response.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {
    private ReviewService reviewService;

    @GetMapping("/v1/reviews")
    ApiResponse<PageResponse<ReviewResponse>> getReviews(
            @RequestParam ReviewTargetType targetType,
            @RequestParam Long targetId,
            @RequestParam Integer offset,
            @RequestParam Integer limit) {
        Page<Review> page = reviewService.findReviews(new ReviewTarget(targetType, targetId), new OffsetLimit(offset, limit));
        return ApiResponse.success(new PageResponse<>(ReviewResponse.of(page.content()), page.hasNext()));
    }

    @PostMapping("/v1/reviews")
    ApiResponse<Void> createReview(
            User user,
            @RequestBody AddReviewRequest request) {
        reviewService.addReview(user, request.toTarget(), request.toContent());
        return ApiResponse.success();
    }

    @PutMapping("/v1/reviews/{reviewId}")
    ApiResponse<Void> updateReview(
            User user,
            @PathVariable Long reviewId,
            @RequestBody UpdateReviewRequest request) {
        reviewService.updateReview(user, reviewId, request.toContent());
        return ApiResponse.success();
    }

    @DeleteMapping("/v1/reviews/{reviewId}")
    ApiResponse<Void> deleteReview(
            User user,
            @PathVariable Long reviewId) {
        reviewService.removeReview(user, reviewId);
        return ApiResponse.success();
    }
}

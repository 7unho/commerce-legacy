package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.enums.OrderState;
import io.april2nd.commerce.core.enums.ReviewTargetType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.OrderItemRepository;
import io.april2nd.commerce.storage.db.core.ReviewEntity;
import io.april2nd.commerce.storage.db.core.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewPolicyValidator {
    private OrderItemRepository orderItemRepository;
    private ReviewRepository reviewRepository;

    public ReviewKey validateNew(User user, ReviewTarget target) {
        if (target.type() == ReviewTargetType.PRODUCT) {
            List<String> reviewKeys = orderItemRepository.findRecentOrderItemsForProduct(user.id(), target.id(), OrderState.PAID, LocalDateTime.now().minusDays(14), EntityStatus.ACTIVE).stream()
                    .map(it -> "ORDER_ITEM_" + it.getId())
                    .collect(Collectors.toList());

            Set<String> existReviewKeys = reviewRepository.findByUserIdAndReviewKeyIn(user.id(), reviewKeys).stream()
                    .map(it -> it.getReviewKey())
                    .collect(Collectors.toSet());

            return new ReviewKey(
                    user,
                    reviewKeys.stream()
                            .filter(key -> !existReviewKeys.contains(key))
                            .findFirst()
                            .orElseThrow(() -> new CoreException(ErrorType.REVIEW_HAS_NOT_ORDER))
            );
        }
        throw new UnsupportedOperationException();
    }

    public void validateUpdate(User user, Long reviewId) {
        ReviewEntity review = reviewRepository.findByIdAndUserId(reviewId, user.id())
                .orElseThrow(() -> new CoreException(ErrorType.REVIEW_HAS_NOT_ORDER));

        if (review.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now())) throw new CoreException(ErrorType.REVIEW_UPDATE_EXPIRED);
    }
}

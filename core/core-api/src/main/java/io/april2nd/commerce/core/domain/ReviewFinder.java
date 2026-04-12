package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.storage.db.core.ReviewEntity;
import io.april2nd.commerce.storage.db.core.ReviewRepository;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewFinder {
    private ReviewRepository reviewRepository;

    public RateSummary findRateSummary(ReviewTarget target) {
        List<ReviewEntity> founds = reviewRepository.findByTargetTypeAndTargetId(target.type(), target.id())
                .stream()
                .filter(ReviewEntity::isActive)
                .collect(Collectors.toList());

        return founds.isEmpty()
                ? RateSummary.EMPTY
                : new RateSummary(
                founds.stream()
                        .map(ReviewEntity::getRate)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(founds.size())),
                (long) founds.size()
        );
    }

    public Page<Review> find(ReviewTarget target, OffsetLimit offsetLimit) {
        Slice<ReviewEntity> result = reviewRepository.findByTargetTypeAndTargetIdAndStatus(
                target.type(),
                target.id(),
                EntityStatus.ACTIVE,
                offsetLimit.toPageable()
        );

        return new Page(
                result.getContent()
                        .stream()
                        .map(it ->
                                new Review(
                                        it.getId(),
                                        it.getUserId(),
                                        new ReviewTarget(it.getTargetType(), it.getTargetId()),
                                        new ReviewContent(it.getRate(), it.getContent())
                                ))
                        .collect(Collectors.toList()),
                result.hasNext()
        );
    }
}

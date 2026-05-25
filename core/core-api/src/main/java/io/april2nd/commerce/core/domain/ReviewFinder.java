package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.storage.db.core.ReviewEntity;
import io.april2nd.commerce.storage.db.core.ReviewImageEntity;
import io.april2nd.commerce.storage.db.core.ReviewImageRepository;
import io.april2nd.commerce.storage.db.core.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReviewFinder {
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;

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

    public Page<Review> find(ReviewTarget target, OffsetLimit offsetLimit, boolean imageOnly) {
        Slice<ReviewEntity> result;
        if (Boolean.TRUE.equals(imageOnly)) {
            result = reviewRepository.findImageReviewsByTargetTypeAndTargetIdAndStatus(
                    target.type(),
                    target.id(),
                    EntityStatus.ACTIVE,
                    offsetLimit.toPageable()
            );
        } else {
            result = reviewRepository.findByTargetTypeAndTargetIdAndStatus(
                    target.type(),
                    target.id(),
                    EntityStatus.ACTIVE,
                    offsetLimit.toPageable()
            );
        }

        List<Long> reviewIds = result.getContent().stream()
                .map(ReviewEntity::getId)
                .collect(Collectors.toList());

        Map<Long, List<ReviewImage>> imagesMap = reviewIds.isEmpty() ? Collections.emptyMap() :
                reviewImageRepository.findByReviewIdInAndStatus(reviewIds, EntityStatus.ACTIVE).stream()
                        .sorted(Comparator.comparing(ReviewImageEntity::getId))
                        .collect(Collectors.groupingBy(
                                ReviewImageEntity::getReviewId,
                                Collectors.mapping(
                                        it -> new ReviewImage(it.getId(), it.getImageUrl()),
                                        Collectors.toList()
                                )
                        ));

        return new Page<>(
                result.getContent()
                        .stream()
                        .map(it ->
                                new Review(
                                        it.getId(),
                                        it.getUserId(),
                                        new ReviewTarget(it.getTargetType(), it.getTargetId()),
                                        new ReviewContent(it.getRate(), it.getContent()),
                                        imagesMap.getOrDefault(it.getId(), Collections.emptyList())
                                ))
                        .collect(Collectors.toList()),
                result.hasNext()
        );
    }
}

package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.EntityStatus;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.core.support.file.ImageHandle;
import io.april2nd.commerce.storage.db.core.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewManager {
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public ReviewProcessResult add(ReviewKey reviewKey, ReviewTarget target, ReviewContent content, ImageHandle imageHandle) {
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

        if (imageHandle.hasImagesToAdd()) {
            List<ImageEntity> uploadedImages = imageRepository.findByUserIdAndIdIn(saved.getUserId(), imageHandle.addImageIds());

            if (imageHandle.addImageIds().size() != uploadedImages.size())
                throw new CoreException(ErrorType.INVALID_REQUEST);

            reviewImageRepository.saveAll(
                    uploadedImages.stream()
                            .map(it -> new ReviewImageEntity(
                                    saved.getUserId(),
                                    saved.getId(),
                                    it.getId(),
                                    it.getImageUrl()
                            ))
                            .toList()
            );
        }

        return new ReviewProcessResult(saved.getId(), (imageHandle.hasImagesToAdd()) ? ReviewFormat.IMAGE : ReviewFormat.TEXT);
    }

    @Transactional
    public Long update(User user, Long reviewId, ReviewContent content, ImageHandle imageHandle) {
        ReviewEntity found = reviewRepository.findByIdAndUserId(reviewId, user.id())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        found.updateContent(content.rate(), content.content());

        List<ReviewImageEntity> existingImages = reviewImageRepository.findByReviewIdAndStatus(reviewId, EntityStatus.ACTIVE);

        // 저장된 이미지 삭제
        if (imageHandle.hasImagesToDelete()) {
            existingImages.stream()
                    .filter(it -> imageHandle.deleteImageIds().contains(it.getId()))
                    .forEach(reviewImageRepository::delete);
        }

        // 기존에 이미지가 있었는데, 삭제 후 남은 이미지도 없고 새로 추가된 이미지도 없다면 error
        if (!existingImages.isEmpty() && existingImages.stream().filter(BaseEntity::isActive).count() <= 0 && !imageHandle.hasImagesToAdd()) {
            throw new CoreException(ErrorType.REVIEW_CANNOT_DELETE_ALL_IMAGES);
        }

        if (imageHandle.hasImagesToAdd()) {
            List<ImageEntity> uploadedImages = imageRepository.findByUserIdAndIdIn(found.getUserId(), imageHandle.addImageIds());

            if (imageHandle.addImageIds().size() != uploadedImages.size())
                throw new CoreException(ErrorType.INVALID_REQUEST);

            reviewImageRepository.saveAll(
                    uploadedImages.stream()
                            .map(it ->
                                    new ReviewImageEntity(
                                            found.getUserId(),
                                            found.getId(),
                                            it.getId(),
                                            it.getImageUrl()
                                    ))
                            .toList()
            );
        }

        return found.getId();
    }

    @Transactional
    public ReviewProcessResult delete(User user, Long reviewId) {
        ReviewEntity found = reviewRepository.findByIdAndUserId(reviewId, user.id())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        found.delete();

        // 이미지도 함께 삭제
        List<ReviewImageEntity> images = reviewImageRepository.findByReviewIdAndStatus(reviewId, EntityStatus.ACTIVE);
        images.stream().forEach(BaseEntity::delete);

        return new ReviewProcessResult(found.getId(), (images.isEmpty()) ? ReviewFormat.TEXT : ReviewFormat.IMAGE);
    }
}

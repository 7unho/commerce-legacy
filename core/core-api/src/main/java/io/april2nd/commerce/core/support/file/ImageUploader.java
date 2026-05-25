package io.april2nd.commerce.core.support.file;

import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.storage.db.core.ImageEntity;
import io.april2nd.commerce.storage.db.core.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ImageUploader {
    private final ImageRepository imageRepository;

    public UploadResult uploadImage(User user, MultipartFile file) {
        // Mock S3 업로드 - Mock URL 생성
        String mockS3Url = uploadToS3Mock(file);

        ImageEntity imageEntity = new ImageEntity(user.id(), mockS3Url, Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"));

        ImageEntity savedEntity = imageRepository.save(imageEntity);

        return new UploadResult(savedEntity.getId(), savedEntity.getImageUrl());
    }

    private String uploadToS3Mock(MultipartFile file) {
        String uniqueId = UUID.randomUUID().toString();
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
        return "https://mock-s3-bucket.s3.amazonaws.com/images/%s_%s".formatted(uniqueId, filename);
    }
}

package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.file.ImageUploader;
import io.april2nd.commerce.core.support.file.UploadResult;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final ImageUploader imageUploader;

    @PostMapping("/v1/images/upload")
    ApiResponse<UploadResult> uploadImage(User user, @RequestParam MultipartFile file) {
        var uploadedImage = imageUploader.uploadImage(user, file);

        return ApiResponse.success(uploadedImage);
    }
}

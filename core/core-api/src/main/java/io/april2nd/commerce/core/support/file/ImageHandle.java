package io.april2nd.commerce.core.support.file;

import java.util.List;

public record ImageHandle(
        List<Long> addImageIds,
        List<Long> deleteImageIds
) {
    public boolean hasImagesToAdd() {
        return !addImageIds.isEmpty();
    }

    public boolean hasImagesToDelete() {
        return !deleteImageIds.isEmpty();
    }
}

package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.assembler.FavoriteAssembler;
import io.april2nd.commerce.core.api.controller.v1.request.ApplyFavoriteRequest;
import io.april2nd.commerce.core.api.controller.v1.response.FavoriteResponse;
import io.april2nd.commerce.core.domain.Favorite;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.OffsetLimit;
import io.april2nd.commerce.core.support.Page;
import io.april2nd.commerce.core.support.response.ApiResponse;
import io.april2nd.commerce.core.support.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteAssembler favoriteAssembler;

    @GetMapping("/v1/favorites")
    ApiResponse<PageResponse<FavoriteResponse>> getFavorites(
            User user,
            @RequestParam Integer offset,
            @RequestParam Integer limit) {
        Page<Favorite> page = favoriteAssembler.findFavorites(user, new OffsetLimit(offset, limit));
        return ApiResponse.success(new PageResponse<>(FavoriteResponse.of(page.content()), page.hasNext()));
    }

    @PostMapping("/v1/favorites")
    ApiResponse<Void> applyFavorite(
            User user,
            @RequestBody ApplyFavoriteRequest request) {
        favoriteAssembler.applyFavorite(user, request);
        return ApiResponse.success();
    }
}

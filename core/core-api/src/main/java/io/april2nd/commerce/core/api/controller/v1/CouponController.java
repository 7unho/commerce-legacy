package io.april2nd.commerce.core.api.controller.v1;

import io.april2nd.commerce.core.api.controller.v1.response.OwnedCouponResponse;
import io.april2nd.commerce.core.domain.OwnedCoupon;
import io.april2nd.commerce.core.domain.OwnedCouponService;
import io.april2nd.commerce.core.domain.User;
import io.april2nd.commerce.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {
    private OwnedCouponService ownedCouponService;

    @PostMapping("/v1/coupons/{couponId}/download")
    ApiResponse<Void> download(
            User user,
            @PathVariable Long couponId) {
        ownedCouponService.download(user, couponId);
        return ApiResponse.success();
    }

    @GetMapping("/v1/owned-coupons")
    ApiResponse<List<OwnedCouponResponse>> getOwnedCoupons(User user) {
        List<OwnedCoupon> coupons =  ownedCouponService.getOwnedCoupons(user);
        return ApiResponse.success(OwnedCouponResponse.of(coupons));
    }
}

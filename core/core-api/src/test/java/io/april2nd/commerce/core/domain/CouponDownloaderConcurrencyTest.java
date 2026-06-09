package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.CouponEntity;
import io.april2nd.commerce.storage.db.core.CouponRepository;
import io.april2nd.commerce.storage.db.core.OwnedCouponEntity;
import io.april2nd.commerce.storage.db.core.OwnedCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponDownloaderConcurrencyTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private OwnedCouponRepository ownedCouponRepository;

    @InjectMocks
    private OwnedCouponAdder ownedCouponAdder;

    @Test
    @DisplayName("동일 사용자가 동일 쿠폰을 동시에 다운로드할 때 하나만 성공해야 한다")
    void downloadConcurrencyTest() throws InterruptedException {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        CouponEntity coupon = new CouponEntity("테스트 쿠폰", null, null, 100L, null, LocalDateTime.now().plusDays(1));
        
        // CouponDownloader를 수동으로 생성 (InjectMocks 대신 명시적 생성 권장 - 가이드라인 준수)
        CouponDownloader couponDownloader = new CouponDownloader(couponRepository, ownedCouponAdder);

        given(couponRepository.findByIdAndStatusAndExpiredAtAfter(anyLong(), any(), any()))
                .willReturn(Optional.of(coupon));

        // 첫 번째 쓰레드는 null 반환(존재하지 않음), 이후 쓰레드들은 경합 상황 시뮬레이션
        // findByUserIdAndCouponId가 호출될 때마다 null을 반환하게 하고, save에서 예외를 던지게 함
        given(ownedCouponRepository.findByUserIdAndCouponId(userId, couponId))
                .willReturn(null);

        // 첫 번째 호출은 성공, 두 번째부터는 DataIntegrityViolationException 던지도록 설정
        AtomicInteger callCount = new AtomicInteger(0);
        given(ownedCouponRepository.save(any(OwnedCouponEntity.class))).willAnswer(invocation -> {
            if (callCount.getAndIncrement() == 0) {
                return invocation.getArgument(0);
            } else {
                throw new DataIntegrityViolationException("Unique constraint violation");
            }
        });

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    couponDownloader.download(userId, couponId);
                    successCount.incrementAndGet();
                } catch (CoreException e) {
                    if (e.getErrorType() == ErrorType.COUPON_ALREADY_DOWNLOADED) {
                        failCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        verify(ownedCouponRepository, atLeastOnce()).save(any(OwnedCouponEntity.class));
    }
}

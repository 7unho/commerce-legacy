package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.enums.PointType;
import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.PointBalanceEntity;
import io.april2nd.commerce.storage.db.core.PointBalanceRepository;
import io.april2nd.commerce.storage.db.core.PointHistoryEntity;
import io.april2nd.commerce.storage.db.core.PointHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PointHandler {
    private PointBalanceRepository pointBalanceRepository;
    private PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void earn(User user, PointType type, Long targetId, BigDecimal amount) {
        if (amount == BigDecimal.ZERO) return;

        // NOTE: 모든 유저는 가입 시 Point 테이블이 생성됨
        PointBalanceEntity balance = pointBalanceRepository.findByUserId(user.id())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        balance.apply(amount);
        pointHistoryRepository.save(
                new PointHistoryEntity(
                        user.id(),
                        type,
                        targetId,
                        amount,
                        balance.getBalance()
                )
        );
    }

    @Transactional
    public void deduct(User user, PointType type, Long targetId, BigDecimal amount) {
        if (amount == BigDecimal.ZERO) return;

        // NOTE: 모든 유저는 가입 시 Point 테이블이 생성됨
        PointBalanceEntity balance = pointBalanceRepository.findByUserId(user.id())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));
        balance.apply(amount.negate());
        pointHistoryRepository.save(
                new PointHistoryEntity(
                        user.id(),
                        type,
                        targetId,
                        amount.negate(),
                        balance.getBalance()
                )
        );
    }
}

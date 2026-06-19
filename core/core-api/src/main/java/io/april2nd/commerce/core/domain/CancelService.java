package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelService {
    private final CancelValidator cancelValidator;
    private final CancelProcessor cancelProcessor;

    public Long cancel(User user, CancelAction action) {
        cancelValidator.validate(user, action);

        /**
         * NOTE: PG 취소 API 호출 => 성공 시 다음 로직으로 진행 | 실패 시 예외 발생
         */
        return cancelProcessor.cancel(action);
    }
}

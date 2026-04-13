package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.core.support.error.CoreException;
import io.april2nd.commerce.core.support.error.ErrorType;
import io.april2nd.commerce.storage.db.core.PointBalanceEntity;
import io.april2nd.commerce.storage.db.core.PointBalanceRepository;
import io.april2nd.commerce.storage.db.core.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {
    private PointBalanceRepository pointBalanceRepository;
    private PointHistoryRepository pointHistoryRepository;

    public PointBalance balance(User user) {
        PointBalanceEntity found = pointBalanceRepository.findByUserId(user.id())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA));

        return new PointBalance(user.id(), found.getBalance());
    }

    public List<PointHistory> histories(User user) {
        return pointHistoryRepository.findByUserId(user.id()).stream()
                .map(history ->
                        new PointHistory(
                                history.getId(),
                                history.getUserId(),
                                history.getType(),
                                history.getReferenceId(),
                                history.getAmount(),
                                history.getCreatedAt()
                        ))
                .collect(Collectors.toList());
    }
}

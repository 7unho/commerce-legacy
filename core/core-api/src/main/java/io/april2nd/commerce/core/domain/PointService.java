package io.april2nd.commerce.core.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointReader pointReader;

    public PointBalance balance(User user) {
        return pointReader.balance(user);
    }

    public List<PointHistory> histories(User user) {
        return pointReader.histories(user);
    }
}

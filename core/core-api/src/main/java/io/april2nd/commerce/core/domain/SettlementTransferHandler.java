package io.april2nd.commerce.core.domain;

import io.april2nd.commerce.storage.db.core.SettlementEntity;
import io.april2nd.commerce.storage.db.core.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettlementTransferHandler {
    private final SettlementRepository settlementRepository;

    @Transactional
    public void success(List<Settlement> settlements) {
        List<Long> ids = settlements.stream()
                .map(Settlement::id)
                .collect(Collectors.toList());

        List<SettlementEntity> entities = settlementRepository.findAllById(ids);
        entities.stream().forEach(SettlementEntity::sent);

        settlementRepository.saveAll(entities);
    }
}

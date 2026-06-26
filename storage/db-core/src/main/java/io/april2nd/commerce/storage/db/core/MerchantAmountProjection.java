package io.april2nd.commerce.storage.db.core;

import java.math.BigDecimal;

public interface MerchantAmountProjection {
    public Long getMerchantId();
    public BigDecimal getAmount();
}

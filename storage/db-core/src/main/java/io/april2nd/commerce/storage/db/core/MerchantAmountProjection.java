package io.april2nd.commerce.storage.db.core;

import java.math.BigDecimal;

public interface MerchantAmountProjection {
    Long getMerchantId();
    BigDecimal getAmount();
}

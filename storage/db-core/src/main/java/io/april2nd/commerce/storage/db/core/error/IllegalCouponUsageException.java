package io.april2nd.commerce.storage.db.core.error;

public class IllegalCouponUsageException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IllegalCouponUsageException(String message) {
        super(message);
    }

    public IllegalCouponUsageException(String message, Throwable cause) {
        super(message, cause);
    }
}
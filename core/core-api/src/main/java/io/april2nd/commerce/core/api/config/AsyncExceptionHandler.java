package io.april2nd.commerce.core.api.config;

import io.april2nd.commerce.core.support.error.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.logging.LogLevel;

import java.lang.reflect.Method;

public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable e, Method method, Object... params) {

        if (e instanceof CoreException coreException) {

            LogLevel logLevel = coreException.getErrorType().getLogLevel();

            switch (logLevel) {
                case ERROR:
                    log.error("CoreException : {}", coreException.getMessage(), coreException);
                    break;
                case WARN:
                    log.warn("CoreException : {}", coreException.getMessage(), coreException);
                    break;
                default:
                    log.info("CoreException : {}", coreException.getMessage(), coreException);
                    break;
            }

        } else {
            log.error("Exception : {}", e.getMessage(), e);
        }
    }
}
package com.zjhy.love.worktools.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * 重试工具类
 */
public class RetryUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryUtil.class);

    public static <T> T retry(Callable<T> task, Predicate<Exception> retryable, int maxAttempts, long delayMs) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                if (!retryable.test(e) || attempt == maxAttempts) {
                    throw e;
                }
                LOGGER.warn("第{}次尝试失败: {}, {}ms后重试", attempt, e.getMessage(), delayMs);
                Thread.sleep(delayMs);
            }
        }
        throw lastException;
    }
} 
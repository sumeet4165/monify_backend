package com.example.MONEYMANAGER.middleware;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AiRateLimiter {

    private static class UserLimit {
        final long windowStart;
        final AtomicInteger count;

        UserLimit(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }

    private final ConcurrentHashMap<String, UserLimit> buckets = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS = 10;
    private final long WINDOW_MS = 60000; // 1 minute window

    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public boolean allowRequest(String userId) {
        long now = getCurrentTime();
        UserLimit limit = buckets.compute(userId, (key, current) -> {
            if (current == null || now - current.windowStart > WINDOW_MS) {
                return new UserLimit(now);
            } else {
                current.count.incrementAndGet();
                return current;
            }
        });
        return limit.count.get() <= MAX_REQUESTS;
    }
}

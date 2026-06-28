package com.example.MONEYMANAGER.middleware;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AiRateLimiterTest {

    private static class TestableAiRateLimiter extends AiRateLimiter {
        long simulatedTime = 1000000L;

        @Override
        protected long getCurrentTime() {
            return simulatedTime;
        }

        void advanceTime(long ms) {
            simulatedTime += ms;
        }
    }

    @Test
    void testRateLimiter_WithinLimit_AllowsRequests() {
        TestableAiRateLimiter rateLimiter = new TestableAiRateLimiter();
        String user = "user1";

        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allowRequest(user), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void testRateLimiter_ExceedingLimit_BlocksRequests() {
        TestableAiRateLimiter rateLimiter = new TestableAiRateLimiter();
        String user = "user2";

        for (int i = 0; i < 10; i++) {
            rateLimiter.allowRequest(user);
        }

        // 11th request should be blocked
        assertFalse(rateLimiter.allowRequest(user), "Request 11 should be blocked");
    }

    @Test
    void testRateLimiter_WindowResets_AllowsRequestsAgain() {
        TestableAiRateLimiter rateLimiter = new TestableAiRateLimiter();
        String user = "user3";

        // Perform 10 requests at the same time
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allowRequest(user));
        }
        // 11th should be blocked
        assertFalse(rateLimiter.allowRequest(user));

        // Advance simulated time by 61 seconds (exceeding WINDOW_MS of 60000)
        rateLimiter.advanceTime(61000L);

        // Next requests should be allowed again
        assertTrue(rateLimiter.allowRequest(user), "Should allow request after window resets");
    }
}

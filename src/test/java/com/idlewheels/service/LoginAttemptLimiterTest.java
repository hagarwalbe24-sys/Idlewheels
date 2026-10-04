package com.idlewheels.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptLimiterTest {
    @Test
    void blocksFifthFailureOnlyForSameEmailAndIpAndSuccessClearsIt() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        String email = "user@example.com";
        String ip = "127.0.0.1";

        for (int i = 0; i < 4; i++) limiter.recordFailure(email, ip);
        assertFalse(limiter.isBlocked(email, ip));
        limiter.recordFailure(email, ip);
        assertTrue(limiter.isBlocked(email, ip));
        assertFalse(limiter.isBlocked(email, "127.0.0.2"));

        limiter.recordSuccess(email, ip);
        assertFalse(limiter.isBlocked(email, ip));
    }
}

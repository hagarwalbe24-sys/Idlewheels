package com.idlewheels.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class LoginAttemptLimiter {
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private final Clock clock;
    private final Map<String, AttemptWindow> attempts = new HashMap<>();

    public LoginAttemptLimiter() {
        this(Clock.systemUTC());
    }

    LoginAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    public synchronized boolean isBlocked(String email, String ipAddress) {
        Instant now = clock.instant();
        removeExpired(now);
        AttemptWindow window = attempts.get(key(email, ipAddress));
        return window != null && window.failures >= MAX_FAILURES && now.isBefore(window.startedAt.plus(WINDOW));
    }

    public synchronized void recordFailure(String email, String ipAddress) {
        Instant now = clock.instant();
        String key = key(email, ipAddress);
        AttemptWindow window = attempts.get(key);
        if (window == null || !now.isBefore(window.startedAt.plus(WINDOW))) {
            attempts.put(key, new AttemptWindow(now, 1));
        } else {
            window.failures++;
        }
    }

    public synchronized void recordSuccess(String email, String ipAddress) {
        attempts.remove(key(email, ipAddress));
    }

    private void removeExpired(Instant now) {
        attempts.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().startedAt.plus(WINDOW)));
    }

    private String key(String email, String ipAddress) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedIp = ipAddress == null ? "unknown" : ipAddress;
        return normalizedEmail + "\u0000" + normalizedIp;
    }

    private static final class AttemptWindow {
        private final Instant startedAt;
        private int failures;

        private AttemptWindow(Instant startedAt, int failures) {
            this.startedAt = startedAt;
            this.failures = failures;
        }
    }
}

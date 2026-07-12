package com.mhsa.backend.auth.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * Periodically purges refresh tokens whose rolling expiry has passed, keeping the table bounded.
 * Scheduling is already enabled application-wide via {@code @EnableScheduling}.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);

    private final RefreshTokenRepository repository;

    /** Runs daily at 03:15 server time. */
    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        int deleted = repository.deleteExpired(Instant.now());
        if (deleted > 0) {
            log.info("Purged {} expired refresh token(s)", deleted);
        }
    }
}

package com.mhsa.backend.auth.jwt;

/**
 * Service for managing JWT token blacklist (revoked tokens).
 * Implementation provided by each service (e.g., Auth, Tracking, AI).
 */
public interface TokenBlacklistService {

    boolean isBlacklisted(String token);

    void blacklist(String token, long expirationTimeMs);
}

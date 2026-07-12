package com.mhsa.backend.auth.service;

/**
 * Thrown when a presented refresh token is unknown, expired, or already used (rotated/revoked).
 * The controller maps this to a 401 so clients clear stored tokens and route to login.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}

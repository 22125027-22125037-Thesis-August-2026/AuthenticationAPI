package com.mhsa.backend.common.util;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mhsa.backend.auth.model.Role;
import com.mhsa.backend.auth.security.AuthenticatedUserPrincipal;
import com.mhsa.backend.common.exception.UnauthorizedException;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getCurrentProfileId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal authenticatedUserPrincipal
                && authenticatedUserPrincipal.profileId() != null) {
            return authenticatedUserPrincipal.profileId();
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedException("Invalid authenticated profile id");
        }
    }

    public static Role getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal authenticatedUserPrincipal
                && authenticatedUserPrincipal.role() != null) {
            return authenticatedUserPrincipal.role();
        }

        throw new UnauthorizedException("Invalid authenticated role");
    }
}

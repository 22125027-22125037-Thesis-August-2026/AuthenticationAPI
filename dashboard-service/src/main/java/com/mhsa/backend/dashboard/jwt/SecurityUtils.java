package com.mhsa.backend.dashboard.jwt;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mhsa.backend.auth.jwt.AuthenticatedUserPrincipal;

public class SecurityUtils {

    public static UUID getCurrentProfileId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
            return principal.profileId();
        }
        throw new IllegalStateException("No authenticated principal found");
    }

    public static String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
            return principal.role().toString();
        }
        throw new IllegalStateException("No authenticated principal found");
    }
}

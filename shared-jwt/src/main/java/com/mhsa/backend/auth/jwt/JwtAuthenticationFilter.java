package com.mhsa.backend.auth.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    @Setter
    private Object tokenBlacklistService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = parseJwt(request);

        if (token != null && jwtUtils.validateJwtToken(token)) {

            if (tokenBlacklistService != null) {
                try {
                    if ((boolean) tokenBlacklistService.getClass().getMethod("isBlacklisted", String.class).invoke(tokenBlacklistService, token)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked (Logged out)");
                        return;
                    }
                } catch (Exception e) {
                    // If blacklist check fails, continue with auth
                }
            }

            String userId = jwtUtils.getUserIdFromJwtToken(token);
            String email = jwtUtils.getEmailFromJwtToken(token);
            UUID profileId = jwtUtils.getProfileIdFromJwtToken(token);
            var role = jwtUtils.getRoleFromJwtToken(token);

            UserDetails userDetails = null;
            if (email != null && !email.isBlank()) {
                try {
                    userDetails = userDetailsService.loadUserByUsername(email);
                } catch (Exception e) {
                    // User not found in UserDetailsService - this is acceptable for JWT-based auth
                    // where we rely on the JWT claims rather than a local user database
                }
            }

            UsernamePasswordAuthenticationToken authentication
                    = new UsernamePasswordAuthenticationToken(
                            new AuthenticatedUserPrincipal(UUID.fromString(userId), profileId, email, role),
                            null,
                            userDetails != null ? userDetails.getAuthorities() : Collections.emptyList()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        return jwtUtils.resolveBearerToken(request.getHeader("Authorization"));
    }
}

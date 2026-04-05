package com.mhsa.backend.auth.config;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mhsa.backend.auth.service.CustomUserDetailsService;
import com.mhsa.backend.auth.service.TokenBlacklistService;
import com.mhsa.backend.auth.security.AuthenticatedUserPrincipal;
import com.mhsa.backend.auth.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Lấy token từ header
        String token = parseJwt(request);

        // 2. Nếu có token và token hợp lệ
        if (token != null && jwtUtils.validateJwtToken(token)) {

            if (tokenBlacklistService.isBlacklisted(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked (Logged out)");
                return;
            }

            // 3. Extract userId from subject and email/profile/role claims from token
            String userId = jwtUtils.getUserIdFromJwtToken(token);
            String email = jwtUtils.getEmailFromJwtToken(token);
            UUID profileId = jwtUtils.getProfileIdFromJwtToken(token);
            var role = jwtUtils.getRoleFromJwtToken(token);

            // 4. Load authorities (prefer email claim, fallback to empty authorities)
            UserDetails userDetails = null;
            if (email != null && !email.isBlank()) {
                userDetails = userDetailsService.loadUserByUsername(email);
            }

            // 5. Set authenticated principal into SecurityContext
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
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}

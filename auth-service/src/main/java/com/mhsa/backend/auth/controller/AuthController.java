package com.mhsa.backend.auth.controller;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mhsa.backend.auth.dto.AuthResponse;
import com.mhsa.backend.auth.dto.ChangePasswordRequest;
import com.mhsa.backend.auth.dto.LicenseResponse;
import com.mhsa.backend.auth.dto.LoginRequest;
import com.mhsa.backend.auth.dto.ProfileUpdateRequest;
import com.mhsa.backend.auth.dto.RefreshRequest;
import com.mhsa.backend.auth.dto.RegisterRequest;
import com.mhsa.backend.auth.dto.UserResponse;
import com.mhsa.backend.auth.jwt.AuthenticatedUserPrincipal;
import com.mhsa.backend.auth.service.AuthService;
import com.mhsa.backend.auth.service.FileStorageService;
import com.mhsa.backend.auth.service.InvalidRefreshTokenException;
import com.mhsa.backend.auth.service.RefreshTokenService;
import com.mhsa.backend.auth.service.TherapistLicenseService;
import com.mhsa.backend.auth.service.TokenBlacklistService;
import com.mhsa.backend.auth.jwt.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;
    private final FileStorageService fileStorageService;
    private final TherapistLicenseService therapistLicenseService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Exchanges a valid refresh token for a new access token and a rotated refresh token.
     * Returns 401 if the refresh token is unknown, expired, or already used.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken is required"));
        }
        try {
            return ResponseEntity.ok(authService.refresh(request.getRefreshToken(), request.getDeviceLabel()));
        } catch (InvalidRefreshTokenException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody ProfileUpdateRequest request) {
        UUID profileId = resolveCurrentProfileId();
        return ResponseEntity.ok(authService.updateProfile(profileId, request));
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            UUID profileId = resolveCurrentProfileId();
            String url = fileStorageService.storeAvatar(file, profileId);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload avatar"));
        }
    }

    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UUID profileId = resolveCurrentProfileId();
        try {
            authService.changePassword(profileId, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/license")
    public ResponseEntity<LicenseResponse> getLicense() {
        UUID profileId = resolveCurrentProfileId();
        return ResponseEntity.ok(therapistLicenseService.getLicense(profileId));
    }

    @PostMapping("/license/renew")
    public ResponseEntity<?> renewLicense(
            @RequestParam(value = "document", required = false) MultipartFile document,
            @RequestParam(value = "licenseNumber", required = false) String licenseNumber,
            @RequestParam(value = "licenseAuthority", required = false) String licenseAuthority,
            @RequestParam(value = "licenseExpiresAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate licenseExpiresAt) {
        UUID profileId = resolveCurrentProfileId();
        try {
            LicenseResponse response = therapistLicenseService.renewLicense(
                    profileId, document, licenseNumber, licenseAuthority, licenseExpiresAt);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
            @RequestBody(required = false) RefreshRequest body) {
        // Immediately cut off the current access token via the Redis blacklist...
        String token = jwtUtils.resolveBearerToken(request.getHeader("Authorization"));
        if (token != null) {
            long expiration = jwtUtils.getExpirationDateFromToken(token).getTime();
            tokenBlacklistService.blacklistToken(token, expiration);
        }

        // ...and revoke the refresh token so the session can't be silently extended.
        if (body != null && body.getRefreshToken() != null && !body.getRefreshToken().isBlank()) {
            refreshTokenService.revoke(body.getRefreshToken());
        }

        if (token == null && (body == null || body.getRefreshToken() == null || body.getRefreshToken().isBlank())) {
            return ResponseEntity.badRequest().body("No token found");
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    private UUID resolveCurrentProfileId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Unauthorized");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal p && p.profileId() != null) {
            return p.profileId();
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Profile ID not found");
        }
    }
}


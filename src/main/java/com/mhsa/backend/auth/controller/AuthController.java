package com.mhsa.backend.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.auth.dto.AuthResponse;
import com.mhsa.backend.auth.dto.LoginRequest;
import com.mhsa.backend.auth.dto.RegisterRequest;
import com.mhsa.backend.auth.dto.UserResponse;
import com.mhsa.backend.auth.service.AuthService;
import com.mhsa.backend.auth.service.TokenBlacklistService;
import com.mhsa.backend.auth.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // 1. Lấy token từ header
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);

            // 2. Tính thời gian hết hạn (để set TTL cho Redis)
            long expiration = jwtUtils.getExpirationDateFromToken(token).getTime();

            tokenBlacklistService.blacklistToken(token, expiration);

            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.badRequest().body("No token found");
    }
}

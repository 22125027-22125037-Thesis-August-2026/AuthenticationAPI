package com.mhsa.backend.auth.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.auth.jwt.JwtUtils;
import com.mhsa.backend.contract.JwksResponse;

import lombok.RequiredArgsConstructor;

/**
 * Publishes the public half of the JWT signing key so other services can verify tokens
 * without being redeployed when the key changes.
 *
 * <p>Served under {@code /internal/**}, which {@code SecurityConfig} permits and the gateway
 * does not route — the key set is public information, but only the services on the compose
 * network need it, so there is no reason to expose it at the edge.
 */
@RestController
@RequestMapping("/internal/v1/.well-known")
@RequiredArgsConstructor
public class JwksController {

    private final JwtUtils jwtUtils;

    @GetMapping("/jwks.json")
    public ResponseEntity<JwksResponse> getJwks() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(jwtUtils.getJwksResponse());
    }
}

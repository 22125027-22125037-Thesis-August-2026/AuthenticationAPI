package com.mhsa.backend.auth.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.auth.jwt.JwtUtils;
import com.mhsa.backend.auth.service.DataAccessGrantService;
import com.mhsa.backend.contract.GrantCheckResponse;
import com.mhsa.backend.contract.JwksResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/v1")
@RequiredArgsConstructor
public class InternalController {

    private final JwtUtils jwtUtils;
    private final DataAccessGrantService dataAccessGrantService;

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<JwksResponse> getJwks() {
        JwksResponse jwks = jwtUtils.getJwksResponse();
        return ResponseEntity.ok(jwks);
    }

    @GetMapping("/grants/check")
    public ResponseEntity<GrantCheckResponse> checkGrant(
            @RequestParam UUID granter,
            @RequestParam UUID grantee,
            @RequestParam String scope) {

        boolean allowed = dataAccessGrantService.hasDelegatedAccess(granter, grantee);
        GrantCheckResponse response = new GrantCheckResponse(
            allowed,
            allowed ? "Grant is active and valid" : "No active grant found"
        );
        return ResponseEntity.ok(response);
    }
}

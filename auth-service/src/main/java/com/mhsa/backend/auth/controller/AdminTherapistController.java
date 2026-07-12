package com.mhsa.backend.auth.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.auth.dto.LicenseResponse;
import com.mhsa.backend.auth.service.TherapistLicenseService;

import lombok.RequiredArgsConstructor;

/**
 * Admin-only therapist administration. The {@code id} path variable is the therapist's
 * profile id. Guarded by {@link com.mhsa.backend.auth.security.AccessGuard#isAdmin}.
 */
@RestController
@RequestMapping("/admin/v1/therapists")
@RequiredArgsConstructor
public class AdminTherapistController {

    private final TherapistLicenseService therapistLicenseService;

    @PostMapping("/{id}/license/verify")
    @PreAuthorize("@accessGuard.isAdmin(authentication)")
    public ResponseEntity<LicenseResponse> verifyLicense(@PathVariable("id") UUID profileId) {
        return ResponseEntity.ok(therapistLicenseService.verifyLicense(profileId));
    }

    @PostMapping("/{id}/license/reject")
    @PreAuthorize("@accessGuard.isAdmin(authentication)")
    public ResponseEntity<LicenseResponse> rejectLicense(@PathVariable("id") UUID profileId) {
        return ResponseEntity.ok(therapistLicenseService.rejectLicense(profileId));
    }
}

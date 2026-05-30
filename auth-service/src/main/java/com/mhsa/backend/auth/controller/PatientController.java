package com.mhsa.backend.auth.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.auth.dto.PatientDetailResponse;
import com.mhsa.backend.auth.service.PatientDirectoryService;

import lombok.RequiredArgsConstructor;

/**
 * Therapist-facing patient directory. Access is restricted to ADMIN, the profile
 * owner, or a caller holding an ACTIVE data-access grant from the patient
 * (the same rule used for reading tracking data).
 */
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientDirectoryService patientDirectoryService;

    @GetMapping("/{profileId}")
    @PreAuthorize("@accessGuard.canReadTrackingData(authentication, #profileId)")
    public ResponseEntity<PatientDetailResponse> getPatient(@PathVariable UUID profileId) {
        return ResponseEntity.ok(patientDirectoryService.getPatientDetail(profileId));
    }
}

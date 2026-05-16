package com.mhsa.backend.tracking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.contract.ApiResponse;
import com.mhsa.backend.tracking.dto.DataAccessGrantRequest;
import com.mhsa.backend.tracking.dto.DataAccessGrantResponse;
import com.mhsa.backend.tracking.jwt.SecurityUtils;
import com.mhsa.backend.tracking.service.DataAccessGrantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tracking/grants")
@RequiredArgsConstructor
public class DataAccessGrantController {

    private final DataAccessGrantService dataAccessGrantService;

    @PostMapping
    public ResponseEntity<ApiResponse<DataAccessGrantResponse>> grantAccess(
            @Valid @RequestBody DataAccessGrantRequest request) {
        UUID granterProfileId = SecurityUtils.getCurrentProfileId();
        DataAccessGrantResponse response = dataAccessGrantService.grantAccess(granterProfileId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{granteeProfileId}")
    public ResponseEntity<ApiResponse<Void>> revokeAccess(@PathVariable UUID granteeProfileId) {
        UUID granterProfileId = SecurityUtils.getCurrentProfileId();
        dataAccessGrantService.revokeAccess(granterProfileId, granteeProfileId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DataAccessGrantResponse>>> listMyGrants() {
        UUID granterProfileId = SecurityUtils.getCurrentProfileId();
        List<DataAccessGrantResponse> grants = dataAccessGrantService.listActiveGrants(granterProfileId);
        return ResponseEntity.ok(ApiResponse.success(grants));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<DataAccessGrantResponse>>> listReceivedGrants() {
        UUID granteeProfileId = SecurityUtils.getCurrentProfileId();
        List<DataAccessGrantResponse> grants = dataAccessGrantService.listReceivedGrants(granteeProfileId);
        return ResponseEntity.ok(ApiResponse.success(grants));
    }
}

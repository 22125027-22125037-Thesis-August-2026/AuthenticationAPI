package com.mhsa.backend.auth.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mhsa.backend.auth.dto.DataAccessGrantResponse;
import com.mhsa.backend.auth.dto.GrantAccessRequest;
import com.mhsa.backend.auth.dto.GrantStatusResponse;
import com.mhsa.backend.auth.service.DataAccessGrantService;
import com.mhsa.backend.common.dto.ApiResponse;
import com.mhsa.backend.common.util.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/grants")
@RequiredArgsConstructor
public class DataAccessGrantController {

    private final DataAccessGrantService dataAccessGrantService;

    /**
     * Grant another profile access to your tracking data.
     * The caller becomes the granter automatically (resolved from JWT).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DataAccessGrantResponse>> grantAccess(
            @Valid @RequestBody GrantAccessRequest request) {
        UUID granterProfileId = SecurityUtils.getCurrentProfileId();
        DataAccessGrantResponse response = dataAccessGrantService.grantAccess(granterProfileId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Revoke a previously granted access. Immediately evicts the Redis cache entry.
     */
    @DeleteMapping("/{granteeProfileId}")
    public ResponseEntity<ApiResponse<Void>> revokeAccess(
            @PathVariable UUID granteeProfileId) {
        UUID granterProfileId = SecurityUtils.getCurrentProfileId();
        dataAccessGrantService.revokeAccess(granterProfileId, granteeProfileId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * List all ACTIVE grants issued by the given profile.
     * Only ADMIN or the profile owner may request this.
     */
    @GetMapping("/{profileId}")
    @PreAuthorize("@accessGuard.canManageGrants(authentication, #profileId)")
    public ResponseEntity<ApiResponse<List<DataAccessGrantResponse>>> listMyGrants(
            @PathVariable UUID profileId) {
        List<DataAccessGrantResponse> grants = dataAccessGrantService.listActiveGrants(profileId);
        return ResponseEntity.ok(ApiResponse.success(grants));
    }

    /**
     * List all ACTIVE grants that other profiles have given to the given profile.
     * Only ADMIN or the profile owner may request this.
     */
    @GetMapping("/{profileId}/received")
    @PreAuthorize("@accessGuard.canManageGrants(authentication, #profileId)")
    public ResponseEntity<ApiResponse<List<DataAccessGrantResponse>>> listReceivedGrants(
            @PathVariable UUID profileId) {
        List<DataAccessGrantResponse> grants = dataAccessGrantService.listReceivedGrants(profileId);
        return ResponseEntity.ok(ApiResponse.success(grants));
    }

    /**
     * Returns the bidirectional grant status between the current profile and one specific profile.
     */
    @GetMapping("/status/{otherProfileId}")
    public ResponseEntity<ApiResponse<GrantStatusResponse>> getGrantStatus(
            @PathVariable UUID otherProfileId) {
        UUID currentProfileId = SecurityUtils.getCurrentProfileId();
        GrantStatusResponse status = dataAccessGrantService.getGrantStatus(currentProfileId, otherProfileId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}

package com.mhsa.backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

import com.mhsa.backend.auth.model.AccessScope;
import com.mhsa.backend.auth.model.DataAccessGrant;
import com.mhsa.backend.auth.model.GrantStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataAccessGrantResponse {

    private UUID grantId;
    private UUID granterProfileId;
    private UUID granteeProfileId;
    private GrantStatus status;
    private AccessScope accessScope;
    private Instant grantedAt;
    private Instant expiresAt;

    public static DataAccessGrantResponse from(DataAccessGrant grant) {
        return DataAccessGrantResponse.builder()
                .grantId(grant.getGrantId())
                .granterProfileId(grant.getGranterProfileId())
                .granteeProfileId(grant.getGranteeProfileId())
                .status(grant.getStatus())
                .accessScope(grant.getAccessScope())
                .grantedAt(grant.getGrantedAt())
                .expiresAt(grant.getExpiresAt())
                .build();
    }
}

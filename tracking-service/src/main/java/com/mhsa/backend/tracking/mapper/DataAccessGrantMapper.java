package com.mhsa.backend.tracking.mapper;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mhsa.backend.tracking.dto.DataAccessGrantRequest;
import com.mhsa.backend.tracking.dto.DataAccessGrantResponse;
import com.mhsa.backend.tracking.entity.DataAccessGrant;
import com.mhsa.backend.tracking.entity.GrantStatus;

@Component
public class DataAccessGrantMapper {

    public DataAccessGrant toEntity(DataAccessGrantRequest dto) {
        if (dto == null) {
            return null;
        }

        // grant_id is an assigned key now (the entity is primarily an auth-service replica), so the
        // local-write path must mint its own id.
        return DataAccessGrant.builder()
                .grantId(UUID.randomUUID())
                .granteeProfileId(dto.getGranteeProfileId())
                .accessScope(dto.getAccessScope())
                .expiresAt(dto.getExpiresAt())
                .status(GrantStatus.ACTIVE)
                .grantedAt(Instant.now())
                .build();
    }

    public DataAccessGrantResponse toResponseDTO(DataAccessGrant entity) {
        if (entity == null) {
            return null;
        }

        return DataAccessGrantResponse.builder()
                .grantId(entity.getGrantId())
                .granterProfileId(entity.getGranterProfileId())
                .granteeProfileId(entity.getGranteeProfileId())
                .status(entity.getStatus())
                .accessScope(entity.getAccessScope())
                .grantedAt(entity.getGrantedAt())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

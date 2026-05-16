package com.mhsa.backend.tracking.mapper;

import java.time.Instant;

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

        return DataAccessGrant.builder()
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

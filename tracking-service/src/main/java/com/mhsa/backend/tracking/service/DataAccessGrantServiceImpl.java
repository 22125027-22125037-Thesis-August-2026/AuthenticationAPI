package com.mhsa.backend.tracking.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mhsa.backend.tracking.dto.DataAccessGrantRequest;
import com.mhsa.backend.tracking.dto.DataAccessGrantResponse;
import com.mhsa.backend.tracking.entity.DataAccessGrant;
import com.mhsa.backend.tracking.entity.GrantStatus;
import com.mhsa.backend.tracking.mapper.DataAccessGrantMapper;
import com.mhsa.backend.tracking.repository.DataAccessGrantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DataAccessGrantServiceImpl implements DataAccessGrantService {

    private final DataAccessGrantRepository dataAccessGrantRepository;
    private final DataAccessGrantMapper dataAccessGrantMapper;

    @Override
    @Transactional
    public DataAccessGrantResponse grantAccess(UUID granterProfileId, DataAccessGrantRequest request) {
        if (granterProfileId == null || request == null) {
            throw new IllegalArgumentException("granterProfileId and request are required");
        }

        dataAccessGrantRepository.findByGranterProfileIdAndGranteeProfileId(granterProfileId, request.getGranteeProfileId())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Grant already exists for this profile pair");
                });

        DataAccessGrant entity = dataAccessGrantMapper.toEntity(request);
        entity.setGranterProfileId(granterProfileId);
        DataAccessGrant saved = dataAccessGrantRepository.save(entity);
        log.info("Access granted from {} to {}", granterProfileId, request.getGranteeProfileId());
        return dataAccessGrantMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void revokeAccess(UUID granterProfileId, UUID granteeProfileId) {
        if (granterProfileId == null || granteeProfileId == null) {
            throw new IllegalArgumentException("granterProfileId and granteeProfileId are required");
        }

        DataAccessGrant grant = dataAccessGrantRepository.findByGranterProfileIdAndGranteeProfileId(granterProfileId, granteeProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found"));

        grant.setStatus(GrantStatus.REVOKED);
        dataAccessGrantRepository.save(grant);
        log.info("Access revoked from {} to {}", granterProfileId, granteeProfileId);
    }

    @Override
    public boolean hasDelegatedAccess(UUID targetProfileId, UUID accessorProfileId) {
        if (targetProfileId == null || accessorProfileId == null) {
            return false;
        }
        return dataAccessGrantRepository.findActiveGrant(targetProfileId, accessorProfileId, Instant.now())
                .isPresent();
    }

    @Override
    public List<DataAccessGrantResponse> listActiveGrants(UUID granterProfileId) {
        if (granterProfileId == null) {
            throw new IllegalArgumentException("granterProfileId is required");
        }

        return dataAccessGrantRepository.findByGranterProfileIdAndStatus(granterProfileId, GrantStatus.ACTIVE)
                .stream()
                .map(dataAccessGrantMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<DataAccessGrantResponse> listReceivedGrants(UUID granteeProfileId) {
        if (granteeProfileId == null) {
            throw new IllegalArgumentException("granteeProfileId is required");
        }

        return dataAccessGrantRepository.findByGranteeProfileIdAndStatus(granteeProfileId, GrantStatus.ACTIVE)
                .stream()
                .map(dataAccessGrantMapper::toResponseDTO)
                .toList();
    }
}

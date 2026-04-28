package com.mhsa.backend.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mhsa.backend.auth.dto.DataAccessGrantResponse;
import com.mhsa.backend.auth.dto.GrantAccessRequest;
import com.mhsa.backend.auth.model.AccessScope;
import com.mhsa.backend.auth.model.DataAccessGrant;
import com.mhsa.backend.auth.model.GrantStatus;
import com.mhsa.backend.auth.repository.DataAccessGrantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataAccessGrantService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    private static final String CACHE_PREFIX = "grant:";

    private final DataAccessGrantRepository grantRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public DataAccessGrantResponse grantAccess(UUID granterProfileId, GrantAccessRequest request) {
        // If a grant already exists between these two profiles, update it in place.
        DataAccessGrant grant = grantRepository
                .findByGranterProfileIdAndGranteeProfileId(granterProfileId, request.getGranteeProfileId())
                .map(existing -> {
                    existing.setStatus(GrantStatus.ACTIVE);
                    existing.setAccessScope(request.getAccessScope());
                    existing.setGrantedAt(Instant.now());
                    existing.setExpiresAt(request.getExpiresAt());
                    return existing;
                })
                .orElseGet(() -> DataAccessGrant.builder()
                        .granterProfileId(granterProfileId)
                        .granteeProfileId(request.getGranteeProfileId())
                        .status(GrantStatus.ACTIVE)
                        .accessScope(request.getAccessScope())
                        .grantedAt(Instant.now())
                        .expiresAt(request.getExpiresAt())
                        .build());

        DataAccessGrant saved = grantRepository.save(grant);

        // Invalidate any cached result so the next check hits the DB with fresh data.
        evictCache(granterProfileId, request.getGranteeProfileId());

        return DataAccessGrantResponse.from(saved);
    }

    @Transactional
    public void revokeAccess(UUID granterProfileId, UUID granteeProfileId) {
        grantRepository
                .findByGranterProfileIdAndGranteeProfileId(granterProfileId, granteeProfileId)
                .ifPresent(grant -> {
                    grant.setStatus(GrantStatus.REVOKED);
                    grantRepository.save(grant);
                });

        // Eagerly delete the cached entry so revocation takes immediate effect.
        evictCache(granterProfileId, granteeProfileId);
    }

    /**
     * Redis-first check. Caches the boolean result (including negative misses)
     * for 15 minutes to avoid DB hits on every protected request.
     */
    public boolean hasDelegatedAccess(UUID ownerProfileId, UUID viewerProfileId) {
        String cacheKey = cacheKey(ownerProfileId, viewerProfileId);

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Boolean.parseBoolean(cached);
        }

        boolean hasAccess = grantRepository
                .findActiveGrant(ownerProfileId, viewerProfileId, Instant.now())
                .isPresent();

        redisTemplate.opsForValue().set(cacheKey, String.valueOf(hasAccess), CACHE_TTL);

        return hasAccess;
    }

    public List<DataAccessGrantResponse> listActiveGrants(UUID granterProfileId) {
        return grantRepository
                .findByGranterProfileIdAndStatus(granterProfileId, GrantStatus.ACTIVE)
                .stream()
                .map(DataAccessGrantResponse::from)
                .toList();
    }

    public DataAccessGrantResponse getGrant(UUID granterProfileId, UUID granteeProfileId, AccessScope scope) {
        return grantRepository
                .findByGranterProfileIdAndGranteeProfileId(granterProfileId, granteeProfileId)
                .map(DataAccessGrantResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("No grant found between the specified profiles"));
    }

    private void evictCache(UUID ownerProfileId, UUID viewerProfileId) {
        redisTemplate.delete(cacheKey(ownerProfileId, viewerProfileId));
    }

    private static String cacheKey(UUID ownerProfileId, UUID viewerProfileId) {
        return CACHE_PREFIX + ownerProfileId + ":" + viewerProfileId;
    }
}

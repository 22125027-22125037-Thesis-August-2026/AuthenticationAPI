package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.DataAccessGrantRequest;
import com.mhsa.backend.tracking.dto.DataAccessGrantResponse;

public interface DataAccessGrantService {

    DataAccessGrantResponse grantAccess(UUID granterProfileId, DataAccessGrantRequest request);

    void revokeAccess(UUID granterProfileId, UUID granteeProfileId);

    /** Whether an ACTIVE, unexpired grant exists from target to accessor, regardless of its scope. */
    boolean hasDelegatedAccess(UUID targetProfileId, UUID accessorProfileId);

    /**
     * Whether an ACTIVE, unexpired grant from target to accessor permits the given tracking category
     * (a token such as {@code READ_SLEEP}; {@code READ_ALL} permits every category).
     */
    boolean hasScopedAccess(UUID targetProfileId, UUID accessorProfileId, String requiredScopeToken);

    List<DataAccessGrantResponse> listActiveGrants(UUID granterProfileId);

    List<DataAccessGrantResponse> listReceivedGrants(UUID granteeProfileId);
}

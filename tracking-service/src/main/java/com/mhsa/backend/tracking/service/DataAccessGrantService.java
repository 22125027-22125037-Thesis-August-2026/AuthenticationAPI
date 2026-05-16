package com.mhsa.backend.tracking.service;

import java.util.List;
import java.util.UUID;

import com.mhsa.backend.tracking.dto.DataAccessGrantRequest;
import com.mhsa.backend.tracking.dto.DataAccessGrantResponse;

public interface DataAccessGrantService {

    DataAccessGrantResponse grantAccess(UUID granterProfileId, DataAccessGrantRequest request);

    void revokeAccess(UUID granterProfileId, UUID granteeProfileId);

    boolean hasDelegatedAccess(UUID targetProfileId, UUID accessorProfileId);

    List<DataAccessGrantResponse> listActiveGrants(UUID granterProfileId);

    List<DataAccessGrantResponse> listReceivedGrants(UUID granteeProfileId);
}

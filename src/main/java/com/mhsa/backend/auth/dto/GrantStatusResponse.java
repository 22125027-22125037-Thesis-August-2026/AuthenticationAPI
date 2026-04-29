package com.mhsa.backend.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GrantStatusResponse {

    private boolean iGaveThemAccess;
    private boolean theyGaveMeAccess;
    private DataAccessGrantResponse myGrant;
    private DataAccessGrantResponse theirGrant;
}

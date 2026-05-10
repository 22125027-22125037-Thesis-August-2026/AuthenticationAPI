package com.mhsa.backend.contract;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrantCheckRequest {
    private UUID granter;
    private UUID grantee;
    private String scope;
}

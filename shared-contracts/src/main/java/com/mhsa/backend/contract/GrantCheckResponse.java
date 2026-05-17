package com.mhsa.backend.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrantCheckResponse {
    private boolean allowed;
    private String reason;
}

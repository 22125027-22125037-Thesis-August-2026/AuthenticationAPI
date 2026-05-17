package com.mhsa.backend.dashboard.dto;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private UUID profileId;
    private JsonNode auth;
    private JsonNode tracking;
    private JsonNode ai;
    private long latencyMs;
}

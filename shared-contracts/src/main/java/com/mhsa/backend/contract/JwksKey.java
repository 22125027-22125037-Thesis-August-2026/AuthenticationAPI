package com.mhsa.backend.contract;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwksKey {

    @JsonProperty("kty")
    private String keyType;

    @JsonProperty("use")
    private String use;

    @JsonProperty("kid")
    private String kid;

    @JsonProperty("alg")
    private String algorithm;

    @JsonProperty("n")
    private String modulus;

    @JsonProperty("e")
    private String exponent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JwksResponse {
        private List<JwksKey> keys;
    }
}

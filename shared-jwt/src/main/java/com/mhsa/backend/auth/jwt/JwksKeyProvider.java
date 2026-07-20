package com.mhsa.backend.auth.jwt;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mhsa.backend.contract.JwksKey;
import com.mhsa.backend.contract.JwksResponse;

import io.jsonwebtoken.JwtException;

/**
 * Resolves RS256 verification keys from Auth's published key set, cached by {@code kid}.
 *
 * <p>Two deliberate choices:
 *
 * <ul>
 *   <li><b>Fetch is lazy, not at startup.</b> A consumer boots fine while Auth is still
 *       coming up; it simply cannot validate tokens until the first fetch succeeds. Fetching
 *       in {@code @PostConstruct} would turn Auth into a hard startup dependency for every
 *       other service, which is a worse failure mode than a few early 401s.
 *   <li><b>Refresh is rate-limited.</b> An unknown kid triggers a refetch, since that is what
 *       a rotation looks like from here. Without a floor on the interval, a stream of tokens
 *       carrying forged kids would turn into a request amplifier pointed at Auth.
 * </ul>
 */
final class JwksKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(JwksKeyProvider.class);
    private static final Duration MIN_REFRESH_INTERVAL = Duration.ofSeconds(60);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final String endpoint;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private volatile Map<String, PublicKey> keysByKid = Map.of();
    private volatile Instant lastFetchAttempt = Instant.EPOCH;

    JwksKeyProvider(String endpoint, ObjectMapper objectMapper) {
        this.endpoint = endpoint;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    PublicKey resolve(String kid) {
        if (kid == null) {
            throw new JwtException("RS256 token has no kid header; cannot select a key from the JWKS");
        }

        PublicKey cached = keysByKid.get(kid);
        if (cached != null) {
            return cached;
        }

        refreshIfDue();

        PublicKey refreshed = keysByKid.get(kid);
        if (refreshed == null) {
            throw new JwtException("No published JWKS key matches kid " + kid);
        }
        return refreshed;
    }

    private synchronized void refreshIfDue() {
        // Re-check inside the lock: while this thread waited, another may have just fetched.
        if (Instant.now().isBefore(lastFetchAttempt.plus(MIN_REFRESH_INTERVAL))) {
            return;
        }
        lastFetchAttempt = Instant.now();

        try {
            keysByKid = fetch();
            log.info("Refreshed JWKS from {}: {} key(s) — {}", endpoint, keysByKid.size(), keysByKid.keySet());
        } catch (Exception e) {
            // Keep serving whatever is already cached. A transient Auth outage should not
            // invalidate keys that are still perfectly good.
            log.warn("Could not refresh JWKS from {}: {} — keeping {} cached key(s)",
                    endpoint, e.getMessage(), keysByKid.size());
        }
    }

    private Map<String, PublicKey> fetch() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("JWKS endpoint returned HTTP " + response.statusCode());
        }

        JwksResponse jwks = objectMapper.readValue(response.body(), JwksResponse.class);
        if (jwks.getKeys() == null || jwks.getKeys().isEmpty()) {
            throw new IllegalStateException("JWKS endpoint returned an empty key set");
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Map<String, PublicKey> parsed = new HashMap<>();

        for (JwksKey key : jwks.getKeys()) {
            if (!"RSA".equals(key.getKeyType()) || key.getKid() == null) {
                log.debug("Skipping unusable JWKS entry: kty={} kid={}", key.getKeyType(), key.getKid());
                continue;
            }
            // Signum 1 forces an unsigned reading, so a publisher that leaves BigInteger's
            // two's-complement sign byte on the modulus still parses correctly here.
            BigInteger modulus = new BigInteger(1, decode(key.getModulus()));
            BigInteger exponent = new BigInteger(1, decode(key.getExponent()));
            parsed.put(key.getKid(), keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent)));
        }

        if (parsed.isEmpty()) {
            throw new IllegalStateException("JWKS endpoint returned no usable RSA keys");
        }
        return Map.copyOf(parsed);
    }

    private static byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}

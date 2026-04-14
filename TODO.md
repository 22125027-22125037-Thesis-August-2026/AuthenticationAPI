# TODO - JWT Security Hardening For Distributed System

## Why this exists
Current JWT implementation is **HMAC symmetric signing (HS256)**.
This is acceptable for a monolith or tightly controlled single backend, but it becomes risky in a distributed system where many services need to verify tokens.

With HS256, any service that can verify tokens must also know the same shared secret, and therefore can also mint valid tokens if compromised.

## Current state summary
- Token signing algorithm: `HS256`
- Signing and verification key source: `mhsa.app.jwtSecret` (loaded from `JWT_SECRET`)
- Validation flow: every protected request checks signature/expiration and builds security principal from claims
- Claims used by app: `sub` (userId), `email`, `profileId`, `role`

## Risks to address
- Shared-secret sprawl: all verifiers must hold signing secret in HS256 design.
- Weak key management governance across services/environments.
- No key ID (`kid`) for rotation strategy.
- Key format ambiguity: code currently expects Base64-decoded secret bytes.

## Migration target
Adopt asymmetric JWT signing (`RS256` or `ES256`):
- Auth service holds **private key only** for signing.
- Other APIs/services hold **public key only** for verification.
- Add key rotation via `kid` + JWKS endpoint.

## Implementation tasks
1. Create dedicated auth key management policy.
- Generate asymmetric key pair per environment.
- Store private key in a secret manager/KMS.
- Expose public keys through a JWKS endpoint.

2. Update JWT issuance in auth service.
- Switch from `HS256` to `RS256` (or `ES256`).
- Add headers/claims: `kid`, `iss`, `aud`, `iat`, `exp`, `jti`, `sub`.
- Keep claims minimal (avoid sensitive PII).

3. Update verification in resource services.
- Verify signature with public key/JWKS lookup by `kid`.
- Enforce `iss`, `aud`, expiration, not-before (if used).
- Reject tokens with unknown `kid` or unsupported `alg`.

4. Add key rotation and revocation strategy.
- Support multiple active public keys during rotation windows.
- Maintain overlap period for old/new keys.
- Keep blacklist/revocation logic or move to short-lived access token + refresh token pattern.

5. Strengthen token lifecycle and standards.
- Access token short TTL (for example 5-15 minutes).
- Introduce refresh tokens (stored and revoked server-side).
- Add `jti` and replay detection where required.

6. Harden observability and failure behavior.
- Replace `System.err` token error logs with structured logging.
- Never log raw tokens/secrets.
- Add security alerts for unusual token validation failures.

7. Verify compatibility and rollout safety.
- Add integration tests for token issuance and verification across services.
- Plan phased rollout: support HS256 + RS256 temporarily, then remove HS256.
- Add migration runbook and rollback plan.

## Acceptance criteria
- Only auth service can sign tokens.
- Resource services can verify tokens without holding private signing material.
- Key rotation is possible without downtime.
- Token validation enforces issuer/audience/expiry consistently across services.

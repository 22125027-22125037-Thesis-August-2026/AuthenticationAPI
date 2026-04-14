# Authentication And JWT Security Overview

## High-level flow in this app
1. User calls `POST /api/v1/auth/login` with credentials.
2. `AuthService.login(...)` authenticates user and generates JWT.
3. API returns `AuthResponse` containing `token` and profile metadata.
4. Client sends `Authorization: Bearer <token>` for protected endpoints.
5. `JwtAuthenticationFilter` validates token and sets authenticated principal into Spring Security context.
6. Controllers/services read identity from security context for business operations.

## Where configuration comes from
- `application.properties` imports `.env` via `spring.config.import=optional:file:.env[.properties]`.
- JWT config binding:
  - `mhsa.app.jwtSecret=${JWT_SECRET}`
  - `mhsa.app.jwtExpirationMs=86400000`

So yes: the effective signing/verification key comes from `JWT_SECRET`.

## How JWT is used today
- Utility class: `JwtUtils`
- Algorithm: `HS256` (HMAC with shared secret)
- Token content:
  - `sub`: userId (UUID string)
  - `email`: user email
  - `profileId`: profile UUID
  - `role`: app role
  - `iat`, `exp`

## Important security clarification
JWT here is **signed, not encrypted**.
- The payload is Base64URL-encoded and readable by anyone who has the token.
- Signature proves integrity/authenticity (token not tampered), assuming secret/key is protected.
- There is no "decrypt JWT" step for normal JWS tokens.
- Server performs **verify + parse claims**.

## How user info is extracted
`JwtAuthenticationFilter` does:
- Read bearer token from `Authorization` header.
- Validate signature and expiration with `jwtUtils.validateJwtToken(token)`.
- Parse claims using helper methods:
  - `getUserIdFromJwtToken`
  - `getEmailFromJwtToken`
  - `getProfileIdFromJwtToken`
  - `getRoleFromJwtToken`
- Build `AuthenticatedUserPrincipal` and set it in `SecurityContextHolder`.

## Is current method wrong?
- For a single backend service: this is a common and acceptable pattern.
- For a distributed architecture with many verifiers: HS256 has a trust-boundary problem because verifiers need the same shared secret.

If your goal is centralized token issuance with broad verification by many services, use asymmetric signing (`RS256`/`ES256`) so only auth service holds private key and others use public key.

## Additional notes for this codebase
- JWT validation currently catches `JwtException` and prints to stderr; production should use structured security logging.
- Claim set includes email/role/profileId, so keep payload non-sensitive.
- There is token blacklist support for logout (Redis), which is useful for early revocation.

## Recommended architecture direction
- Keep current HS256 only for local/single-service deployments.
- Migrate to asymmetric keys for distributed services.
- Add `kid` and JWKS for key rotation and verifier discovery.

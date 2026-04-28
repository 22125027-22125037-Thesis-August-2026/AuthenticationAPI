# Backend Developer Onboarding Runbook

## 1. System Architecture & Domain Boundaries

### 🧩 Current Architecture
This repository is a Spring Boot backend application implementing several bounded domains:
- `auth` — authentication, authorization, JWT handling, user/profile persistence
- `tracking` — user activity tracking for diary entries, sleep, food, mood, media attachments, and streaks
- `dashboard` — read-side aggregation for summary data
- `ai` — chat session and message persistence plus AI integration
- `common` — shared config, DTOs, utilities, and exception primitives

> **Important:** In the current codebase, there is no `booking` package or `ScheduleSlotRepository` Java implementation present. The code focuses on Auth, Tracking, Dashboard, and AI.

### 🔒 Domain Isolation Principles
The design is intentionally modular:
- `Auth` manages user identity, roles, login/logout, JWT creation, and Redis token blacklist state.
- `Tracking` owns the domain data for user activity and stores `profile_id` as a scalar UUID.
- `Dashboard` consumes tracking data and exposes aggregated summaries.
- `AI` stores chat sessions and messages with internal relational mappings within its own domain.

The Booking domain should be strictly isolated from Auth and Tracking. That means:
- Booking should not import or depend on `auth.model.*` or `tracking.entity.*` directly.
- Cross-domain references should be plain UUIDs like `profile_id` or `account_id` rather than JPA `@ManyToOne` associations.
- This keeps the bounded contexts decoupled and avoids ORM join complexity across service boundaries.

### 🔐 Hybrid Security Model
This repository uses a hybrid security model with:
- **Stateless JWTs** for request authentication
- **Stateful Redis** for token revocation and logout support

How JWTs are issued:
- `AuthController.login()` delegates to `AuthService.login()`.
- `AuthService` calls `JwtUtils.generateToken(UUID userId, UUID profileId, String email, Role role)`.
- The generated token contains standard claims and custom claims:
  - `sub` → `userId`
  - `email` → user email
  - `profileId` → profile UUID
  - `role` → user role name
  - `iss` → `mhsa.app.jwtIssuer`
  - `aud` → `mhsa.app.jwtAudience`
  - `iat` and `exp`

#### Algorithm selection and RS256 validation
`JwtUtils.initializeKeys()` chooses the signing mode based on environment config.
This repo is configured for **RS256 only** because:
- `mhsa.app.jwtPrivateKey` is provided
- `mhsa.app.jwtPublicKey` is provided
- `JWT_ALLOW_HS256_FALLBACK=false`
- `jwtSecret` is not configured in `.env`

That means the app will not use HMAC/HS256 at runtime in this environment.
If RSA keys are missing, startup fails with `IllegalStateException` because the code requires either RSA key material or a valid HMAC secret.

##### RS256 token generation
When `AuthService.login()` issues a token, it calls `JwtUtils.generateToken(...)`.
Inside `JwtUtils`:
- A JWT builder is created with claims and metadata.
- The signed token carries:
  - `sub` → user ID
  - `email` → user email
  - `profileId` → profile UUID
  - `role` → user role
  - `iss` → issuer
  - `aud` → audience
  - `iat` and `exp`
- If RS256 is active, the builder also sets `kid` in the JWT header using `mhsa.app.jwtSigningKid`.
- It signs with the RSA private key:
  - the private key is parsed from PEM/base64 in `parsePrivateKey(...)`
  - headers are stripped, whitespace removed, and the raw base64 DER is decoded
  - `PKCS8EncodedKeySpec` is used to instantiate a `PrivateKey` via `KeyFactory.getInstance("RSA")`
- The final JWT string is produced as `header.payload.signature`, where each part is Base64URL encoded.

##### How RS256 verification works
On every protected request, `JwtAuthenticationFilter` validates the token by calling `JwtUtils.validateJwtToken(token)`.
Validation includes:
- decoding the token header and payload
- parsing the algorithm from the header
- verifying the signature using the public RSA key
- validating standard claims

Under the hood:
- `readAlgorithm(token)` decodes the first JWT segment (header) using Base64URL decode
- `parseClaims(token)` branches on the algorithm
- for `RS256`, it uses the public key built in `initializeKeys()`
- the parser verifies the token signature with `Jwts.parserBuilder().setSigningKey(publicVerificationKey).build().parseClaimsJws(token)`
- `validateKid(token)` ensures the JWT header `kid` matches `mhsa.app.jwtSigningKid`
- `validateStandardClaims(...)` enforces issuer and audience values

That means the public key is only used for verification, while the private key is only used for signing.
This is the core advantage of RSA: producers sign tokens with a private key, and consumers validate them with a public key.

#### Request authentication flow
1. The HTTP request arrives with `Authorization: Bearer <token>`.
2. `JwtAuthenticationFilter.doFilterInternal()` calls `JwtUtils.resolveBearerToken(...)`.
   - This method strips quotes and normalizes the token if necessary.
3. If the token is present and `JwtUtils.validateJwtToken(token)` returns `true`, the filter continues.
4. The filter checks `TokenBlacklistService.isBlacklisted(token)`.
   - If blacklisted, the request is rejected with `401 Unauthorized`.
5. The filter extracts claims from the JWT:
   - `userId`, `email`, `profileId`, `role`
6. An `AuthenticatedUserPrincipal` is created and stored in the `SecurityContext`.

#### Logout / revocation
- `AuthController.logout()` resolves the bearer token from the request header.
- It computes expiration milliseconds from `JwtUtils.getExpirationDateFromToken(token)`.
- `TokenBlacklistService.blacklistToken(token, expiration)` stores the token in Redis with TTL equal to remaining token lifetime.
- Redis keys use the format: `blacklist:<token>`.

This gives you:
- **stateless token validation** for every request
- **stateful revocation** to support logout and token invalidation without changing the JWT itself

#### Why this matters
- All service access is gated by `JwtAuthenticationFilter` and Spring Security.
- The app avoids HTTP sessions by using `SessionCreationPolicy.STATELESS` in `SecurityConfig`.
- User identity and authorization are derived from JWT claims, not database lookups on every call.

### 🐰 RabbitMQ Event-Driven Integration
The repo currently includes RabbitMQ in `docker-compose.yml`, but there is no Java `RabbitMQConfig` or message publisher/consumer class present yet.

If RabbitMQ is added later, the expected topology should be:
- One or more `TopicExchange` / `DirectExchange`
- Queues bound by routing key
- Jackson-based message conversion with JavaTime support

For now, the backend can still run locally with RabbitMQ available for future async integration.

---

## 2. Environment & Local Setup

### 🧪 Required Environment Variables
The app loads environment variables from `application.properties` and optionally from `.env` / `.env.properties` via `spring.config.import`.

#### Core runtime vars
- `SERVER_PORT` — backend HTTP port (default `8080`)
- `DB_URL` — JDBC URL, default `jdbc:postgresql://localhost:5433/mhsa_db`
- `DB_USERNAME` — Postgres username, default `postgres`
- `DB_PASSWORD` — Postgres password, default `123456`
- `REDIS_HOST` — Redis hostname, default `localhost`
- `REDIS_PORT` — Redis port, default `6379`

#### JWT / auth config
- `mhsa.app.jwtExpirationMs` — access token TTL in milliseconds
- `mhsa.app.jwtIssuer` — token issuer
- `mhsa.app.jwtAudience` — token audience
- `mhsa.app.jwtSigningKid` — key ID used for RSA tokens
- `mhsa.app.jwtPrivateKey` — RSA private key PEM / base64 string
- `mhsa.app.jwtPublicKey` — RSA public key PEM / base64 string
- `mhsa.app.jwtAllowHs256Fallback` — allow HS256 fallback if RSA is unavailable

#### AI config
- `gemini.api.key` — Gemini API key
- `gemini.api.url` — Gemini endpoint URL

#### Docker compose override variables
- `POSTGRES_HOST_PORT` — host port mapped to Postgres container `5432` (default `5433`)
- `DB_PASSWORD` — Postgres password for container
- `PGADMIN_HOST_PORT` — host port for pgAdmin (default `5050`)
- `RABBITMQ_AMQP_HOST_PORT` — host port for RabbitMQ AMQP (default `5672`)
- `RABBITMQ_MANAGEMENT_HOST_PORT` — host port for RabbitMQ management UI (default `15672`)
- `REDIS_HOST_PORT` — host port for Redis (default `6379`)

### 🚀 Local infrastructure commands
From repo root:

```bash
cd "d:/Y4-Sem 2 Thesis/thesis-backend"
docker compose up -d
```

Verify services:

```bash
docker compose ps
```

Start the Java backend:

```bash
./mvnw spring-boot:run
```

Or build and run the jar:

```bash
./mvnw clean package
java -jar target/backend-server-0.0.1-SNAPSHOT.jar
```

### 🌐 Access points
- Backend API: `http://localhost:8080`
- RabbitMQ Management UI: `http://localhost:15672` (guest / guest)
- pgAdmin UI: `http://localhost:5050` (admin@mhsa.com / admin)
- Postgres: `localhost:5433`, database `mhsa_db`, user `postgres`, password `123456`

---

## 3. Codebase Tour (Where things live)

### 📦 Package layout

```
src/main/java/com/mhsa/backend
  ├── auth
  │   ├── config
  │   ├── controller
  │   ├── dto
  │   ├── model
  │   ├── repository
  │   ├── security
  │   ├── service
  │   └── utils
  ├── tracking
  │   ├── controller
  │   ├── dto
  │   ├── entity
  │   ├── mapper
  │   ├── repository
  │   └── service
  ├── dashboard
  │   ├── controller
  │   ├── dto
  │   └── service
  ├── ai
  │   ├── controller
  │   ├── dto
  │   ├── entity
  │   ├── exception
  │   ├── repository
  │   ├── service
  │   └── util
  └── common
      ├── config
      ├── dto
      ├── exception
      └── util
```

### 🔧 Auth package
- `auth/controller/AuthController.java`
- `auth/service/AuthService.java`
- `auth/service/TokenBlacklistService.java`
- `auth/config/SecurityConfig.java`
- `auth/config/JwtAuthenticationFilter.java`
- `auth/utils/JwtUtils.java`
- `auth/repository/UserRepository.java`, `ProfileRepository.java`
- `auth/model/User.java`, `Profile.java`, `Role.java`, `TeenProfile.java`, `ParentProfile.java`, `TherapistProfile.java`

### 🔧 Tracking package
- `tracking/controller/*Controller.java`
- `tracking/service/*Service.java`, `*ServiceImpl.java`
- `tracking/repository/*Repository.java`
- `tracking/entity/*` hold JPA entities
- `tracking/dto/*Request.java`, `*Response.java`
- `tracking/mapper/*Mapper.java`

### 🔧 Dashboard package
- `dashboard/controller/DashboardController.java`
- `dashboard/service/DashboardService.java`
- `dashboard/dto/DashboardSummaryDto.java`

### 🔧 AI package
- `ai/controller/AiChatController.java`
- `ai/service/*Service.java`
- `ai/repository/*Repository.java`
- `ai/entity/ChatSession.java`, `ChatMessage.java`
- `ai/dto/*`
- `ai/util/AesEncryptor.java`

### 🔧 Common package
- `common/config/JacksonConfig.java`
- `common/config/UsersRoleConstraintMigration.java`
- `common/dto/ApiResponse.java`
- `common/exception/UnauthorizedException.java`
- `common/util/SecurityUtils.java`

### 🔁 Architectural pattern
The codebase follows a layered architecture with:
- controller layer for HTTP endpoints
- service layer for business logic and domain rules
- repository layer for JPA persistence
- dto layer for input/output payloads
- entity layer for JPA domain models
- shared common utilities for cross-cutting concerns

Controllers are thin, services enforce domain rules, and repositories are mostly Spring Data JPA interfaces.

---

## 4. Data Management & Concurrency (The Danger Zones)

### 🧠 Tracking data ownership
Tracking entities store `profile_id` as a plain UUID scalar field, not as a cross-domain JPA relationship.

Examples:
- `tracking/entity/DiaryEntry.java`
- `tracking/entity/FoodLog.java`
- `tracking/entity/MoodLog.java`
- `tracking/entity/SleepLog.java`
- `tracking/entity/Streak.java`
- `tracking/entity/MediaAttachment.java`

This is intentional: the tracking domain owns its own persistence and only references profiles by UUID.

### 🛡️ Why scalar UUID references?
Use scalar UUIDs for cross-domain references because:
- it avoids `@ManyToOne` coupling across bounded contexts
- it keeps JPA entity graphs simple and bounded
- it prevents accidental lazy-loading happening across modules
- it reduces the need for join-time cognitive load in shared domains

In this repo, cross-domain user identity is resolved through security claims, not entity joins.

### ⚠️ Booking / schedule concurrency note
There is no `ScheduleSlotRepository` currently in the checked-in Java sources. If you add booking support later, follow this pattern:
- persist slots with status or availability state
- avoid `SELECT` then `UPDATE` race conditions
- use a native atomic SQL update like:

```sql
UPDATE schedule_slots
SET status = 'BOOKED', updated_at = now()
WHERE slot_id = :slotId
  AND status = 'AVAILABLE'
```

This ensures only one transaction can claim a slot and prevents double-booking.

### 🧪 Current concurrency protections in repo
Existing tracking tables rely on unique indexes and owner-scoped lookups.
Examples:
- `food_logs` has `idx_food_logs_profile_entry_date` on `(profile_id, entry_date)`
- `sleep_logs` has `idx_sleep_logs_profile_entry_date` on `(profile_id, entry_date)`
- `streaks` has `uk_streak_profile_type` unique constraint on `(profile_id, streak_type)`

Service methods like `FoodLogServiceImpl.create(...)` and `SleepLogServiceImpl.create(...)` resolve by `profileId` and date to prevent duplicates.

### 📌 Appointment state machine guidance
The repo currently does not contain an appointment entity or explicit state machine class.

For future booking/appointment workflows, use a strict state progression:
- `UPCOMING` -> `IN_PROGRESS` -> `COMPLETED`
- disallow transitions that skip states (e.g. do not go from `UPCOMING` to `COMPLETED` directly)
- store state in a dedicated column like `appointment_status`
- enforce transitions in service layer or domain event handlers

---

## 5. Asynchronous Messaging (RabbitMQ)

### 📦 Current RabbitMQ status
`docker-compose.yml` provisions RabbitMQ with management UI, but the Java code currently does not contain a `RabbitMQConfig` class or active publisher/consumer wiring.

The stack is ready for future message integration, and the Docker service is configured as:
- AMQP port: `5672`
- Management UI: `15672`
- default user/password: `guest` / `guest`

### 🧷 Jackson / JavaTime support
The current app config registers Jackson modules automatically in `common/config/JacksonConfig.java`:

```java
@Bean
public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    return objectMapper;
}
```

That means timestamps such as `Instant`, `LocalDate`, `LocalDateTime`, and `ZonedDateTime` are handled automatically by Jackson.

If RabbitMQ is added later, publish/consume payloads with the same `ObjectMapper` policy so date/time fields serialize consistently.

### 🛠️ Expected RabbitMQ topology
When RabbitMQ config is added, the expected topology should include:
- exchanges (`topic` / `direct` / `fanout` depending on event shape)
- queues bound by routing keys
- durable queues for replay/resilience
- bindings to route domain events to interested services

Example future topology:
- `mhsa.exchange.events`
- `mhsa.booking.queue`
- `mhsa.tracking.queue`
- `mhsa.auth.queue`

---

## 6. Developer Standard Operating Procedure (How to add a feature)

### ✅ Feature addition workflow
1. **Design the API contract first**
   - Define request/response DTOs in the appropriate package, e.g. `tracking/dto`.
2. **Add the controller endpoint**
   - Add a method in the relevant controller class.
   - Keep controllers thin and delegate to services.
3. **Write the service logic**
   - Add an interface method if needed.
   - Implement business rules in `*ServiceImpl.java`.
4. **Add repository access**
   - Define repository methods in `tracking/repository` or `auth/repository`.
   - Prefer derived query methods with `findByProfileId...` and explicit ownership checks.
5. **Use the security context**
   - Retrieve the authenticated profile in services with `SecurityUtils.getCurrentProfileId()`.
6. **Throw domain exceptions, not generic errors**
   - Use `IllegalArgumentException`, `UnauthorizedException`, or create a domain-specific exception class.
7. **Wire validation and request constraints**
   - Annotate DTO fields with `jakarta.validation` constraints.
8. **Test the behavior**
   - Add service/controller tests where appropriate.

### 🧪 Example pattern
For new tracking API features, the flow should look like:
- `tracking/dto/NewThingRequest.java`
- `tracking/dto/NewThingResponse.java`
- `tracking/controller/NewThingController.java`
- `tracking/service/NewThingService.java`
- `tracking/service/NewThingServiceImpl.java`
- `tracking/repository/NewThingRepository.java`
- `tracking/entity/NewThing.java`

### 🎯 Good code style rules in this repo
- Keep controllers focused on HTTP mapping and response return values.
- Keep service methods responsible for validation, security checks, mapping, and repository orchestration.
- Avoid adding business logic into repositories.
- Prefer `Optional` lookups + `orElseThrow` for ownership checks.
- Use `UUID` for IDs consistently.

---

## 7. Troubleshooting & Debugging

### 🔍 Where to look first
- `auth/config/JwtAuthenticationFilter.java` — JWT extraction, validation, and token blacklist checks
- `auth/utils/JwtUtils.java` — token parsing, issuer/audience validation, algorithm logic, RSA/HS fallback
- `auth/service/TokenBlacklistService.java` — Redis-based token revocation and TTL handling
- `common/config/JacksonConfig.java` — Jackson module registration for Java time serialization
- `common/util/SecurityUtils.java` — how the authenticated `profileId` and `role` are resolved from `SecurityContext`
- `tracking/service/*ServiceImpl.java` — domain rules and owner-scoped record selection

### 🧯 Exceptions and error handling
There is not currently a centralized `@ControllerAdvice` or `GlobalExceptionHandler` class in this repo.

If one is added, it should probably:
- catch `Exception.class`
- convert runtime exceptions to `ApiResponse.error(...)`
- return consistent JSON shape for clients
- map `UnauthorizedException` to `401`
- map `IllegalArgumentException` to `400`

For now, Spring Boot’s default error handling is active.

### 📜 Reading logs
The code uses SLF4J via `LoggerFactory` in classes such as `JwtUtils`.
- Look for `WARN` lines during JWT validation failures.
- Look for `INFO` lines on JWT signing mode selection.

### 🧰 Common failure modes
- `No JWT signing material configured` — missing `mhsa.app.jwtPrivateKey`, `mhsa.app.jwtPublicKey`, or `mhsa.app.jwtSecret`
- `Token has been revoked` — Redis blacklist contains the JWT
- `Invalid authenticated profile id` — security principal is malformed
- Postgres connection issues — ensure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and Docker Postgres container are aligned

---

## Appendix: Current repo realities
- The current source tree does not include a Java booking domain or schedule-slot persistence layer.
- RabbitMQ is provisioned via Docker, but no Java RabbitMQ configuration class exists yet.
- There is a ready-to-use `ApiResponse` wrapper in `common/dto` for future global error handling.

Keep this document as the single-source onboarding reference for the current backend implementation.

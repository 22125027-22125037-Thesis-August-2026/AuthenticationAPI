# Next Session Prompt - Continue Phase 1 Testing → Phase 2

## Current Status (2026-05-10)

**Phase 1:** Auth Service extraction - CODE COMPLETE ✅, DOCKER READY ✅, TESTING PENDING ⏳

**Latest commits:**
```
f2c2b16 feat(phase-1): complete docker setup for auth-service testing
ae1f968 feat(phase-1): create auth-service microservice with independent database and S3 storage
```

---

## Immediate Task: Fix Phase 1 Testing

### Problem
Auth-service Docker container fails to start because JWT private/public keys in `.env` file have literal `\n` characters (escaped newlines) instead of actual newlines. When Docker passes them as environment variables to Java system properties, the JJWT library tries to base64-decode the backslash-n literally and fails.

**Error message:**
```
io.jsonwebtoken.io.DecodingException: Illegal base64 character: '\'
```

### Solution (Choose 1 - all take ~5 minutes)

#### Option A (Simplest): Create properties file
1. In `auth-service/src/main/resources/`, create `jwt-keys.properties`:
   ```properties
   mhsa.app.jwtPrivateKey=-----BEGIN PRIVATE KEY-----\n[rest of key with real newlines]
   ```
2. Load via Spring: add to `application-docker.properties`:
   ```
   spring.config.import=file:/app/jwt-keys.properties
   ```
3. Copy into Docker: update `auth-service/Dockerfile` to `COPY jwt-keys.properties /app/`

#### Option B: Fix entrypoint.sh
Update `auth-service/entrypoint.sh` to unescape `\n` before passing to Java:
```bash
#!/bin/sh
JWT_PRIVATE_KEY=$(echo "$JWT_PRIVATE_KEY" | sed 's/\\n/\n/g')
JWT_PUBLIC_KEY=$(echo "$JWT_PUBLIC_KEY" | sed 's/\\n/\n/g')
exec java -Dspring.profiles.active=docker \
  -Dmhsa.app.jwtPrivateKey="$JWT_PRIVATE_KEY" \
  -Dmhsa.app.jwtPublicKey="$JWT_PUBLIC_KEY" \
  -jar app.jar
```

#### Option C: Use environment file in Docker
Update `docker-compose.yml` `auth-service` section:
```yaml
env_file:
  - auth-service/.env.docker  # create this with keys as multiline PEM
```

### After Fixing JWT Keys (estimated 2 hours total):

1. **Start services**: `docker-compose up -d --build`
2. **Verify auth-service boots**: `docker-compose logs auth-service | grep "Started AuthServiceApplication"`
3. **Test 5 endpoints** (Postman or curl):
   - `POST /api/v1/auth/register` → JWT token
   - `POST /api/v1/auth/login` → JWT token
   - `GET /internal/v1/.well-known/jwks.json` → JWKS response
   - `GET /internal/v1/grants/check?granter=UUID&grantee=UUID` → grant status
   - `POST /api/v1/auth/profile/avatar` (multipart file) → S3 presigned URL

4. **Document findings** in memory: what worked, any edge cases

---

## Then: Start Phase 2 - AI Service (3-4 days)

**Phase 2 Goals:**
- Extract AI chat module into `ai-service` (port 8082, ai_db)
- Refactor `ContextAggregatorService` to call tracking-service via REST (initially monolith)
- Implement JWKS validation for incoming JWTs
- Subscribe to RabbitMQ `auth.grant.*` events for cache invalidation (setup placeholder)

**Key files to extract:**
- `app/src/main/java/com/mhsa/backend/ai/` → `ai-service/src/main/java/com/mhsa/backend/ai/`
- `app/src/main/resources/db/migration/V2__*.sql` → `ai-service/src/main/resources/db/migration/V1__*.sql` (AI tables only)

**Architecture decision already made:**
- AI calls Tracking for context: `GET http://tracking-service:8083/internal/v1/tracking/context/{profileId}?days=7`
- In Phase 2: points to monolith `GET http://backend-app:8080/internal/v1/tracking/context/...`
- In Phase 3: automatically points to tracking-service (DNS update only)

---

## Context for This Work

**Repository structure:**
```
thesis-backend/
├── app/                    # Monolith (tracking, ai, dashboard remain here)
├── auth-service/           # Phase 1 - EXTRACTED ✅
├── ai-service/            # Phase 2 - TODO (start next)
├── tracking-service/      # Phase 3 - TODO
├── dashboard-service/     # Phase 4 - TODO
├── shared-jwt/            # Reusable JWT library (all services use)
├── shared-contracts/      # Cross-service DTOs
├── docker-compose.yml     # Orchestration
└── pom.xml               # Maven aggregator
```

**Microservices pattern established:**
- Each service has own PostgreSQL database
- Services validate JWT independently (JWKS from auth-service)
- Cross-service calls via REST (`http://service-name:port/internal/v1/...`)
- Async events via RabbitMQ (placeholder setup in Phase 2)
- File storage: S3-compatible (MinIO dev, AWS S3 prod)

**Memory notes available:**
- `memory/MEMORY.md` — index of project context
- `memory/phase-1-auth-service.md` — detailed Phase 1 status and JWT key workaround

---

## Commands to Start Next Session

```bash
# 1. Navigate to repo
cd d:\StudyFiles\Thesis\thesis-backend

# 2. Check current state
git log --oneline -3
docker-compose ps

# 3. Implement JWT key fix (choose Option A/B/C above)

# 4. Rebuild and test
docker-compose down
docker-compose up -d --build
docker-compose logs auth-service

# 5. Test endpoints once service is healthy
curl http://localhost:8081/internal/v1/.well-known/jwks.json
```

---

## Passing to Claude in Next Session

**Use this exact prompt:**

> Continue Phase 1 testing and then start Phase 2 - AI Service extraction
> 
> **Status:** Auth-service code ✅, Docker infrastructure ✅, JWT key env var issue ⏳ (workaround documented in NEXT_SESSION_PROMPT.md)
> 
> **Immediate task:** Fix JWT key format (Option A/B/C - all ~5 min), start docker-compose, test 5 endpoints
> 
> **Then:** Start Phase 2 - extract AI service, refactor ContextAggregatorService to call tracking REST API
> 
> See NEXT_SESSION_PROMPT.md for detailed context and workarounds.

---

**Estimated time:**
- Fix JWT keys + test Phase 1: 1-2 hours
- Phase 2 extraction + testing: 3-4 days
- Total to Phase 2 complete: ~1 week

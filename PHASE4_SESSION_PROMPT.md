# Phase 4 Session Prompt: Advanced Features & Integration

**Status:** Ready to start  
**Previous Session:** 2026-05-14 - Phase 3 complete (tracking-service extracted)  
**Estimated Duration:** 6-8 hours

---

## What is Phase 4?

Phase 4 focuses on **enhancing the microservices architecture** with advanced features, improved cross-service integration, and preparing for production deployment. This phase builds on the solid foundation of Phase 3 (3 independent microservices with separate databases).

## Current State (End of Phase 3)

✅ **Architecture Complete:**
- auth-service (8081, auth_db) - ✅ Production-ready
- ai-service (8082, ai_db) - ✅ Functional, calls tracking-service
- tracking-service (8083, tracking_db) - ✅ Newly extracted, fully operational

✅ **Database Setup:**
- 3 separate PostgreSQL instances with Flyway migrations
- No cross-database foreign keys (distributed architecture)
- All 50+ tables migrated and accessible

✅ **Security:**
- Shared JWT library across all services
- Owner-based access control (self-access only)
- JWT validation via JWKS

---

## Phase 4 Goals (Priority Order)

### Goal 1: Data Sharing & Delegation (High Priority)
**Current State:** AccessGuard only allows self-access  
**Target:** Support data sharing between users (e.g., therapist viewing client data)

**Tasks:**
1. Extend AccessGuard bean with delegation support
   - `canReadTrackingData(Auth, profileId, grantor)` - Check if user has explicit grant
   - Store grants in tracking_db: table `data_access_grants` (grantor_id, grantee_id, grant_type, created_at)
   - Query grants before denying access

2. Add grant management endpoints in tracking-service
   - `POST /api/v1/tracking/grants` - Create access grant
   - `GET /api/v1/tracking/grants` - List grants for user
   - `DELETE /api/v1/tracking/grants/{grantId}` - Revoke grant

3. Publish grant events to RabbitMQ
   - `tracking.grant.created` - When user grants access
   - `tracking.grant.revoked` - When user revokes access

4. Update AuthEventListener
   - Listen for `auth.user.deleted` → delete all grants involving that user
   - Listen for `auth.grant.created` → create corresponding tracking grant

**Estimated Effort:** 4 hours

### Goal 2: Full RabbitMQ Event Processing (High Priority)
**Current State:** Event publishers created, listeners have TODO comments  
**Target:** Fully functional async event processing

**Tasks:**
1. Complete AuthEventListener in tracking-service
   - `onUserDeleted()` - Delete all tracking data for user
   - `onUserUpdated()` - Invalidate cached context
   - `onGrantCreated()` - Process new access grants

2. Complete auth-service event listeners (if needed)
   - Listen for `tracking.grant.created` → Update auth service
   - Listen for `tracking.*` events for audit logging

3. Implement event processors with proper error handling
   - Retry logic for failed message processing
   - Dead-letter queue for permanent failures
   - Logging for all event processing

4. Add integration tests
   - Test event publishing from services
   - Test event consumption and side effects
   - Verify message format and deserialization

**Estimated Effort:** 3 hours

### Goal 3: Context Aggregation Caching (Medium Priority)
**Current State:** Context endpoint queries DB every time  
**Target:** Cache results in Redis for performance

**Tasks:**
1. Update ContextAggregatorService
   - Check Redis cache first (key: `context:{profileId}:{days}`)
   - Query DB if cache miss
   - Cache results with TTL (30 minutes default)

2. Invalidate cache on data changes
   - DiaryEntryServiceImpl.save() → invalidate context cache
   - SleepLogServiceImpl.save() → invalidate context cache
   - FoodLogServiceImpl.save() → invalidate context cache
   - MoodLogServiceImpl.save() → invalidate context cache

3. Add cache configuration
   - Spring Cache annotation support
   - Configure Redis as cache backend
   - TTL policies based on data type

4. Monitor cache hit rates
   - Log cache hits/misses for tracking
   - Adjust TTL based on usage patterns

**Estimated Effort:** 2 hours

### Goal 4: Metrics & Observability (Medium Priority)
**Current State:** Basic Spring Actuator endpoints  
**Target:** Rich metrics for monitoring

**Tasks:**
1. Add Micrometer metrics
   - Custom metrics: tracking events published, context requests, cache hit ratio
   - Expose on `/actuator/metrics`

2. Update health endpoints
   - Dependency health checks (DB, Redis, RabbitMQ)
   - Custom health indicators for each service

3. Add request/response logging
   - HTTP request/response interceptors
   - Log payload size, latency, error rates

4. Configure for Prometheus/Grafana (if available)
   - Expose metrics in Prometheus format
   - Document dashboard setup

**Estimated Effort:** 2 hours

### Goal 5: Integration Testing (Medium Priority)
**Current State:** No integration tests  
**Target:** Comprehensive test suite

**Tasks:**
1. Create integration tests for tracking-service
   - Test context endpoint with sample data
   - Test CRUD operations with JWT auth
   - Test authorization (owner vs. shared data)

2. Test cross-service communication
   - AI service → Tracking service context call
   - Auth service → Tracking service event processing
   - Verify service discovery (docker-compose networking)

3. Test database migrations
   - Verify all tables created
   - Check schema consistency across services

4. Performance tests
   - Context aggregation latency
   - Cache effectiveness

**Estimated Effort:** 3 hours

### Goal 6: Documentation & Deployment Guides (Low Priority)
**Current State:** PHASE3_COMPLETION.md exists  
**Target:** Production-ready documentation

**Tasks:**
1. Create deployment guide
   - Docker setup instructions
   - Environment variable reference
   - Database initialization procedures

2. Create troubleshooting guide
   - Common issues and solutions
   - Debug logging configuration
   - Health check interpretation

3. Update API documentation
   - Swagger/OpenAPI specs for each service
   - Example requests/responses
   - Error code reference

4. Architecture decision records (ADRs)
   - Why 3 separate databases
   - Why owner-based access control
   - Why RabbitMQ for events

**Estimated Effort:** 2 hours

---

## Recommended Priority & Sequence

### High Priority (Do First)
1. **Goal 1: Data Sharing** - Many use cases depend on this
2. **Goal 2: RabbitMQ Events** - Foundation for async operations

### Medium Priority (Parallel)
3. **Goal 4: Metrics** - Can start while Goals 1-2 in progress
4. **Goal 5: Tests** - Test Goals 1-2 implementations

### Lower Priority (Polish)
5. **Goal 3: Caching** - Performance optimization
6. **Goal 6: Documentation** - Last but important

---

## Key Files to Modify/Create

### Core Implementation Files
```
tracking-service/
├── src/main/java/com/mhsa/backend/tracking/
│   ├── entity/DataAccessGrant.java ← NEW
│   ├── repository/DataAccessGrantRepository.java ← NEW
│   ├── controller/GrantController.java ← NEW
│   ├── service/GrantService.java ← NEW (interface + impl)
│   ├── security/AccessGuard.java ← MODIFY
│   ├── messaging/AuthEventListener.java ← MODIFY (implement TODOs)
│   ├── service/ContextAggregatorService.java ← MODIFY (add caching)
│   └── config/CacheConfig.java ← NEW
├── src/main/resources/
│   └── db/migration/
│       └── V2__add_data_access_grants_table.sql ← NEW
└── src/test/java/
    └── com/mhsa/backend/tracking/
        ├── integration/ContextControllerIntegrationTest.java ← NEW
        ├── integration/GrantIntegrationTest.java ← NEW
        └── messaging/AuthEventListenerTest.java ← NEW

auth-service/
├── src/main/java/com/mhsa/backend/auth/
│   └── messaging/TrackingEventListener.java ← NEW (optional)
└── src/main/resources/
    └── db/migration/
        └── V2__add_grant_audit_table.sql ← NEW (optional)
```

### Configuration Files
```
tracking-service/src/main/resources/
├── application-docker.properties ← MODIFY (add cache settings)
└── application-dev.properties ← NEW (local dev config)
```

### Testing Files
```
tracking-service/src/test/
├── java/.../
│   ├── ContextControllerIntegrationTest.java
│   ├── AccessGuardTests.java
│   ├── RabbitMQEventTest.java
│   └── CacheTests.java
└── resources/
    ├── application-test.properties
    └── test-data.sql
```

---

## Testing Checklist for Phase 4

- [ ] Grant creation endpoint works
- [ ] Grant revocation works
- [ ] SharedAccessGuard properly checks grants
- [ ] User A can see User B's data only if granted access
- [ ] Auth event listener receives and processes events
- [ ] Tracking event listeners work (when implemented in auth-service)
- [ ] Context endpoint returns cached results on second call
- [ ] Cache invalidates after data modifications
- [ ] Context endpoint still works without cache (fallback)
- [ ] All integration tests pass
- [ ] Services start up correctly with new tables
- [ ] No circular dependencies between services

---

## Docker & Deployment Notes

### Current Docker Setup (Phase 3)
```yaml
services:
  postgres-auth, postgres-ai, postgres-tracking
  redis, rabbitmq (if available)
  auth-service, ai-service, tracking-service
  (monolith backend-app as fallback)
```

### Phase 4 Changes
- Add V2 migrations to Flyway scripts
- Update docker-compose to include new volumes if needed
- Ensure RabbitMQ is running (may need to add to docker-compose if not present)
- Add Redis for caching (already present)

### Deployment Steps
1. `mvn clean package -DskipTests`
2. `docker-compose down -v` (clean DB if migration issues)
3. `docker-compose up -d`
4. Verify migrations via `SELECT * FROM schema_version WHERE version >= '2'`
5. Test endpoints

---

## Session Notes for Next Developer

- **Current Branch:** `migrate/microservice`
- **Main Branch:** `main`
- **Last Commit:** 6a525be (Phase 3 completion docs)
- **Build Status:** ✅ All 6 modules compile (mvnw clean package -DskipTests)
- **Services Running:** Test with `docker-compose up -d` then curl endpoints
- **JWT Testing:** Get token from auth-service login, use in other service calls

---

## Useful Command Reference

```bash
# Build & test
mvn clean package -DskipTests
mvn clean test

# Docker
docker-compose up -d
docker-compose logs -f tracking-service
docker-compose down

# Git
git log --oneline | head -5
git diff HEAD~5 HEAD tracking-service/pom.xml

# Database
psql -h localhost -p 5434 -U postgres -d tracking_db
SELECT * FROM schema_version;

# Test endpoints
curl http://localhost:8083/actuator/health
curl http://localhost:8083/internal/v1/tracking/context/{uuid}?days=7
```

---

## Optional: Future Phases (Phase 5+)

After Phase 4, consider:
- **Phase 5:** Analytics & reporting endpoints
- **Phase 6:** User-facing dashboard APIs
- **Phase 7:** Remove tracking module from monolith (full cutover)
- **Phase 8:** API gateway / service mesh
- **Phase 9:** Observability platform integration

---

## How to Start This Session

1. Read this prompt end-to-end
2. Run: `mvn clean package -DskipTests` to verify build state
3. Choose highest priority goal (recommend Goal 1: Data Sharing)
4. Create implementation plan using EnterPlanMode
5. Start with the first high-priority task
6. Test incrementally and commit frequently
7. Update PHASE4_COMPLETION.md as you complete goals
8. Push to branch when ready for review

**Questions?** Check PHASE3_COMPLETION.md or git log for context on previous decisions.

Good luck! 🚀

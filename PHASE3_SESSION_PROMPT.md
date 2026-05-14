# Next Session Prompt - Phase 3: Tracking Service Extraction

## Current Status (2026-05-14)

**Phase 1:** ✅ COMPLETE - Auth-service fully operational
- JWT generation, validation, JWKS endpoint
- All 5 endpoints tested and working

**Phase 2:** ✅ COMPLETE - AI Service Extraction successful
- ai-service running on port 8082
- Separate ai_db database
- JWT validation working
- REST calls to tracking-service (via monolith)
- RabbitMQ event listeners (placeholders)

**Latest Commits:**
```
16f44f5 docs: add comprehensive Phase 2 completion guide for next session
985b12e feat(phase-2): complete AI service extraction from monolith
```

**Docker Services Running:**
- postgres-auth:5432 ✅
- postgres-ai:5433 ✅
- redis:6379 ✅
- minio:9000 ✅
- auth-service:8081 ✅
- ai-service:8082 ✅

---

## Phase 3 Task: Extract Tracking Service (2-3 days estimate)

### Architecture Overview

```
thesis-backend/
├── app/                         # Monolith (dashboard remains)
├── auth-service/                # Phase 1 ✅
├── ai-service/                  # Phase 2 ✅
├── tracking-service/            # Phase 3 → YOU ARE HERE
│   ├── src/main/java/com/mhsa/backend/tracking/
│   ├── src/main/resources/db/migration/
│   ├── pom.xml
│   ├── Dockerfile
│   └── Dockerfile.build
├── shared-jwt/                  # Reusable
├── shared-contracts/            # Reusable
├── docker-compose.yml           # Add tracking-service + postgres-tracking
└── Dockerfile.build             # Multi-stage build
```

### Key Differences from AI Service

**AI Service:** Consumes data (calls other services)  
**Tracking Service:** Provides data (called by other services)

This means tracking-service needs to expose REST endpoints that other services will call.

---

## Step-by-Step Implementation

### Step 1: Create tracking-service Module Structure (20 min)

```bash
# Create directories
mkdir -p tracking-service/src/main/java/com/mhsa/backend/tracking
mkdir -p tracking-service/src/main/resources/db/migration
mkdir -p tracking-service/src/test/java

# Structure needed:
# ├── src/main/java/com/mhsa/backend/tracking/
# │   ├── controller/         # REST endpoints
# │   ├── service/            # Business logic
# │   ├── repository/         # Data access
# │   ├── entity/             # JPA entities
# │   ├── dto/                # Request/response objects
# │   └── config/             # Spring configuration
# ├── src/main/resources/
# │   ├── db/migration/       # Flyway migrations
# │   └── application-docker.properties
# ├── pom.xml                 # Dependencies
# ├── Dockerfile              # Container
# └── entrypoint.sh           # Startup script
```

**Create pom.xml** (similar to ai-service but without RabbitMQ client dependencies for now):
- spring-boot-starter-web
- spring-boot-starter-jpa
- postgresql driver
- spring-data-redis
- shared-jwt (for JWT validation)
- shared-contracts
- Flyway for migrations

### Step 2: Extract Tracking Code from Monolith (2-3 hours)

**Source:** `app/src/main/java/com/mhsa/backend/tracking/`

**Classes to Extract:**
- Controllers:
  - `TrackingController` - Main API endpoints (GET /internal/v1/tracking/context/{profileId})
  - `DiaryEntryController` - Diary endpoints
  - `SleepLogController` - Sleep tracking endpoints
  - `FoodLogController` - Food/nutrition endpoints
  - `MoodLogController` - Mood tracking endpoints

- Services:
  - `TrackingService` - Aggregates tracking data
  - `DiaryEntryService` - Diary business logic
  - `SleepLogService` - Sleep analysis
  - `FoodLogService` - Food tracking
  - `MoodLogService` - Mood tracking

- Repositories:
  - `DiaryEntryRepository`
  - `SleepLogRepository`
  - `FoodLogRepository`
  - `MoodLogRepository`
  - `StreakRepository`

- Entities:
  - `DiaryEntry`
  - `SleepLog`
  - `FoodLog`
  - `MoodLog`
  - `Streak`

- DTOs:
  - `DiaryEntryDto`
  - `SleepLogDto`
  - `FoodLogDto`
  - `MoodLogDto`
  - `ContextDataDto` - Response object for context aggregation

**Package names remain:** `com.mhsa.backend.tracking.*`

### Step 3: Create Tracking-Only Database Schema (1 hour)

**Create:** `tracking-service/src/main/resources/db/migration/V1__create_tracking_tables.sql`

**Extract from:** `app/src/main/resources/db/migration/V1__create_tables.sql` (only tracking tables)

**Tables needed:**
```sql
diary_entries (diary_entry_id, profile_id, title, content, mood_tag, positivity_score, entry_date, created_at, updated_at)
food_logs (food_id, profile_id, water_glasses, food_description, satiety_level, entry_date, created_at)
mood_logs (mood_log_id, profile_id, mood_score, note, logged_at, created_at, updated_at)
sleep_logs (sleep_log_id, profile_id, sleep_start_at, sleep_end_at, duration_minutes, sleep_quality, note, created_at, updated_at)
streaks (streak_id, profile_id, streak_type, current_count, last_activity_date, created_at, updated_at)
media_attachments (media_attachment_id, profile_id, diary_entry_id, food_log_id, file_url, file_name, media_type, mime_type, file_size_bytes, created_at)
```

**Important:** Do NOT include users, profiles, or other non-tracking tables

### Step 4: Create REST API for Context Aggregation (1-2 hours)

**Create:** `TrackingController` with endpoint that replaces the call in ContextAggregatorService

**New Endpoint:**
```java
@GetMapping("/internal/v1/tracking/context/{profileId}")
@RequestParam(name = "days", defaultValue = "7") int days
public ResponseEntity<String> getUserContextSummary(@PathVariable UUID profileId, @RequestParam int days)
```

This endpoint should:
1. Accept profileId and days as parameters
2. Query sleep logs from last N days
3. Query diary entries from last N days
4. Query food logs from last N days
5. Calculate statistics (average sleep, dominant emotions, water intake, skipped meals)
6. Return formatted context string (matching the format from ai-service ContextAggregatorService)

**Response Format:**
```
[USER CONTEXT - LAST 7 DAYS]
- Sleep: Averaging X.X hours. Note: Y days of "Poor" sleep.
- Mood: Dominant emotion is EMOTION1 and EMOTION2.
- Diet: Skipped breakfast Z times.
- Hydration: W days below 6 glasses.
```

### Step 5: Refactor ContextAggregatorService in AI Service (30 min)

**Update:** `ai-service/src/main/java/com/mhsa/backend/ai/service/ContextAggregatorService.java`

**Change URL from:**
```java
service.tracking.url=http://backend-app:8080
String url = trackingServiceUrl + "/internal/v1/tracking/context/{profileId}?days=7";
```

**To:**
```java
service.tracking.url=http://tracking-service:8083
String url = trackingServiceUrl + "/internal/v1/tracking/context/{profileId}?days=7";
```

Just update the URL - no code logic changes needed!

### Step 6: Implement JWT Security (1 hour)

**Use the same pattern as ai-service:**
- Copy `SecurityConfig.java` from ai-service (with port adjusted to 8083)
- Copy `AiServiceUserDetailsService.java` and rename to `TrackingServiceUserDetailsService.java`
- Copy `ApplicationConfig.java` (RestTemplate, ObjectMapper beans)

**Security Rules:**
```java
.requestMatchers("/internal/v1/**").permitAll()  // Service-to-service
.requestMatchers("/api/v1/tracking/**").authenticated()  // Client APIs (if any)
.anyRequest().authenticated()
```

### Step 7: Setup RabbitMQ Event Publishing (1 hour)

**Create:** `TrackingEventPublisher` - Publishes tracking-related events

**Events to publish (when data is added/updated):**
- `tracking.diary.created` - When diary entry is created
- `tracking.sleep.logged` - When sleep data is logged
- `tracking.mood.recorded` - When mood is recorded
- `tracking.food.logged` - When food is logged

**Note:** Make this asynchronous so the REST endpoints don't block waiting for event publishing.

### Step 8: Configuration Files (30 min)

**Create:** `tracking-service/src/main/resources/application-docker.properties`

```properties
# Server
server.port=8083
spring.application.name=tracking-service

# Database (separate from auth and AI)
spring.datasource.url=jdbc:postgresql://postgres-tracking:5432/tracking_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baselineOnMigrate=true

# Redis
spring.data.redis.host=redis
spring.data.redis.port=6379

# RabbitMQ
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# JWT
mhsa.app.jwtExpirationMs=3600000
mhsa.app.jwtIssuer=mhsa.backend
mhsa.app.jwtAudience=mhsa-api

# Service URLs
service.auth.url=http://auth-service:8081

# Logging
logging.level.root=INFO
logging.level.com.mhsa.backend=DEBUG
```

**Create:** `tracking-service/Dockerfile`

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/tracking-service.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["/app/entrypoint.sh"]
```

**Create:** `tracking-service/entrypoint.sh`

```bash
#!/bin/sh
exec java -Dspring.profiles.active=docker -jar /app/app.jar
```

### Step 9: Update docker-compose.yml (30 min)

**Add postgres-tracking service:**
```yaml
postgres-tracking:
  image: postgres:16-alpine
  container_name: postgres-tracking
  environment:
    POSTGRES_DB: tracking_db
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
  ports:
    - "5434:5432"  # Different from other databases
  volumes:
    - postgres-tracking-data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U postgres"]
    interval: 10s
    timeout: 5s
    retries: 5
  networks:
    - mhsa-network
```

**Add tracking-service:**
```yaml
tracking-service:
  build:
    context: .
    dockerfile: tracking-service/Dockerfile.build
  container_name: tracking-service
  environment:
    SERVER_PORT: 8083
    SPRING_APPLICATION_NAME: tracking-service
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-tracking:5432/tracking_db
    SPRING_DATASOURCE_USERNAME: postgres
    SPRING_DATASOURCE_PASSWORD: postgres
    # ... other env vars
  ports:
    - "8083:8083"
  depends_on:
    postgres-tracking:
      condition: service_healthy
    redis:
      condition: service_healthy
    auth-service:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 5
    start_period: 60s
  networks:
    - mhsa-network
```

**Add volume:**
```yaml
volumes:
  postgres-tracking-data:
```

### Step 10: Update Root pom.xml (15 min)

**Add to `<modules>`:**
```xml
<module>tracking-service</module>
```

**Create:** `tracking-service/Dockerfile.build`

(Same pattern as ai-service - multi-stage Maven build, copy tracking-service JAR)

### Step 11: Update AI Service (10 min)

**File:** `ai-service/src/main/resources/application-docker.properties`

**Change:**
```properties
# Old
service.tracking.url=http://backend-app:8080

# New
service.tracking.url=http://tracking-service:8083
```

---

## Testing Plan for Phase 3

After extracting tracking service, test:

1. **Build:** `mvn clean package -DskipTests`
2. **Services:** `docker-compose up -d`
3. **Health Check:** `GET http://localhost:8083/actuator/health` → 200 OK
4. **Database Migration:** Verify tracking_db created with all tables
5. **Context Endpoint:** `GET http://localhost:8083/internal/v1/tracking/context/{profileId}?days=7` → Valid JSON
6. **AI Integration:** Send chat message in ai-service → should call tracking-service
7. **Event Publishing:** Check logs for tracking events (diary.created, sleep.logged, etc.)
8. **Authentication:** Endpoints require valid JWT from auth-service

---

## Common Pitfalls to Avoid

❌ **Don't:** Include user tables in tracking_db  
✅ **Do:** Keep user references as UUID only

❌ **Don't:** Hard-code service URLs  
✅ **Do:** Use environment variables (configured in docker-compose)

❌ **Don't:** Forget to update ContextAggregatorService URL  
✅ **Do:** Change service.tracking.url in ai-service config

❌ **Don't:** Copy entire monolith - extract only tracking code  
✅ **Do:** Keep clean separation of concerns

❌ **Don't:** Skip database migration baseline  
✅ **Do:** Set `spring.flyway.baselineOnMigrate=true`

---

## Command Checklist for Next Session

```bash
# 1. Verify Phase 2 still running
docker-compose ps

# 2. Create tracking-service directory structure
mkdir -p tracking-service/src/main/java/com/mhsa/backend/tracking
mkdir -p tracking-service/src/main/resources/db/migration

# 3. Copy tracking code from monolith
cp -r app/src/main/java/com/mhsa/backend/tracking/* \
  tracking-service/src/main/java/com/mhsa/backend/tracking/

# 4. Create database schema
# (Extract tracking tables from app/src/main/resources/db/migration/V1__create_tables.sql)

# 5. Update pom.xml modules
# Add: <module>tracking-service</module>

# 6. Build and test
docker-compose down
docker-compose up -d --build

# 7. Verify tracking-service started
docker-compose logs tracking-service | grep "Started\|error"

# 8. Test endpoints
curl http://localhost:8083/actuator/health
curl http://localhost:8083/internal/v1/tracking/context/$(uuidgen)?days=7

# 9. Test ai-service still works with new tracking URL
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/v1/ai/chat/send \
  -H "Content-Type: application/json" \
  -d '{"content":"Hello"}'
```

---

## Timeline Estimate

- Steps 1-3 (Setup + Extract): 2.5 hours
- Steps 4-6 (API + Security + Events): 2.5 hours
- Steps 7-11 (Config + Docker + Integration): 1.5 hours
- Testing & debugging: 1-2 hours

**Total: 7-9 hours of work (can split across 1-2 days)**

---

## After Phase 3 Complete

Will be ready for Phase 4 (Optional):
- **Dashboard Service Extraction** - Extract dashboard module if needed
- **API Gateway** - Add Kong/Spring Cloud Gateway
- **Service Discovery** - Add Eureka/Consul
- **Full Event Processing** - Complete RabbitMQ pipelines

---

## Key Success Metrics

✅ `tracking-service` running on port 8083  
✅ `postgres-tracking` with all 6 tracking tables  
✅ `GET /internal/v1/tracking/context/{profileId}` returns valid context data  
✅ AI Service calls tracking-service (not backend-app) for context  
✅ Database migrations auto-run on startup  
✅ All tests pass without authentication errors  
✅ RabbitMQ events logged when tracking data created  

---

## Related Documentation

- [PHASE2_COMPLETION.md](PHASE2_COMPLETION.md) - Phase 2 reference
- [PHASE2_SESSION_PROMPT.md](PHASE2_SESSION_PROMPT.md) - Phase 2 guide
- [docker-compose.yml](docker-compose.yml) - Current infrastructure
- [pom.xml](pom.xml) - Parent module configuration

---

**Ready to start Phase 3!** This extracts the last major monolith module, leaving only the dashboard and admin functions in the main `app` service. Phase 3 establishes the full microservices pattern. 🚀

After Phase 3, the architecture will be:
- **auth-service** (8081) - Authentication & authorization
- **ai-service** (8082) - AI chat with context aggregation
- **tracking-service** (8083) - User tracking data & analytics
- **app/monolith** (8080) - Dashboard & admin only (reduced scope)

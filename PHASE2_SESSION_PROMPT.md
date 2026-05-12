# Next Session Prompt - Phase 2: AI Service Extraction

## Current Status (2026-05-12)

**Phase 1:** ✅ COMPLETE - Auth-service fully tested, all 5 endpoints working
- JWKS endpoint returning public keys
- Registration & login functional with JWT generation
- Internal APIs accessible for service-to-service communication
- PostgreSQL (auth_db), Redis, MinIO running and verified

**Latest Commits:**
```
b8f557d fix: make TokenBlacklistService optional in JwtAuthenticationFilter and allow internal endpoints
6afadc1 fix: unescape JWT key newlines in auth-service entrypoint.sh
31c906b fix: move TokenBlacklistService to shared-jwt library and fix JWT key handling
```

**Docker Services Running:**
- postgres-auth:5432 ✅
- redis:6379 ✅
- minio:9000 ✅
- auth-service:8081 ✅

---

## Phase 2 Task: Extract AI Service (3-4 days estimate)

### Architecture Overview

```
thesis-backend/
├── app/                         # Monolith (tracking, dashboard remain)
├── auth-service/                # Phase 1 ✅
├── ai-service/                  # Phase 2 → YOU ARE HERE
│   ├── src/main/java/com/mhsa/backend/ai/   # Extracted AI code
│   ├── src/main/resources/db/migration/     # AI tables only
│   ├── pom.xml                  # Own Maven module
│   ├── Dockerfile               # Own container
│   └── application-docker.properties        # Config
├── shared-jwt/                  # Reusable (has TokenBlacklistService)
├── shared-contracts/            # Cross-service DTOs
├── docker-compose.yml           # Add ai-service (8082, ai_db)
└── Dockerfile.build             # Multi-stage build
```

### Step-by-Step Implementation

#### Step 1: Create ai-service Module Structure (30 min)

```bash
# Create directories
mkdir -p ai-service/src/main/java/com/mhsa/backend/ai
mkdir -p ai-service/src/main/resources/db/migration
mkdir -p ai-service/src/test/java

# Create pom.xml with dependencies:
# - spring-boot-starter-web
# - spring-boot-starter-jpa
# - postgresql
# - spring-data-redis
# - shared-jwt (dependency)
# - shared-contracts (dependency)
```

**Key pom.xml settings:**
```xml
<artifactId>ai-service</artifactId>
<version>0.0.1-SNAPSHOT</version>
<name>AI Service</name>
<properties>
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
</properties>
<!-- Dependencies: spring-web, jpa, postgresql, redis, shared-jwt, shared-contracts -->
```

#### Step 2: Extract AI Code from Monolith (1-2 hours)

**Source:** `app/src/main/java/com/mhsa/backend/ai/`

**Classes to Extract:**
- Controllers: `AiChatController`, `AiConversationController`
- Services: `AiChatService`, `AiConversationService`, `ContextAggregatorService`
- Models: `AiChat`, `AiConversation`, `ContextMessage`
- Repositories: `AiChatRepository`, `AiConversationRepository`
- DTOs: `AiChatRequest`, `AiChatResponse`, `ConversationRequest`
- Configuration: `AiConfig` (if exists)

**Copy to:** `ai-service/src/main/java/com/mhsa/backend/ai/`

**Package names remain:** `com.mhsa.backend.ai.*`

#### Step 3: Create AI-Only Database Schema (45 min)

**Create:** `ai-service/src/main/resources/db/migration/V1__create_ai_tables.sql`

**Extract from:** `app/src/main/resources/db/migration/V2__*.sql` (only AI tables)

**Tables needed:**
```sql
-- ai_conversations (conversation history)
CREATE TABLE ai_conversations (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  title VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)  -- references auth-service
);

-- ai_messages (conversation messages)
CREATE TABLE ai_messages (
  id UUID PRIMARY KEY,
  conversation_id UUID NOT NULL,
  role VARCHAR(50),
  content TEXT,
  created_at TIMESTAMP,
  FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id)
);

-- (Add other AI-related tables from V2__*.sql)
```

**Important:** Do NOT include auth tables (users, profiles) - those stay in auth_db

#### Step 4: Refactor ContextAggregatorService (1-2 hours)

**Critical Change:** Call tracking-service via REST instead of direct service injection

**Current (Monolith):**
```java
@Autowired
private TrackingService trackingService;  // Direct dependency

public List<ContextData> aggregateContext(UUID profileId) {
    return trackingService.getTrackingData(profileId);  // Same process
}
```

**New (Microservice):**
```java
private final RestTemplate restTemplate;

public List<ContextData> aggregateContext(UUID profileId) {
    try {
        String url = "http://backend-app:8080/internal/v1/tracking/context/{profileId}?days=7";
        ResponseEntity<ContextData[]> response = 
            restTemplate.getForEntity(url, ContextData[].class, profileId);
        return Arrays.asList(response.getBody());
    } catch (RestClientException e) {
        logger.warn("Failed to fetch context from tracking-service", e);
        return Collections.emptyList();
    }
}
```

**Note for Phase 3:** URL will change to `http://tracking-service:8083/internal/v1/tracking/context/...`

#### Step 5: Implement JWKS Validation (45 min)

**Create:** `ai-service/src/main/java/com/mhsa/backend/ai/config/JwtSecurityConfig.java`

**Setup:**
```java
@Configuration
@EnableWebSecurity
public class JwtSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/internal/v1/**").permitAll()  // Service-to-service
                .requestMatchers("/api/v1/ai/**").authenticated()  // Client APIs
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtils(), userDetailsService());
    }
}
```

**AI-service will:**
1. Import `shared-jwt` library (already has JwtUtils, JwtAuthenticationFilter)
2. Fetch JWKS from auth-service: `http://auth-service:8081/internal/v1/.well-known/jwks.json`
3. Validate incoming JWT tokens using shared-jwt library
4. Extract user context (userId, profileId, role) from JWT claims

#### Step 6: Setup RabbitMQ Event Subscription Placeholder (30 min)

**Create:** `ai-service/src/main/java/com/mhsa/backend/ai/messaging/AuthEventListener.java`

**Placeholder Implementation:**
```java
@Component
public class AuthEventListener {
    
    private final JwtAuthenticationCache jwtCache;  // Redis-backed cache
    
    @RabbitListener(queues = "auth.grant.created")
    public void onGrantCreated(GrantCreatedEvent event) {
        // Placeholder: Invalidate cache when grants change
        logger.info("Grant created event received: {}", event.getGranteeId());
        // In Phase 3, will invalidate permission cache
    }
    
    @RabbitListener(queues = "auth.user.updated")
    public void onUserUpdated(UserUpdatedEvent event) {
        // Placeholder: React to user profile changes
        logger.info("User updated event received: {}", event.getUserId());
    }
}
```

**Docker-compose RabbitMQ:** Add to docker-compose.yml (use existing or create new)

#### Step 7: Configuration Files (30 min)

**Create:** `ai-service/src/main/resources/application-docker.properties`

```properties
# Server
server.port=8082
spring.application.name=ai-service

# Database (separate from auth-service)
spring.datasource.url=jdbc:postgresql://postgres-ai:5432/ai_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway/Migration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baselineOnMigrate=true

# Redis (shared for caching)
spring.data.redis.host=redis
spring.data.redis.port=6379

# JWT (loaded from shared-jwt library)
mhsa.app.jwtExpirationMs=3600000
mhsa.app.jwtIssuer=mhsa.backend
mhsa.app.jwtAudience=mhsa-api

# Service URLs (for cross-service calls)
service.tracking.url=http://backend-app:8080
service.auth.url=http://auth-service:8081

# Logging
logging.level.root=INFO
logging.level.com.mhsa.backend=DEBUG
```

**Create:** `ai-service/Dockerfile`

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/ai-service.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
```

**Create:** `ai-service/entrypoint.sh`

```bash
#!/bin/sh
exec java -Dspring.profiles.active=docker -jar /app/app.jar
```

#### Step 8: Update docker-compose.yml (30 min)

**Add postgres-ai service:**
```yaml
postgres-ai:
  image: postgres:16-alpine
  container_name: postgres-ai
  environment:
    POSTGRES_DB: ai_db
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
  ports:
    - "5433:5432"  # Different port from auth-service
  volumes:
    - postgres-ai-data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U postgres"]
    interval: 10s
    timeout: 5s
    retries: 5
  networks:
    - mhsa-network
```

**Add ai-service:**
```yaml
ai-service:
  build:
    context: .
    dockerfile: Dockerfile.build
  container_name: ai-service
  environment:
    SERVER_PORT: 8082
    # ... (other env vars from docker-compose pattern)
  ports:
    - "8082:8082"
  depends_on:
    postgres-ai:
      condition: service_healthy
    redis:
      condition: service_healthy
    auth-service:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8082/actuator/health"]
    interval: 30s
    timeout: 10s
    start_period: 40s
    retries: 3
  networks:
    - mhsa-network
```

**Add volume:**
```yaml
volumes:
  # ... existing volumes
  postgres-ai-data:
```

#### Step 9: Update Root pom.xml (15 min)

**Add to `<modules>`:**
```xml
<module>ai-service</module>
```

**Update Dockerfile.build to include ai-service:**
```dockerfile
# Line 11 in Dockerfile.build
RUN mvn clean package -DskipTests
# This will automatically build ai-service since it's in the modules
```

#### Step 10: Security & Authorization (1 hour)

**Create:** `ai-service/src/main/java/com/mhsa/backend/ai/controller/AiChatController.java`

**Template:**
```java
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
public class AiChatController {
    
    private final AiChatService aiChatService;
    private final JwtUtils jwtUtils;
    
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestBody AiChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Extract user context from JWT (via SecurityContext)
        UUID userId = // extracted from JWT via JwtAuthenticationFilter
        UUID profileId = // from JWT claims
        
        return ResponseEntity.ok(aiChatService.processMessage(profileId, request));
    }
}
```

---

## Testing Plan for Phase 2

After extracting AI service, test:

1. **Health Check:** `GET http://localhost:8082/actuator/health`
2. **JWKS Fetch:** Service fetches from auth-service successfully
3. **JWT Validation:** Send JWT from Phase 1 login, verify accepted
4. **Cross-Service Call:** `GET http://ai-service:8082/internal/v1/...` works
5. **Tracking Service Call:** AI-service successfully calls `http://backend-app:8080/internal/v1/tracking/context/{profileId}`
6. **Database Migration:** ai_db created with all AI tables
7. **Cache Integration:** Redis cache working for conversation context

---

## Common Pitfalls to Avoid

❌ **Don't:** Extract user tables to ai_db
✅ **Do:** Keep user/profile queries as cross-service REST calls

❌ **Don't:** Hard-code service URLs
✅ **Do:** Use environment variables (configured in docker-compose)

❌ **Don't:** Import monolith modules directly in ai-service pom.xml
✅ **Do:** Use only shared-jwt and shared-contracts libraries

❌ **Don't:** Forget Flyway migration baseline
✅ **Do:** Set `spring.flyway.baselineOnMigrate=true`

---

## Command Checklist for Next Session

```bash
# 1. Verify Phase 1 still running
docker-compose ps
curl http://localhost:8081/internal/v1/.well-known/jwks.json

# 2. Create ai-service directory structure
mkdir -p ai-service/src/main/java/com/mhsa/backend/ai
mkdir -p ai-service/src/main/resources/db/migration

# 3. Copy AI code from monolith
cp app/src/main/java/com/mhsa/backend/ai/* ai-service/src/main/java/com/mhsa/backend/ai/

# 4. Create database schema file
# (Extract AI tables from app/src/main/resources/db/migration/V2__*.sql)

# 5. Build and test
docker-compose down
docker-compose up -d --build

# 6. Verify ai-service started
docker-compose logs ai-service | grep "Started\|error"

# 7. Test endpoints
curl http://localhost:8082/actuator/health
```

---

## Timeline Estimate

- Steps 1-3 (Setup): 1 hour
- Steps 4-5 (Refactoring): 2-3 hours  
- Steps 6-7 (Config): 1 hour
- Step 8 (Docker setup): 30 min
- Step 9-10 (Integration): 1 hour
- Testing & debugging: 1-2 hours

**Total: 6-8 hours of work (split across 3-4 days)**

---

## After Phase 2 Complete

Will be ready for Phase 3:
- **Tracking Service Extraction** - Extract tracking/analytics module
- AI-service will automatically point to tracking-service (DNS update only)
- Implement full async event processing

---

## Useful Git Commands

```bash
# Create feature branch for Phase 2
git checkout -b feature/phase-2-ai-service

# Commit after each major step
git add -A && git commit -m "feat(phase-2): create ai-service structure"
git commit -m "feat(phase-2): extract AI code from monolith"
git commit -m "feat(phase-2): implement context aggregator REST calls"

# Push when Phase 2 complete
git push origin feature/phase-2-ai-service
```

---

## Files Reference

**Memory documents:**
- `memory/MEMORY.md` - Index
- `memory/phase-1-testing-complete.md` - Phase 1 results
- `memory/phase-1-auth-service.md` - Phase 1 details
- This file: `PHASE2_SESSION_PROMPT.md` - Phase 2 guide

**Key files to check:**
- `app/src/main/java/com/mhsa/backend/ai/` - Code to extract
- `app/src/main/resources/db/migration/V2__*.sql` - Database schema
- `pom.xml` - Parent module (add ai-service)
- `docker-compose.yml` - Add ai-service + postgres-ai

---

**Happy coding! Phase 2 is well-documented and ready to go. 🚀**

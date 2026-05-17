# 🏗️ System Architecture

## Overview

The MHSA Backend uses a **Microservices Architecture** with Docker containerization, featuring 4 independent microservices, 2 shared libraries, and multiple infrastructure components.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT (Frontend)                           │
│                                                                     │
│  Web Browser / Mobile App (http://localhost:8080)                  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────┐
│                    NGINX API GATEWAY                                │
│                      Port: 8080                                      │
│                  (Reverse Proxy + Load Balancer)                    │
└─────┬────────────────┬─────────────────┬──────────────────┬─────────┘
      │                │                 │                  │
      ▼                ▼                 ▼                  ▼
  ┌────────┐    ┌──────────┐    ┌───────────┐    ┌──────────────┐
  │  Auth  │    │    AI    │    │ Tracking  │    │  Dashboard   │
  │Service │    │ Service  │    │ Service   │    │   Service    │
  │ :8081  │    │  :8082   │    │  :8083    │    │   :8084      │
  └────┬───┘    └────┬─────┘    └────┬──────┘    └──────┬───────┘
       │             │               │                  │
       └─────────────┼───────────────┼──────────────────┘
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
    ┌─────────┐ ┌────────┐ ┌─────────┐
    │JWT Lib  │ │Contract│ │ Shared  │
    │         │ │ Lib    │ │ Security│
    └─────────┘ └────────┘ └─────────┘
         │           │           │
         └───────────┼───────────┘
                     │
         ┌───────────┼────────────────────┐
         │           │                    │
         ▼           ▼                    ▼
    ┌─────────┐ ┌──────────┐        ┌───────────┐
    │ Redis   │ │ RabbitMQ │        │PostgreSQL │
    │ Cache   │ │ Messages │        │Databases  │
    │ :6379   │ │ :5672    │        │(3x)       │
    └─────────┘ └────┬─────┘        └─────┬─────┘
                     │                     │
                ┌────┴─────────┐      ┌────┴──────┐
                │              │      │           │
                ▼              ▼      ▼           ▼
            RabbitMQ      MinIO    auth_db    ai_db
            Management    Storage  tracking_db
            UI :15672     UI :9001
```

---

## Microservices

### 1. **Auth Service** (Port: 8081)

**Responsibility:** User authentication, profile management, role-based access control

**Stack:**
- Spring Boot 4.0
- Spring Security with JWT
- Spring Data JPA (Hibernate)
- PostgreSQL (auth_db)

**Key Components:**
```
auth-service/
├── controller/
│   ├── AuthController          (Register, Login, Profile CRUD)
│   ├── DataAccessGrantController (Data sharing permissions)
│   └── InternalController      (Internal APIs for other services)
├── service/
│   ├── AuthService             (Core auth logic)
│   ├── TokenBlacklistService   (Logout token management)
│   ├── FileStorageService      (Avatar uploads)
│   └── JwtUtils                (Token generation/validation)
├── model/
│   ├── User                    (User account)
│   ├── Profile                 (Base profile - JOINED inheritance)
│   ├── TeenProfile             (Teen account type)
│   ├── TherapistProfile        (Therapist account type)
│   └── DataAccessGrant         (Data sharing between users)
└── config/
    ├── SecurityConfig          (Spring Security setup)
    └── RabbitMQConfig          (Event messaging)
```

**Database Schema:**
```sql
-- Users & Profiles
profiles (PK: profile_id, FK: user_id, discriminator: profile_type)
├── TEEN
├── THERAPIST

-- Data Sharing
data_access_grants (PK: grant_id)
├── granter_profile_id (FK)
├── grantee_profile_id (FK)
├── status (ACTIVE, REVOKED, EXPIRED)
```

**Endpoints:**
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `GET /api/v1/auth/me` - Get current user
- `PATCH /api/v1/auth/profile` - Update profile
- `POST /api/v1/auth/profile/avatar` - Upload avatar
- `POST /api/v1/auth/grants` - Grant data access
- `GET /api/v1/auth/grants` - List grants

---

### 2. **AI Service** (Port: 8082)

**Responsibility:** AI-powered mental health chatbot using Google Gemini API

**Stack:**
- Spring Boot 4.0
- Google Gemini API integration
- Spring Data JPA
- PostgreSQL (ai_db)
- RabbitMQ (event publishing)

**Key Components:**
```
ai-service/
├── controller/
│   ├── AiChatController        (Chat sessions & messages)
│   └── InternalController      (Dashboard stats)
├── service/
│   ├── ChatSessionService      (Session management)
│   ├── ChatMessageService      (Message CRUD)
│   ├── GeminiAiService         (Gemini API calls)
│   └── AiEventPublisher        (Event publishing)
├── model/
│   ├── ChatSession             (Conversation session)
│   ├── ChatMessage             (Individual message)
│   └── AiResponse              (Gemini response wrapper)
└── config/
    ├── RestClientConfig        (HTTP client setup)
    └── RabbitMQConfig          (Event messaging)
```

**Database Schema:**
```sql
-- Chat Sessions
chat_sessions (PK: session_id)
├── profile_id (FK to auth_service.profiles)
├── title
├── created_at

-- Chat Messages
chat_messages (PK: message_id)
├── session_id (FK)
├── sender (USER | AI)
├── content
├── timestamp
```

**Endpoints:**
- `POST /api/v1/ai/sessions` - Create chat session
- `GET /api/v1/ai/sessions` - List sessions
- `GET /api/v1/ai/sessions/{id}` - Get session details
- `POST /api/v1/ai/sessions/{id}/messages` - Send message to Gemini

**External Integration:**
- Google Gemini 1.5 Flash API
- REST calls with response streaming

---

### 3. **Tracking Service** (Port: 8083)

**Responsibility:** Health tracking data (mood, sleep, food, diary, streak system)

**Stack:**
- Spring Boot 4.0
- Spring Data JPA
- PostgreSQL (tracking_db)
- Redis (caching context)
- RabbitMQ (event-driven updates)

**Key Components:**
```
tracking-service/
├── controller/
│   ├── MoodLogController       (Mood logging CRUD)
│   ├── SleepLogController      (Sleep logging CRUD)
│   ├── FoodLogController       (Food logging CRUD)
│   ├── DiaryEntryController    (Diary CRUD)
│   ├── StreakController        (Streak tracking)
│   ├── ContextController       (Aggregated data)
│   ├── DataAccessGrantController (Share tracking data)
│   └── InternalController      (For dashboard)
├── service/
│   ├── MoodLogService
│   ├── SleepLogService
│   ├── FoodLogService
│   ├── DiaryEntryService
│   ├── StreakService           (Calculate streaks)
│   ├── ContextAggregatorService (Aggregate past N days)
│   ├── TrackingEventPublisher  (RabbitMQ events)
│   └── AuthEventListener       (User deletion cascades)
├── model/
│   ├── MoodLog
│   ├── SleepLog
│   ├── FoodLog
│   ├── DiaryEntry
│   ├── Streak
│   ├── MediaAttachment
│   └── DataAccessGrant
└── config/
    ├── CacheConfig             (Redis caching)
    ├── RabbitMQConfig
    └── RestClientConfig
```

**Database Schema:**
```sql
-- Mood Tracking
mood_logs (PK: id)
├── profile_id (FK)
├── mood_score (1-10)
├── emotion_tags (JSON)
├── logged_at

-- Sleep Tracking
sleep_logs (PK: id)
├── profile_id (FK)
├── duration_minutes
├── quality (POOR|FAIR|GOOD|EXCELLENT)

-- Food Tracking
food_logs (PK: id)
├── profile_id (FK)
├── meal_type (BREAKFAST|LUNCH|DINNER|SNACK)
├── food_items (JSON)
├── calories

-- Diary Entries
diary_entries (PK: id)
├── profile_id (FK)
├── title
├── content
├── mood_at_time

-- Streaks
streaks (PK: id)
├── profile_id (FK)
├── streak_type (DAILY_LOG|MOOD_TRACKING)
├── current_streak (days)
├── best_streak (days)

-- Data Access
data_access_grants (PK: grant_id)
├── granter_profile_id (FK)
├── grantee_profile_id (FK)
├── status (ACTIVE|REVOKED)
```

**Caching Strategy:**
```
Redis Keys:
- context:{profileId}:{days}  (TTL: 30 min)
  Cached: mood logs, sleep logs, food logs, diary entries
```

**Event Publishing (RabbitMQ):**
```
Exchange: mhsa.tracking
├── tracking.mood.logged
├── tracking.sleep.logged
├── tracking.food.logged
├── tracking.diary.created
└── tracking.streak.updated
```

**Endpoints:**
- `POST /api/v1/tracking/mood` - Log mood
- `GET /api/v1/tracking/mood?days=7` - Get mood logs
- `POST /api/v1/tracking/sleep` - Log sleep
- `GET /api/v1/tracking/sleep` - Get sleep logs
- `POST /api/v1/tracking/food` - Log food
- `GET /api/v1/tracking/food` - Get food logs
- `POST /api/v1/tracking/diary` - Create diary entry
- `GET /api/v1/tracking/diary` - Get diary entries
- `GET /api/v1/tracking/context/{profileId}?days=7` - Get aggregated context
- `POST /api/v1/tracking/grants` - Share data with therapist

---

### 4. **Dashboard Service** (Port: 8084)

**Responsibility:** Backend For Frontend (BFF) - Aggregates data from all services

**Stack:**
- Spring Boot 4.0
- Async/Parallel API calls (CompletableFuture)
- Spring WebFlux (RestClient)
- No database (stateless BFF)

**Key Components:**
```
dashboard-service/
├── controller/
│   ├── DashboardController     (Summary & context)
├── service/
│   ├── DashboardService        (Parallel aggregation)
├── client/
│   ├── AuthClient              (Calls auth-service)
│   ├── TrackingClient          (Calls tracking-service)
│   ├── AiClient                (Calls ai-service)
└── config/
    ├── RestClientConfig        (HTTP client)
    └── SecurityConfig          (JWT validation)
```

**Design Pattern: BFF (Backend For Frontend)**
```
Frontend Request:
  GET /api/v1/dashboard/summary

Dashboard Service (Parallel Execution):
  ├─ CompletableFuture.supplyAsync(() -> authClient.getProfile())
  ├─ CompletableFuture.supplyAsync(() -> trackingClient.getDashboardData())
  └─ CompletableFuture.supplyAsync(() -> aiClient.getStats())

Aggregate Results → Response
```

**Endpoints:**
- `GET /api/v1/dashboard/summary` - Aggregated summary
- `GET /api/v1/dashboard/context/{profileId}?days=7` - User context
- `GET /api/v1/dashboard/health` - Service health

**No Database:**
- Stateless
- No persistence
- Lightweight & fast
- Easy to scale horizontally

---

## Shared Libraries

### 1. **shared-jwt**
Centralized JWT and security utilities used by all services

```
shared-jwt/
├── JwtUtils                    (Token creation/validation)
├── JwtAuthenticationFilter     (Spring Security filter)
├── AuthenticatedUserPrincipal  (Custom principal)
├── Role                        (Enum: TEEN, THERAPIST)
├── UnauthorizedException
└── SecurityUtils              (Extract current user from context)
```

### 2. **shared-contracts**
DTOs and response models shared across services

```
shared-contracts/
├── ApiResponse<T>             (Unified response wrapper)
├── AuthRequest/AuthResponse
├── ProfileRequest/ProfileResponse
├── TrackingRequest/TrackingResponse
└── ErrorResponse
```

---

## Infrastructure Components

### 1. **Nginx (API Gateway)**
- **Port:** 8080
- **Role:** Reverse proxy, load balancer, unified entry point
- **Routing Rules:**
  ```nginx
  /api/v1/auth/*      → auth-service:8081
  /api/v1/ai/*        → ai-service:8082
  /api/v1/tracking/*  → tracking-service:8083
  /api/v1/dashboard/* → dashboard-service:8084
  ```

### 2. **PostgreSQL Databases** (3 instances)

| Database | Port | Service | Purpose |
|----------|------|---------|---------|
| auth_db | 5432 | auth-service | Users, profiles, grants |
| ai_db | 5433 | ai-service | Chat sessions, messages |
| tracking_db | 5434 | tracking-service | Health logs, streaks |

**Isolation Principle:** Each service owns its database (Database per Service pattern)

### 3. **Redis**
- **Port:** 6379
- **Purpose:** Caching layer
- **Current Usage:**
  - Context aggregation (TTL: 30 min)
  - Session tokens (blacklisting)
  - Future: Rate limiting, distributed locks

### 4. **RabbitMQ**
- **Message Broker Port:** 5672
- **Management UI:** 15672 (guest/guest)
- **Purpose:** Asynchronous event processing

**Message Flows:**
```
1. Tracking Service Events:
   tracking.mood.logged
   tracking.food.logged
   tracking.sleep.logged
   tracking.diary.created
   tracking.streak.updated

2. Auth Service Events:
   auth.user.deleted
   auth.user.updated
   auth.grant.created
   auth.grant.revoked
```

### 5. **MinIO**
- **API Port:** 9000
- **Console Port:** 9001
- **Purpose:** S3-compatible object storage
- **Current Usage:** User avatars, diary attachments

---

## Data Flow Examples

### 1. **User Registration Flow**
```
Frontend Request:
  POST /api/v1/auth/register

Nginx Routes to:
  auth-service:8081/api/v1/auth/register

Auth Service:
  1. Validate input
  2. Hash password (BCrypt)
  3. Create User + Profile in auth_db
  4. Generate JWT tokens
  5. Publish auth.user.created event to RabbitMQ
  6. Return response with tokens

Response to Frontend:
  {
    "userId": "uuid",
    "profileId": "uuid",
    "accessToken": "...",
    "refreshToken": "..."
  }
```

### 2. **Log Mood with Streak Update Flow**
```
Frontend Request:
  POST /api/v1/tracking/mood (with JWT token)

Tracking Service:
  1. Validate token via JwtAuthenticationFilter
  2. Save MoodLog to tracking_db
  3. Update Streak (increment or reset)
  4. Cache invalidation: DELETE context:{profileId}:*
  5. Publish tracking.mood.logged event

RabbitMQ Consumers:
  - AI Service: Could subscribe to mood events for insights
  - Notification Service: Could alert on mood drops
```

### 3. **Dashboard Summary Request Flow (BFF Pattern)**
```
Frontend Request:
  GET /api/v1/dashboard/summary (with JWT)

Dashboard Service (Parallel):
  CompletableFuture 1: authClient.getProfile()
    → auth-service:8081/internal/v1/profile/summary
  
  CompletableFuture 2: trackingClient.getDashboardData()
    → tracking-service:8083/internal/v1/dashboard
  
  CompletableFuture 3: aiClient.getStats()
    → ai-service:8082/internal/v1/dashboard

Wait for all to complete → Aggregate → Return combined response

Response:
  {
    "auth": { profile data },
    "tracking": { mood, sleep, food, diary },
    "ai": { chat sessions, stats },
    "latencyMs": 245
  }
```

### 4. **Data Sharing Flow (Therapist Access)**
```
Patient Action:
  POST /api/v1/auth/grants
  { "granteeProfileId": "therapist-uuid" }

Auth Service:
  1. Create DataAccessGrant record
  2. Publish auth.grant.created event

Therapist Access:
  GET /api/v1/tracking/context/{patientId}
  
Tracking Service:
  1. Check DataAccessGrant in tracking_db (replicated from auth)
  2. Verify grant is ACTIVE
  3. Return context data
```

---

## Deployment Topology

### Docker Compose Structure
```yaml
services:
  nginx           → Container (thesis-backend-nginx)
  auth-service    → Container (thesis-backend-auth-service)
  ai-service      → Container (thesis-backend-ai-service)
  tracking-service → Container (thesis-backend-tracking-service)
  dashboard-service → Container (thesis-backend-dashboard-service)
  postgres-auth   → Container (postgres:16-alpine)
  postgres-ai     → Container (postgres:16-alpine)
  postgres-tracking → Container (postgres:16-alpine)
  redis           → Container (redis:7-alpine)
  rabbitmq        → Container (rabbitmq:3.12-management-alpine)
  minio           → Container (minio/minio:latest)

volumes:
  postgres-auth-data
  postgres-ai-data
  postgres-tracking-data
  minio-data

networks:
  mhsa-network (bridge network for inter-container communication)
```

---

## Security Architecture

### 1. **JWT Authentication**
- Generated by auth-service
- Shared across all services
- Validated by JwtAuthenticationFilter in each service
- Blacklist for logout tokens

### 2. **Role-Based Access Control (RBAC)**
```
Roles: TEEN, THERAPIST

Access Rules:
- TEEN: Can log own health data
- TEEN: Can grant access to THERAPIST
- THERAPIST: Can view TEEN data only with grant
- THERAPIST: Can update own profile

@PreAuthorize("hasRole('TEEN')")
@PreAuthorize("@accessGuard.canRead(authentication, #profileId)")
```

### 3. **Data Access Control**
- Owner-based access (can only modify own profile)
- Grant-based access (therapist with active grant)
- Checked in AccessGuard utility

---

## Performance Optimizations

### 1. **Caching Strategy**
```
Redis Cache:
- Key: context:{profileId}:{days}
- Value: Aggregated mood, sleep, food, diary data
- TTL: 30 minutes
- Invalidate on: New log entry, profile update
```

### 2. **Database Indexing**
```sql
CREATE INDEX idx_mood_logs_profile_logged
ON mood_logs(profile_id, logged_at);

CREATE INDEX idx_streak_profile
ON streaks(profile_id, streak_type);

CREATE INDEX idx_grants_pair
ON data_access_grants(granter_profile_id, grantee_profile_id);
```

### 3. **Async Processing**
```
RabbitMQ Events:
- Non-blocking event publishing
- Decoupled services
- Fault tolerance via message queue

CompletableFuture:
- Parallel API calls in Dashboard Service
- Reduce latency (parallel vs sequential)
```

---

## Future Enhancements

### Planned Services
1. **Therapist Service** - Discovery, directory, marketplace
2. **Messaging Service** - Real-time chat
3. **Booking Service** - Appointment scheduling
4. **Social Service** - Feed, reviews, community
5. **Streaming Service** - Video consultation

### Technology Upgrades
- gRPC for service-to-service communication
- GraphQL for flexible data queries
- Kafka for high-throughput event streaming
- Elasticsearch for full-text search
- Kubernetes for orchestration

# Microservices Setup & Deployment Guide

## Quick Start (5 minutes)

### Prerequisites
- Docker & Docker Compose installed
- Git cloned: `d:\StudyFiles\Thesis\thesis-backend`
- Java 17+ (for local development)
- Maven 3.8+ (for local development)

### Start Full Stack

```bash
# 1. Navigate to project root
cd d:\StudyFiles\Thesis\thesis-backend

# 2. Build all services (first time only)
mvn clean package -DskipTests

# 3. Start Docker Compose stack
docker-compose up -d

# 4. Wait for services to be healthy (30-60 seconds)
docker-compose ps

# 5. Verify all services are running
curl http://localhost:8080/health
```

**Expected Output:**
```json
{
  "status": "healthy"
}
```

---

## Service Architecture

### Port Mapping

| Service | Port | Internal | Purpose |
|---------|------|----------|---------|
| **Nginx (BFF)** | 8080 | - | Public API Gateway |
| **Auth Service** | 8081 | Yes | User authentication & JWT |
| **AI Service** | 8082 | Yes | Chat & AI features |
| **Tracking Service** | 8083 | Yes | User tracking data |
| **Dashboard Service** | 8084 | Yes | BFF Aggregator |
| **PostgreSQL Auth** | 5432 | Docker | Auth database |
| **PostgreSQL AI** | 5433 | Docker | AI database |
| **PostgreSQL Tracking** | 5434 | Docker | Tracking database |
| **Redis** | 6379 | Docker | Caching & sessions |
| **RabbitMQ** | 5672 | Docker | Event messaging |
| **RabbitMQ Admin** | 15672 | Docker | Admin console |
| **MinIO** | 9000 | Docker | File storage |
| **MinIO Console** | 9001 | Docker | Storage admin |

### Communication Flow

```
┌─────────────────────────────────────────────────┐
│         Frontend (React/Vue/Flutter)            │
│          http://localhost:3000                  │
└────────────────────┬────────────────────────────┘
                     │
                     ▼ HTTP/REST
          ┌──────────────────────┐
          │  Nginx Gateway       │
          │ (8080) Public API    │
          └──────────┬───────────┘
                     │
     ┌───────────┬───┴───────┬──────────┐
     ▼           ▼           ▼          ▼
  ┌─────────┐ ┌────────┐ ┌─────────┐ ┌──────────┐
  │Auth Svc │ │ AI Svc │ │Tracking │ │Dashboard │
  │(8081)   │ │(8082)  │ │ (8083)  │ │ (8084)   │
  └────┬────┘ └────┬───┘ └────┬────┘ └──────────┘
       │            │          │        (BFF)
  ┌────▼────┐  ┌────▼───┐ ┌───▼────┐
  │auth_db  │  │ai_db   │ │track_db│
  │(5432)   │  │(5433)  │ │(5434)  │
  └─────────┘  └────────┘ └────────┘
```

---

## Running Locally (Development)

### Option 1: Full Docker Compose (Recommended)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Clean volumes (WARNING: deletes data)
docker-compose down -v
```

### Option 2: Hybrid Mode (Services + Local Java)

```bash
# Start only databases and infrastructure
docker-compose up -d postgres-auth postgres-ai postgres-tracking redis rabbitmq minio

# Start each service locally in separate terminals
cd auth-service && mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=docker"
cd ai-service && mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=docker"
cd tracking-service && mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=docker"
cd dashboard-service && mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=docker"
```

### Option 3: Local Everything (Windows PowerShell)

```powershell
# 1. Build all modules
mvn clean install -DskipTests

# 2. Start Redis (requires local Redis server)
# Download from: https://github.com/microsoftarchive/redis/releases

# 3. Start RabbitMQ (requires local RabbitMQ server)
# Download from: https://www.rabbitmq.com/install-windows.html

# 4. Create PostgreSQL databases
# psql -U postgres -c "CREATE DATABASE auth_db;"
# psql -U postgres -c "CREATE DATABASE ai_db;"
# psql -U postgres -c "CREATE DATABASE tracking_db;"

# 5. Run each service
$services = @(
    "auth-service",
    "ai-service", 
    "tracking-service",
    "dashboard-service"
)

foreach ($svc in $services) {
    Start-Process -FilePath "cmd" `
        -ArgumentList "/k cd $svc && mvn spring-boot:run" `
        -WindowStyle Normal
}
```

---

## Testing & Verification

### 1. Health Check All Services

```bash
# Via Nginx gateway
curl http://localhost:8080/health

# Direct service checks
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # AI
curl http://localhost:8083/actuator/health  # Tracking
curl http://localhost:8084/api/v1/dashboard/health  # Dashboard
```

### 2. Test Auth Service

```bash
# Register new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!",
    "name": "Test User"
  }'

# Expected Response (201 Created)
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "profileId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "test@example.com"
  }
}

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

### 3. Test Dashboard Service (Aggregation)

```bash
# Get aggregated dashboard
# Replace TOKEN with JWT from register/login
TOKEN="eyJhbGciOi..."

curl -X GET http://localhost:8080/api/v1/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"

# Expected Response
{
  "success": true,
  "data": {
    "profileId": "550e8400-e29b-41d4-a716-446655440000",
    "latencyMs": 234,
    "auth": {
      "profileId": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Test User",
      "email": "test@example.com",
      "role": "USER"
    },
    "tracking": {
      "latestMood": null,
      "moodCount": 0,
      "avgSleepMinutes": 0,
      "sleepCount": 0,
      "currentStreak": 0,
      "longestStreak": 0
    },
    "ai": {
      "totalSessions": 0,
      "activeSessions": 0
    }
  }
}
```

### 4. Test Tracking Service

```bash
# Create mood log
curl -X POST http://localhost:8080/api/v1/tracking/moods \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "positivityScore": 7,
    "note": "Feeling great today"
  }'

# Get all moods
curl -X GET http://localhost:8080/api/v1/tracking/moods \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Test AI Service

```bash
# Send chat message
curl -X POST http://localhost:8080/api/v1/ai/chat/send \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "I am feeling anxious today",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000"
  }'

# Get chat history
curl -X GET http://localhost:8080/api/v1/ai/chat/history/{sessionId} \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Test File Upload (MinIO)

```bash
# Upload avatar
curl -X POST http://localhost:8080/api/v1/auth/profile/avatar \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/avatar.jpg"
```

---

## Docker Compose Monitoring

### View Service Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service
docker-compose logs -f tracking-service
docker-compose logs -f dashboard-service

# Last 50 lines
docker-compose logs --tail=50
```

### Access Service Consoles

| Service | URL | User | Password |
|---------|-----|------|----------|
| RabbitMQ | http://localhost:15672 | guest | guest |
| MinIO | http://localhost:9001 | minioadmin | minioadmin |

### Check Service Status

```bash
# Docker status
docker-compose ps

# Service dependencies
docker-compose logs --tail=20 nginx
```

---

## Environment Variables

### Authentication (.env file)

Create `.env` file in root directory:

```bash
# JWT Configuration
JWT_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQE...\n-----END PRIVATE KEY-----"
JWT_SIGNING_KID="mhsa.app.jwtSigningKid"
JWT_EXPIRATION_MS=3600000

# Database URLs (auto-configured in docker-compose)
SPRING_DATASOURCE_URL_AUTH=jdbc:postgresql://postgres-auth:5432/auth_db
SPRING_DATASOURCE_URL_AI=jdbc:postgresql://postgres-ai:5433/ai_db
SPRING_DATASOURCE_URL_TRACKING=jdbc:postgresql://postgres-tracking:5434/tracking_db

# S3/MinIO Storage
S3_ENDPOINT=http://minio:9000
S3_REGION=us-east-1
S3_BUCKET=mhsa-media
S3_ACCESS_KEY=minioadmin
S3_SECRET_KEY=minioadmin
S3_PATH_STYLE_ACCESS=true

# Gemini AI API
GEMINI_API_KEY="your-gemini-api-key"
```

---

## Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose logs service-name

# Common issues:
# 1. Port already in use
lsof -i :8080  # Find process on port
kill -9 <PID>   # Kill process

# 2. Database not ready
docker-compose logs postgres-auth

# 3. DNS resolution issue
docker-compose down
docker-compose up -d --force-recreate
```

### Service Communication Fails

```bash
# Test internal network
docker exec dashboard-service curl http://auth-service:8081/actuator/health

# Check Docker network
docker network inspect thesis-backend_mhsa-network
```

### Database Issues

```bash
# Reset database
docker-compose down -v
docker-compose up -d

# Backup database
docker exec postgres-auth pg_dump -U postgres auth_db > backup.sql

# Restore database
docker exec -i postgres-auth psql -U postgres auth_db < backup.sql
```

### Clear Docker Resources

```bash
# Stop all containers
docker-compose down

# Remove all images
docker rmi auth-service ai-service tracking-service dashboard-service

# Remove volumes
docker volume prune

# Full cleanup
docker system prune -a --volumes
```

---

## Performance Optimization

### Connection Pooling

Services use HikariCP (auto-configured):
- Auth: 10 connections
- AI: 10 connections  
- Tracking: 15 connections (more load)
- Dashboard: 5 connections (read-only)

### Caching Strategy

| Component | Cache | TTL | Hit Rate |
|-----------|-------|-----|----------|
| JWKS | Redis | 1 hour | 99%+ |
| Grant checks | Redis | 60s | 80%+ |
| Context | Redis | 30min | 70%+ |
| Database | None | - | DB queries direct |

### Monitoring

```bash
# Check CPU/Memory usage
docker stats

# Expected resource usage:
# - Each service: 200-400MB RAM
# - Nginx: 50MB RAM
# - PostgreSQL: 500MB RAM
# - Redis: 100MB RAM
# - RabbitMQ: 300MB RAM
# - Total: ~2.5GB
```

---

## Next: Frontend Integration

See `FRONTEND_INTEGRATION.md` for detailed guide on connecting React/Vue/Flutter applications to this backend.

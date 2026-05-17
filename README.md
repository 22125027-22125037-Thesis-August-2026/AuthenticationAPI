# 🏥 Mental Health Support System (MHSA) - Backend

This is the Backend API for the **Mental Health Support Application** (Thesis Project).
Built with **Java Spring Boot 4.0** using a **Microservices Architecture**, powered by **PostgreSQL**, **Redis**, **RabbitMQ**, and **Docker Compose**.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Nginx API Gateway (Port 8080)                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ├─ /api/v1/auth/*      → auth-service (8081)              │
│  ├─ /api/v1/ai/*        → ai-service (8082)                │
│  ├─ /api/v1/tracking/*  → tracking-service (8083)          │
│  └─ /api/v1/dashboard/* → dashboard-service (8084)         │
│                                                              │
└─────────────────────────────────────────────────────────────┘

Microservices (4):
├── auth-service:8081           (User authentication, profiles)
├── ai-service:8082             (AI chat, Gemini integration)
├── tracking-service:8083       (Mood, sleep, food, diary logs)
└── dashboard-service:8084      (BFF - aggregates all services)

Shared Libraries (2):
├── shared-jwt                  (JWT utilities, security)
└── shared-contracts            (DTOs, common interfaces)

Infrastructure (5):
├── PostgreSQL (3 instances)    (auth_db, ai_db, tracking_db)
├── Redis                       (Caching)
├── RabbitMQ                    (Event messaging)
├── MinIO                       (File storage)
└── Nginx                       (Reverse proxy gateway)
```

---

## 🛠 Prerequisites

Ensure you have the following installed:

| Requirement | Version | Download |
|---|---|---|
| **Java JDK** | 21+ | [Download](https://www.oracle.com/java/technologies/downloads/#java21) |
| **Docker Desktop** | Latest | [Download](https://www.docker.com/products/docker-desktop/) |
| **Docker Compose** | 2.0+ | (Included in Docker Desktop) |
| **Git** | Any | [Download](https://git-scm.com/) |

---

## 🚀 Quick Start

### Step 1: Start All Services (Docker Compose)

```powershell
cd d:\StudyFiles\Thesis\thesis-backend
docker-compose up -d
```

This starts **11 containers**:
- ✅ 4 Microservices (auth, ai, tracking, dashboard)
- ✅ 3 PostgreSQL databases (auth_db, ai_db, tracking_db)
- ✅ Redis, RabbitMQ, MinIO
- ✅ Nginx gateway

**Verify all services are healthy:**
```powershell
docker-compose ps
# All should show "healthy" status
```

### Step 2: Access the Backend

| Service | URL | Purpose |
|---------|-----|---------|
| **API Gateway (Nginx)** | http://localhost:8080 | Main entry point for frontend |
| **Auth Service** | http://localhost:8081 | User authentication, profiles |
| **AI Service** | http://localhost:8082 | AI chat endpoints |
| **Tracking Service** | http://localhost:8083 | Health tracking data |
| **Dashboard Service** | http://localhost:8084 | Aggregated dashboard data |

### Step 3: Test API Health

```bash
# Via Nginx Gateway (recommended for frontend)
curl http://localhost:8080/health

# Individual service health checks
curl http://localhost:8081/actuator/health      # auth-service
curl http://localhost:8082/actuator/health      # ai-service
curl http://localhost:8083/actuator/health      # tracking-service
curl http://localhost:8084/api/v1/dashboard/health  # dashboard-service
```

---

## 📚 API Documentation

### Authentication Flow

```
1. Register User (Teen or Therapist)
   POST /api/v1/auth/register
   
2. Login
   POST /api/v1/auth/login
   → Returns: { accessToken, refreshToken }
   
3. Use Token in all requests
   Header: Authorization: Bearer <access_token>
```

### Main API Endpoints

#### 🔐 Auth Service (8081)
```
POST   /api/v1/auth/register          Register new user (teen/therapist)
POST   /api/v1/auth/login             Login user
GET    /api/v1/auth/me                Get current user profile
PATCH  /api/v1/auth/profile           Update profile
POST   /api/v1/auth/profile/avatar    Upload avatar
POST   /api/v1/auth/grants            Share data with other user
GET    /api/v1/auth/grants            List data sharing grants
POST   /api/v1/auth/logout            Logout user
```

#### 🤖 AI Service (8082)
```
GET    /api/v1/ai/sessions            List chat sessions
POST   /api/v1/ai/sessions            Create new chat session
GET    /api/v1/ai/sessions/{id}       Get session messages
POST   /api/v1/ai/sessions/{id}/messages   Send message to Gemini AI
```

#### 📊 Tracking Service (8083)
```
GET    /api/v1/tracking/diary         List diary entries
POST   /api/v1/tracking/diary         Create diary entry
GET    /api/v1/tracking/mood          List mood logs
POST   /api/v1/tracking/mood          Log mood
GET    /api/v1/tracking/sleep         List sleep logs
POST   /api/v1/tracking/sleep         Log sleep
GET    /api/v1/tracking/food          List food logs
POST   /api/v1/tracking/food          Log food
GET    /api/v1/tracking/context       Get aggregated tracking data
```

#### 📈 Dashboard Service (8084)
```
GET    /api/v1/dashboard/summary      Get dashboard summary
GET    /api/v1/dashboard/context/{id} Get user context (mood, sleep, food, diary)
GET    /api/v1/dashboard/health       Health check endpoint
```

### Use via API Gateway (Recommended for Frontend)
```
# Instead of: http://localhost:8081/api/v1/auth/...
# Use:        http://localhost:8080/api/v1/auth/...

# All endpoints go through Nginx gateway at port 8080
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "role": "TEEN",
    "accountType": "TEEN"
  }'
```

---

## 🗄️ Database Access

### Using Docker
```powershell
# View logs from a specific service
docker logs auth-service
docker logs -f tracking-service  # Follow mode

# Access PostgreSQL directly
docker exec -it postgres-auth psql -U postgres -d auth_db

# View RabbitMQ Management UI
http://localhost:15672
Username: guest
Password: guest

# View MinIO Console
http://localhost:9001
Username: minioadmin
Password: minioadmin
```

---

## 🔧 Useful Commands

### Docker Management
```powershell
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Stop and remove volumes (⚠️ deletes all data)
docker-compose down -v

# Rebuild and start (after code changes)
docker-compose up -d --build

# View service status
docker-compose ps

# View logs
docker-compose logs -f [service-name]
docker-compose logs -f auth-service
```

### Maven Build (If running services locally without Docker)
```bash
# Build all modules
./mvnw clean package

# Build specific module
./mvnw clean package -pl auth-service

# Run with Spring Boot
./mvnw spring-boot:run -pl auth-service
```

---

## 📝 Configuration

### Docker Compose Environment Variables
Located in `.env` file (if using) or set in `docker-compose.yml`:

```yaml
# Database credentials
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# JWT Configuration
JWT_EXPIRATION_MS=3600000
JWT_ISSUER=mhsa.backend
JWT_AUDIENCE=mhsa-api

# Service URLs
SERVICE_AUTH_URL=http://auth-service:8081
SERVICE_TRACKING_URL=http://tracking-service:8083
```

---

## 🔄 Data Sharing & Access Control

The system supports **data sharing between users**:

### Grant Data Access
```bash
POST /api/v1/auth/grants
{
  "granteeProfileId": "uuid-of-therapist"
}
```

### Check Shared Access
```bash
GET /api/v1/auth/grants/{profileId}
GET /api/v1/auth/grants/{profileId}/received
```

When a therapist has access, they can view the patient's:
- Mood logs
- Sleep logs
- Food logs
- Diary entries
- Aggregated context summary

---

## 🚨 Troubleshooting

### All Containers Running but Services Returning 500 Errors
**Solution:** Wait 30-60 seconds for services to fully initialize their health checks.

### Docker Port Already in Use
```powershell
# Find and kill process using port
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or change port in docker-compose.yml
```

### Database Connection Errors
```powershell
# Verify containers are running
docker-compose ps

# Check logs
docker logs postgres-auth
docker logs auth-service

# Reset database
docker-compose down -v
docker-compose up -d
```

### Service Health Check Failing
```powershell
# Check if service is actually started
docker logs dashboard-service | tail -50

# Rebuild and restart
docker-compose down
docker-compose up -d --build
```

---

## 📦 Current Features

### ✅ Implemented
- **User Profiles**: Teen & Therapist profiles with role-based access
- **Authentication**: JWT-based authentication with refresh tokens
- **AI Chat**: Integration with Google Gemini API for mental health chatbot
- **Health Tracking**: Mood, sleep, food, and diary entry logging
- **Data Aggregation**: Dashboard that aggregates data from all services
- **File Storage**: Avatar uploads via MinIO
- **Event Messaging**: RabbitMQ for async event processing
- **Caching**: Redis for performance optimization
- **API Gateway**: Nginx reverse proxy with unified entry point
- **Data Sharing**: Users can grant access to their health data to therapists

### ❌ Not Implemented (Future Features)
- Therapist directory/search
- Patient-Therapist matching system
- Appointment/booking system
- Real-time messaging between users
- Video consultation
- Social feed / community features
- Reviews and ratings
- Advanced analytics

---

## 📂 Project Structure

```
thesis-backend/
├── auth-service/              Microservice - User auth & profiles
├── ai-service/                Microservice - AI chatbot
├── tracking-service/          Microservice - Health tracking
├── dashboard-service/         Microservice - BFF aggregator
├── shared-jwt/                Shared library - JWT utilities
├── shared-contracts/          Shared library - DTOs
├── nginx/                     API Gateway configuration
├── docker-compose.yml         Container orchestration
├── pom.xml                    Maven parent POM
├── README.md                  This file
└── .github/
    └── copilot-instructions-backend.md
```

---

## 🤝 Frontend Integration

### Using Nginx Gateway (Recommended)
```javascript
// Vue.js / React example
const API_BASE = "http://localhost:8080";

// All APIs go through gateway
async function register(userData) {
  return fetch(`${API_BASE}/api/v1/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData)
  });
}

async function getTrackingData(profileId) {
  return fetch(
    `${API_BASE}/api/v1/dashboard/context/${profileId}?days=7`,
    {
      headers: { 'Authorization': `Bearer ${accessToken}` }
    }
  );
}
```

### Docker Network (If Frontend Also in Docker)
```javascript
// Use container DNS names
const API_BASE = "http://nginx:8080";  // or http://dashboard-service:8084
```

---

## 📞 Support

For issues or questions:
- Check the **Troubleshooting** section above
- Review `docker-compose logs` for error messages
- Verify all containers are healthy: `docker-compose ps`

---

## 📄 License

This project is part of a Thesis.

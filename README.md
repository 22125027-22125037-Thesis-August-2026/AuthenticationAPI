# 🏥 Mental Health Support System (MHSA) - Backend

This is the Backend API for the **Mental Health Support Application** (Thesis Project).
Built with **Java Spring Boot 4.0** using a **Microservices Architecture**, powered by **PostgreSQL**, **Redis**, **RabbitMQ**, **MinIO**, and **Docker Compose**.

---

## 📋 Table of Contents

1. [Quick Start](#quick-start)
2. [Architecture Overview](#architecture-overview)
3. [Prerequisites](#prerequisites)
4. [Setup & Deployment](#setup--deployment)
5. [Services & Ports](#services--ports)
6. [API Documentation](#api-documentation)
7. [Database Access](#database-access)
8. [Frontend Integration](#frontend-integration)
9. [Troubleshooting](#troubleshooting)
10. [Project Structure](#project-structure)

---

## 🚀 Quick Start

### One Command to Start Everything

```powershell
cd d:\StudyFiles\Thesis\thesis-backend
docker-compose up -d
```

**Wait 2-3 minutes** for all services to become healthy, then verify:

```powershell
docker-compose ps
# All 11 containers should show "healthy" status
```

**Test the API:**
```bash
curl http://localhost:8080/health
# Response: healthy
```

That's it! The backend is fully operational. No need to run Maven, build JARs, or start services manually.

---

## 🏗️ Architecture Overview

### System Diagram

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

### Core Architecture Principles

- **Microservices Pattern:** Independent services with separate databases
- **API Gateway Pattern:** Nginx routes all requests (single entry point)
- **Backend For Frontend (BFF):** Dashboard service aggregates data
- **Event-Driven:** RabbitMQ for async service communication
- **Caching:** Redis for performance optimization
- **Database per Service:** Each microservice owns its PostgreSQL instance

---

## 🛠 Prerequisites

| Requirement | Version | Download |
|---|---|---|
| **Java JDK** | 21+ | [Download](https://www.oracle.com/java/technologies/downloads/#java21) |
| **Docker Desktop** | Latest | [Download](https://www.docker.com/products/docker-desktop/) |
| **Docker Compose** | 2.0+ | (Included in Docker Desktop) |
| **Git** | Any | [Download](https://git-scm.com/) |

---

## 📦 Setup & Deployment

### Development Environment (Local Docker)

#### Step 1: Clone & Navigate

```bash
git clone <repo-url>
cd d:\StudyFiles\Thesis\thesis-backend
```

#### Step 2: Start All Services

```powershell
docker-compose up -d
```

This command:
- Builds JAR files (Maven)
- Creates Docker images
- Starts **11 containers** (4 microservices + 7 infrastructure)
- Runs health checks automatically

#### Step 3: Verify Deployment

```powershell
# Check container status
docker-compose ps

# View service logs
docker-compose logs -f auth-service
docker-compose logs -f tracking-service
```

#### Step 4: Test Health Endpoints

```bash
# Via Nginx Gateway (recommended)
curl http://localhost:8080/health

# Individual services
curl http://localhost:8081/actuator/health      # auth
curl http://localhost:8082/actuator/health      # ai
curl http://localhost:8083/actuator/health      # tracking
curl http://localhost:8084/api/v1/dashboard/health  # dashboard
```

### Production Deployment (Cloud VPS)

See **DEPLOYMENT.md** for:
- Ubuntu VPS setup with Docker
- Nginx reverse proxy with SSL
- Let's Encrypt SSL certificates
- Automated backup strategy
- Kubernetes deployment manifests

### Common Docker Commands

```powershell
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Stop and remove all data (⚠️ Careful!)
docker-compose down -v

# Rebuild after code changes
docker-compose up -d --build

# View logs
docker-compose logs -f [service-name]

# Restart a single service
docker-compose restart auth-service
```

---

## 🔌 Services & Ports

### Microservices

| Service | Port | Purpose | Database |
|---------|------|---------|----------|
| **Nginx Gateway** | 8080 | API entry point | N/A |
| **Auth Service** | 8081 | User auth, profiles, grants | auth_db (5432) |
| **AI Service** | 8082 | Gemini AI chatbot | ai_db (5433) |
| **Tracking Service** | 8083 | Health tracking (mood, sleep, food, diary) | tracking_db (5434) |
| **Dashboard Service** | 8084 | BFF - data aggregation | N/A (stateless) |

### Infrastructure & Admin UIs

| Service | Port | URL | Credentials |
|---------|------|-----|-------------|
| **PostgreSQL (Auth)** | 5432 | `localhost:5432` | User: postgres, Pass: postgres |
| **PostgreSQL (AI)** | 5433 | `localhost:5433` | User: postgres, Pass: postgres |
| **PostgreSQL (Tracking)** | 5434 | `localhost:5434` | User: postgres, Pass: postgres |
| **Redis** | 6379 | `localhost:6379` | N/A |
| **RabbitMQ** | 5672 | `localhost:5672` | User: guest, Pass: guest |
| **RabbitMQ Management** | 15672 | `http://localhost:15672` | User: guest, Pass: guest |
| **MinIO API** | 9000 | `localhost:9000` | User: minioadmin, Pass: minioadmin |
| **MinIO Console** | 9001 | `http://localhost:9001` | User: minioadmin, Pass: minioadmin |

### Admin Access Examples

```bash
# PostgreSQL CLI
docker exec -it postgres-auth psql -U postgres -d auth_db

# View RabbitMQ queues
# Open: http://localhost:15672
# Login: guest / guest

# Access MinIO file storage
# Open: http://localhost:9001
# Login: minioadmin / minioadmin

# View service logs
docker logs -f auth-service
docker logs -f tracking-service
```

---

## 📚 API Documentation

### Base URL

```
Frontend: http://localhost:8080
Direct Services: http://localhost:8081-8084
```

**All examples use the gateway URL** (recommended for frontend).

### Authentication Flow

```
1. POST /api/v1/auth/register → Get tokens
2. Use access_token in all requests
3. Header: Authorization: Bearer <access_token>
```

### Main API Endpoints

#### 🔐 Auth Service (Port 8081)
```
POST   /api/v1/auth/register          Register new user (teen/therapist)
POST   /api/v1/auth/login             Login user
GET    /api/v1/auth/me                Get current user profile
PATCH  /api/v1/auth/profile           Update profile
POST   /api/v1/auth/profile/avatar    Upload avatar
POST   /api/v1/auth/logout            Logout user
POST   /api/v1/auth/grants            Grant data access to therapist
GET    /api/v1/auth/grants            List granted access
GET    /api/v1/auth/grants/received   List received access
```

#### 🤖 AI Service (Port 8082)
```
POST   /api/v1/ai/sessions            Create chat session
GET    /api/v1/ai/sessions            List chat sessions
GET    /api/v1/ai/sessions/{id}       Get session with messages
POST   /api/v1/ai/sessions/{id}/messages   Send message to AI
```

#### 📊 Tracking Service (Port 8083)
```
POST   /api/v1/tracking/mood          Log mood
GET    /api/v1/tracking/mood          List mood logs
POST   /api/v1/tracking/sleep         Log sleep
GET    /api/v1/tracking/sleep         List sleep logs
POST   /api/v1/tracking/food          Log food
GET    /api/v1/tracking/food          List food logs
POST   /api/v1/tracking/diary         Create diary entry
GET    /api/v1/tracking/diary         List diary entries
GET    /api/v1/tracking/context       Get aggregated context
```

#### 📈 Dashboard Service (Port 8084)
```
GET    /api/v1/dashboard/summary      Aggregated summary
GET    /api/v1/dashboard/context/{id} Get user context
GET    /api/v1/dashboard/health       Health check
```

### Example API Call

```bash
# Register user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "role": "TEEN",
    "accountType": "TEEN",
    "school": "High School Name"
  }'

# Response:
# {
#   "userId": "uuid",
#   "profileId": "uuid",
#   "email": "user@example.com",
#   "fullName": "John Doe",
#   "role": "TEEN",
#   "accessToken": "eyJhbGciOiJIUzI1NiIs...",
#   "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
#   "expiresIn": 3600
# }
```

### Log Mood Example

```bash
# Get token first
export TOKEN="<accessToken>"

# Log mood
curl -X POST http://localhost:8080/api/v1/tracking/mood \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "moodScore": 7,
    "notes": "Feeling better today",
    "emotionTags": ["happy", "energetic"]
  }'
```

**Full API Documentation:** See [API.md](API.md)

---

## 🗄️ Database Access

### View Logs from Services

```powershell
# Specific service logs
docker logs auth-service
docker logs -f tracking-service  # Follow mode

# View all logs
docker-compose logs -f
```

### Access PostgreSQL Directly

```powershell
# Auth database
docker exec -it postgres-auth psql -U postgres -d auth_db

# AI database
docker exec -it postgres-ai psql -U postgres -d ai_db

# Tracking database
docker exec -it postgres-tracking psql -U postgres -d tracking_db

# Example queries:
# \dt                          -- List tables
# SELECT * FROM profiles;      -- View profiles
# SELECT * FROM mood_logs;     -- View mood logs
# \q                           -- Exit
```

### Database Schema Overview

**Auth Service Database (auth_db):**
- `users` - User accounts
- `profiles` - User profiles (TEEN/THERAPIST)
- `data_access_grants` - Sharing permissions

**AI Service Database (ai_db):**
- `chat_sessions` - Conversation sessions
- `chat_messages` - Messages in sessions

**Tracking Service Database (tracking_db):**
- `mood_logs` - Mood entries
- `sleep_logs` - Sleep tracking
- `food_logs` - Food intake
- `diary_entries` - Journal entries
- `streaks` - Habit streaks

### Data Sharing

The system supports **therapist access to patient data**:

```bash
# Grant access (patient action)
POST /api/v1/auth/grants
{ "granteeProfileId": "therapist-uuid" }

# Therapist can then view
GET /api/v1/tracking/context/{patientId}?days=7
```

---

## 🎨 Frontend Integration

### Configuration

```javascript
// .env or .env.local
VITE_API_URL=http://localhost:8080
VITE_API_TIMEOUT=30000
```

### API Client Setup (Fetch)

```javascript
const API_BASE = 'http://localhost:8080';

async function apiRequest(endpoint, options = {}) {
  const token = localStorage.getItem('accessToken');
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`);
  }

  return response.json();
}

// Usage
const user = await apiRequest('/api/v1/auth/me');
```

### API Client Setup (Axios)

```javascript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 30000,
});

// Add token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 (expired token)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### Common Frontend Functions

```javascript
// Authentication
async function register(userData) {
  return apiClient.post('/api/v1/auth/register', userData);
}

async function login(email, password) {
  const response = await apiClient.post('/api/v1/auth/login', {
    email,
    password,
  });
  localStorage.setItem('accessToken', response.data.accessToken);
  return response.data;
}

async function getCurrentUser() {
  return apiClient.get('/api/v1/auth/me');
}

// Health Tracking
async function logMood(moodData) {
  return apiClient.post('/api/v1/tracking/mood', moodData);
}

async function getMoodLogs(days = 7) {
  return apiClient.get(`/api/v1/tracking/mood?days=${days}`);
}

// AI Chat
async function createChatSession() {
  return apiClient.post('/api/v1/ai/sessions', {
    title: 'Mental Health Chat',
  });
}

async function sendAiMessage(sessionId, userMessage) {
  return apiClient.post(
    `/api/v1/ai/sessions/${sessionId}/messages`,
    { userMessage }
  );
}

// Dashboard
async function getDashboardSummary() {
  return apiClient.get('/api/v1/dashboard/summary');
}
```

**Full Frontend Guide:** See [FRONTEND_GUIDE.md](FRONTEND_GUIDE.md)

---

## 🔧 Useful Commands

### View Service Status

```powershell
# All containers
docker-compose ps

# Specific service
docker ps | findstr auth-service

# Health details
docker inspect thesis-backend-auth-service
```

### Manage Services

```powershell
# Stop all
docker-compose down

# Stop one service
docker-compose stop auth-service

# Restart one service
docker-compose restart auth-service

# View resource usage
docker stats

# Prune unused images/volumes
docker system prune
```

### Build & Compile (Local Development)

```bash
# Build all modules
./mvnw clean package

# Build specific module
./mvnw clean package -pl auth-service

# Run locally (without Docker)
./mvnw spring-boot:run -pl auth-service
```

### Monitor & Logs

```powershell
# Real-time logs
docker-compose logs -f

# Logs for one service
docker-compose logs -f auth-service

# Last 100 lines
docker logs --tail 100 auth-service

# Filter by error
docker-compose logs | findstr ERROR
```

---

## 🚨 Troubleshooting

### Containers Running but Services Unhealthy

**Problem:** All containers are up but health checks fail

**Solution:**
1. Wait 30-60 seconds (health checks take time on first start)
2. Check logs: `docker logs auth-service`
3. Verify database migrations: `docker logs postgres-auth | grep migration`

### Port Already in Use

```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F

# Or change port in docker-compose.yml
```

### Database Connection Errors

```powershell
# Reset database
docker-compose down -v
docker-compose up -d

# Check database logs
docker logs postgres-auth
docker logs postgres-tracking

# Verify database is running
docker exec postgres-auth psql -U postgres -c "\l"
```

### Service Health Check Failing

```powershell
# View detailed logs
docker logs -f auth-service | head -50

# Check if migrations ran
docker logs postgres-auth | grep "migration"

# Rebuild and restart
docker-compose down
docker-compose up -d --build
```

### Out of Disk Space

```powershell
# Clean up Docker
docker system prune -a

# Remove volumes (⚠️ Deletes all data)
docker-compose down -v
docker-compose up -d
```

### Network Issues Between Services

```powershell
# Check Docker network
docker network ls
docker network inspect thesis-backend_mhsa-network

# Test connectivity inside container
docker exec auth-service curl http://tracking-service:8083/actuator/health
```

---

## 📂 Project Structure

```
thesis-backend/
├── auth-service/              Microservice - User auth & profiles
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/      Database migrations
│   ├── pom.xml
│   └── Dockerfile
│
├── ai-service/                Microservice - AI chatbot
├── tracking-service/          Microservice - Health tracking
├── dashboard-service/         Microservice - BFF aggregator
│
├── shared-jwt/                Shared library - JWT utilities
├── shared-contracts/          Shared library - DTOs
│
├── nginx/                     API Gateway configuration
│   ├── nginx.conf
│   └── Dockerfile
│
├── docker-compose.yml         Container orchestration
├── pom.xml                    Maven parent POM
├── README.md                  This file (comprehensive guide)
├── ARCHITECTURE.md            System architecture details
├── API.md                     Full API reference
├── DEPLOYMENT.md              Production deployment guide
└── FRONTEND_GUIDE.md          Frontend integration
```

---

## 📋 Microservices Overview

### Auth Service (8081)
- **Role:** User authentication, profile management, data sharing
- **Database:** PostgreSQL (auth_db)
- **Key Features:**
  - JWT token generation
  - Role-based access control (TEEN/THERAPIST)
  - Avatar upload (MinIO)
  - Data access grants

### AI Service (8082)
- **Role:** Mental health chatbot using Google Gemini API
- **Database:** PostgreSQL (ai_db)
- **Key Features:**
  - Chat session management
  - Gemini API integration
  - Message history
  - Event publishing

### Tracking Service (8083)
- **Role:** Health data tracking
- **Database:** PostgreSQL (tracking_db)
- **Cache:** Redis
- **Key Features:**
  - Mood, sleep, food, diary logging
  - Streak tracking
  - Context aggregation
  - Data access control

### Dashboard Service (8084)
- **Role:** Backend For Frontend (BFF)
- **Database:** None (stateless)
- **Key Features:**
  - Parallel data aggregation
  - Health checks
  - Request orchestration

---

## 🔄 Key Features

### ✅ Implemented
- User profiles (Teen & Therapist)
- JWT authentication with refresh tokens
- AI chat with Google Gemini
- Health tracking (mood, sleep, food, diary)
- Data aggregation dashboard
- File storage (MinIO)
- Async messaging (RabbitMQ)
- Caching layer (Redis)
- API Gateway (Nginx)
- Data sharing (therapist access)
- Role-based access control

### ❌ Future Features
- Therapist directory
- Patient-therapist matching
- Appointment booking
- Real-time messaging
- Video consultation
- Social features
- Advanced analytics

---

## 🤝 Support

For issues or questions:
1. Check the **Troubleshooting** section above
2. Review `docker-compose logs` for error messages
3. Verify all containers are healthy: `docker-compose ps`
4. Check individual service logs: `docker logs [service-name]`

---

## 📄 License

This project is part of a Thesis for ACS - HCMUS.

---

## 📞 Contact

For technical support or contributions, contact the development team.

**Last Updated:** 2026-05-19

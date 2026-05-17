# ⚡ Quick Setup Guide

## One Command to Start Everything

```powershell
cd d:\StudyFiles\Thesis\thesis-backend
docker-compose up -d
```

Wait 2-3 minutes for all services to become healthy.

---

## Verify Services Are Running

```powershell
docker-compose ps
```

Expected output: All 11 containers showing "healthy" status

```
NAME                STATUS
auth-service        Healthy
ai-service          Healthy
tracking-service    Healthy
dashboard-service   Healthy
nginx               Healthy
postgres-auth       Healthy
postgres-ai         Healthy
postgres-tracking   Healthy
redis               Healthy
rabbitmq            Healthy
minio               Healthy
```

---

## Test API

```bash
# Test via gateway (recommended for frontend)
curl http://localhost:8080/health
# Response: healthy

# Test individual services
curl http://localhost:8081/actuator/health      # auth
curl http://localhost:8082/actuator/health      # ai
curl http://localhost:8083/actuator/health      # tracking
curl http://localhost:8084/api/v1/dashboard/health  # dashboard
```

---

## Available Ports

| Service | Port | Purpose |
|---------|------|---------|
| **Nginx Gateway** | 8080 | Main API entry point |
| **Auth Service** | 8081 | Authentication & profiles |
| **AI Service** | 8082 | AI chatbot |
| **Tracking Service** | 8083 | Health tracking |
| **Dashboard Service** | 8084 | Data aggregation |
| **RabbitMQ Web** | 15672 | Message queue UI |
| **MinIO Console** | 9001 | File storage UI |
| **PostgreSQL (Auth)** | 5432 | Database |
| **PostgreSQL (AI)** | 5433 | Database |
| **PostgreSQL (Tracking)** | 5434 | Database |
| **Redis** | 6379 | Cache |

---

## Common Tasks

### View Service Logs
```powershell
docker-compose logs -f auth-service
docker-compose logs -f tracking-service
docker-compose logs -f [service-name]
```

### Rebuild After Code Changes
```powershell
docker-compose down
docker-compose up -d --build
```

### Stop All Services
```powershell
docker-compose down
```

### Delete All Data (⚠️ Careful!)
```powershell
docker-compose down -v
docker-compose up -d
```

### Access RabbitMQ Management
```
URL: http://localhost:15672
Username: guest
Password: guest
```

### Access MinIO Console
```
URL: http://localhost:9001
Username: minioadmin
Password: minioadmin
```

---

## Frontend Integration

### Using API Gateway (Recommended)
```javascript
const API_URL = "http://localhost:8080";

// Register
fetch(`${API_URL}/api/v1/auth/register`, {...})

// Login
fetch(`${API_URL}/api/v1/auth/login`, {...})

// Get Dashboard
fetch(`${API_URL}/api/v1/dashboard/summary`, {
  headers: { Authorization: `Bearer ${token}` }
})
```

### Using Direct Service URLs (For Development)
```javascript
const AUTH_URL = "http://localhost:8081";
const AI_URL = "http://localhost:8082";
const TRACKING_URL = "http://localhost:8083";
const DASHBOARD_URL = "http://localhost:8084";
```

---

## Troubleshooting

### Services show "unhealthy"
**Wait longer** - Health checks take 30-60 seconds on first start

### Port already in use
```powershell
# Kill process using port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Container keeps restarting
```powershell
docker logs [container-name]
# Check for errors in logs
```

### Database connection errors
```powershell
# Reset everything
docker-compose down -v
docker-compose up -d
```

---

## Next Steps

1. ✅ All services running
2. ✅ Test health endpoints
3. → Connect your frontend to `http://localhost:8080`
4. → Start using the APIs!

See [README.md](README.md) for full API documentation.

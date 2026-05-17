# ⚡ Chỉ Cần Docker - Quick Start

## TL;DR - Tóm Tắt Ngắn Gọn

```bash
# Đó là tất cả bạn cần!
cd d:\StudyFiles\Thesis\thesis-backend
docker-compose up -d

# Chờ 2-3 phút... XONG! ✅
```

---

## ❓ Câu Hỏi: Phải chạy app backend riêng không?

### 🔴 KHÔNG! (Don't!)

```bash
# ❌ KHÔNG CẦN cái này
.\mvnw spring-boot:run -pl auth-service

# ❌ KHÔNG CẦN mở IDE

# ❌ KHÔNG CẦN build JAR thủ công
```

### 🟢 CHỈ CẦN Docker

```bash
# ✅ CHỈNH CẦN cái này
docker-compose up -d
```

---

## 🔄 Docker-Compose Sẽ Làm Gì?

```
docker-compose up -d
    ↓
1. Build JAR files (mvn clean package)
2. Build Docker images
3. Start 11 containers
4. Run health checks
5. All services ready ✅
```

---

## ⏱️ Thời Gian

| Lần | Thời Gian | Lý Do |
|-----|-----------|-------|
| **Lần Đầu** | 3-5 phút | Download Maven dependencies, build JARs |
| **Lần Kị** | 1-2 phút | Đã có cache, chỉ khởi động containers |

---

## ✅ Các Bước Chi Tiết

### 1️⃣ Mở Terminal

```bash
# Windows PowerShell hoặc CMD
cd d:\StudyFiles\Thesis\thesis-backend
```

### 2️⃣ Chạy Docker Compose

```bash
docker-compose up -d
```

### 3️⃣ Chờ Services Healthy

```bash
# Xem status
docker-compose ps

# Chờ tất cả show "healthy"
# (khoảng 2-3 phút)
```

### 4️⃣ Verify Backend Ready

```bash
# Test API
curl http://localhost:8080/health

# Response: "healthy"
```

---

## 📦 Những Gì Được Start

### 11 Containers

```
Infrastructure:
✓ postgres-auth       (Database - Port 5432)
✓ postgres-ai         (Database - Port 5433)
✓ postgres-tracking   (Database - Port 5434)
✓ redis               (Cache - Port 6379)
✓ rabbitmq            (Message Queue - Port 5672)
✓ minio               (File Storage - Port 9000-9001)
✓ nginx               (API Gateway - Port 8080)

Microservices:
✓ auth-service        (Port 8081)
✓ ai-service          (Port 8082)
✓ tracking-service    (Port 8083)
✓ dashboard-service   (Port 8084)
```

---

## 🔌 Frontend Connection

```javascript
// Frontend chỉ cần biết cái này:
const API_BASE = 'http://localhost:8080';

// Tất cả endpoints:
/api/v1/auth/*
/api/v1/ai/*
/api/v1/tracking/*
/api/v1/dashboard/*
```

---

## 📋 Common Commands

### Start
```bash
docker-compose up -d
```

### Stop
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f auth-service
docker-compose logs -f tracking-service
```

### Restart After Code Changes
```bash
docker-compose down
docker-compose up -d --build
```

### Reset Everything
```bash
docker-compose down -v
docker-compose up -d
```

---

## ✨ Tại Sao Chỉ Docker?

### Docker-Compose Advantages

| Điểm | Lợi Ích |
|------|---------|
| **1 Lệnh** | `docker-compose up -d` là xong |
| **Tất Cả Cùng Lúc** | 11 services khởi động đồng thời |
| **Health Check** | Tự động kiểm tra mỗi service |
| **Reproducible** | Ai chạy cũng như nhau |
| **Prod-like** | Giống environment production |
| **Isolate** | Không ảnh hưởng máy local |
| **Easy Debug** | `docker logs <container>` |

---

## 🚨 Troubleshooting

### Containers không healthy?
```bash
# Chờ lâu hơn (3-5 phút)
docker-compose ps

# Xem chi tiết lỗi
docker logs auth-service
docker logs tracking-service
```

### Port đã bị dùng?
```bash
# Tìm process dùng port 8080
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F
```

### Docker out of disk space?
```bash
# Clear unused images
docker system prune

# Remove all containers
docker-compose down -v
```

---

## 🎉 Ready!

```
✅ Backend hoàn toàn ready
✅ Frontend có thể kết nối
✅ Không cần chạy app riêng
✅ All services running
✅ All databases ready
✅ Health checks passing

Type: docker-compose up -d
Wait: 2-3 minutes
Done: 100% ready! 🚀
```

---

## 📚 Chi Tiết Hơn?

Xem các files:
- [README.md](README.md) - Full overview
- [SETUP.md](SETUP.md) - Detailed setup
- [API.md](API.md) - API reference
- [FRONTEND_GUIDE.md](FRONTEND_GUIDE.md) - Frontend integration

---

## ⚠️ Tóm Tắt

| Câu Hỏi | Trả Lời |
|--------|--------|
| Phải chạy app backend riêng? | ❌ KHÔNG |
| Phải chạy Spring Boot? | ❌ KHÔNG |
| Phải build JAR? | ❌ KHÔNG |
| Chỉ cần Docker? | ✅ CÓ |
| Một lệnh là đủ? | ✅ CÓ |
| `docker-compose up -d` | ✅ YES! |

---

**That's it! Enjoy! 🎉**

# 📚 API Documentation

## Base URLs

| Environment | URL |
|---|---|
| **Production (via Gateway)** | http://localhost:8080 |
| **Development (Direct)** | http://localhost:8081 (auth), 8082 (ai), etc. |

All examples use the gateway URL (recommended for frontend).

---

## Authentication

### 1. Register User

**Endpoint:** `POST /api/v1/auth/register`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+84912345678",
  "dob": "2005-06-15",
  "gender": "male",
  "role": "TEEN",
  "accountType": "TEEN",
  "school": "High School Name"
}
```

For Therapist:
```json
{
  "email": "therapist@example.com",
  "password": "password123",
  "fullName": "Dr. Jane Smith",
  "role": "THERAPIST",
  "accountType": "THERAPIST",
  "specialization": "CBT, Anxiety",
  "bio": "Licensed therapist with 10+ years experience",
  "yearsOfExperience": 10,
  "consultationFee": 500000
}
```

**Response:** `200 OK`
```json
{
  "userId": "uuid",
  "profileId": "uuid",
  "email": "user@example.com",
  "fullName": "John Doe",
  "role": "TEEN",
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

---

### 2. Login

**Endpoint:** `POST /api/v1/auth/login`

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "userId": "uuid",
  "profileId": "uuid",
  "email": "user@example.com",
  "fullName": "John Doe",
  "role": "TEEN",
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

---

### 3. Get Current User

**Endpoint:** `GET /api/v1/auth/me`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`
```json
{
  "userId": "uuid",
  "profileId": "uuid",
  "email": "user@example.com",
  "fullName": "John Doe",
  "role": "TEEN",
  "avatarUrl": "https://minio.../avatar.jpg",
  "dateOfBirth": "2005-06-15",
  "phoneNumber": "+84912345678",
  "gender": "male"
}
```

---

### 4. Update Profile

**Endpoint:** `PATCH /api/v1/auth/profile`

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request:**
```json
{
  "fullName": "John Doe Updated",
  "phoneNumber": "+84987654321",
  "gender": "male"
}
```

For Therapist:
```json
{
  "fullName": "Dr. Jane Smith",
  "specialization": "CBT, Anxiety, Depression",
  "bio": "Updated bio",
  "yearsOfExperience": 11,
  "consultationFee": 600000
}
```

**Response:** `200 OK` (Same as Get Current User)

---

### 5. Upload Avatar

**Endpoint:** `POST /api/v1/auth/profile/avatar`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Body:** (multipart/form-data)
```
file: <image-file>
```

**Response:** `200 OK`
```json
{
  "url": "https://minio.../avatars/uuid.jpg"
}
```

---

### 6. Logout

**Endpoint:** `POST /api/v1/auth/logout`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`
```json
{
  "message": "Logged out successfully"
}
```

---

## Data Sharing (Therapist Access)

### Grant Data Access

**Endpoint:** `POST /api/v1/auth/grants`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "granteeProfileId": "therapist-uuid"
}
```

**Response:** `200 OK`
```json
{
  "grantId": "uuid",
  "granterProfileId": "your-uuid",
  "granteeProfileId": "therapist-uuid",
  "status": "ACTIVE",
  "grantedAt": "2026-05-16T20:00:00Z",
  "expiresAt": null
}
```

---

### List Granted Access

**Endpoint:** `GET /api/v1/auth/grants`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`
```json
[
  {
    "grantId": "uuid",
    "granteeProfileId": "therapist-uuid",
    "granteeFullName": "Dr. Jane Smith",
    "status": "ACTIVE",
    "grantedAt": "2026-05-16T20:00:00Z"
  }
]
```

---

### List Received Access

**Endpoint:** `GET /api/v1/auth/grants/received`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`
```json
[
  {
    "grantId": "uuid",
    "granterProfileId": "patient-uuid",
    "granterFullName": "John Doe",
    "status": "ACTIVE",
    "grantedAt": "2026-05-16T20:00:00Z"
  }
]
```

---

## Health Tracking

### 1. Create Mood Log

**Endpoint:** `POST /api/v1/tracking/mood`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "moodScore": 7,
  "notes": "Feeling better today",
  "emotionTags": ["happy", "energetic"]
}
```

**Response:** `201 Created`
```json
{
  "id": "uuid",
  "profileId": "your-uuid",
  "moodScore": 7,
  "notes": "Feeling better today",
  "emotionTags": ["happy", "energetic"],
  "loggedAt": "2026-05-16T20:00:00Z"
}
```

---

### 2. Get Mood Logs

**Endpoint:** `GET /api/v1/tracking/mood?days=7`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Query Parameters:**
- `days` (optional): Number of days to retrieve (default: 7)

**Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "moodScore": 7,
    "notes": "Feeling better",
    "loggedAt": "2026-05-16T20:00:00Z"
  }
]
```

---

### 3. Create Sleep Log

**Endpoint:** `POST /api/v1/tracking/sleep`

**Request:**
```json
{
  "durationMinutes": 480,
  "quality": "GOOD",
  "notes": "Slept well"
}
```

**Response:** `201 Created`

---

### 4. Create Food Log

**Endpoint:** `POST /api/v1/tracking/food`

**Request:**
```json
{
  "mealType": "BREAKFAST",
  "foodItems": ["eggs", "toast", "coffee"],
  "calories": 350,
  "notes": "Healthy breakfast"
}
```

**Response:** `201 Created`

---

### 5. Create Diary Entry

**Endpoint:** `POST /api/v1/tracking/diary`

**Request:**
```json
{
  "title": "Today's Reflection",
  "content": "Long form diary entry text...",
  "mood": 6,
  "attachments": ["url1", "url2"]
}
```

**Response:** `201 Created`

---

## AI Chat Service

### 1. Create Chat Session

**Endpoint:** `POST /api/v1/ai/sessions`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "title": "Mental Health Chat"
}
```

**Response:** `201 Created`
```json
{
  "sessionId": "uuid",
  "profileId": "your-uuid",
  "title": "Mental Health Chat",
  "createdAt": "2026-05-16T20:00:00Z"
}
```

---

### 2. Get Chat Sessions

**Endpoint:** `GET /api/v1/ai/sessions`

**Response:** `200 OK`
```json
[
  {
    "sessionId": "uuid",
    "title": "Mental Health Chat",
    "createdAt": "2026-05-16T20:00:00Z",
    "lastMessageAt": "2026-05-16T20:15:00Z"
  }
]
```

---

### 3. Send Message to AI

**Endpoint:** `POST /api/v1/ai/sessions/{sessionId}/messages`

**Request:**
```json
{
  "userMessage": "I'm feeling anxious today"
}
```

**Response:** `200 OK`
```json
{
  "sessionId": "uuid",
  "messages": [
    {
      "id": "uuid",
      "sender": "USER",
      "content": "I'm feeling anxious today",
      "timestamp": "2026-05-16T20:00:00Z"
    },
    {
      "id": "uuid",
      "sender": "AI",
      "content": "I understand. Anxiety can be challenging... [AI response]",
      "timestamp": "2026-05-16T20:00:05Z"
    }
  ]
}
```

---

## Dashboard & Analytics

### 1. Get Dashboard Summary

**Endpoint:** `GET /api/v1/dashboard/summary`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`
```json
{
  "profileId": "your-uuid",
  "auth": {
    "userId": "uuid",
    "fullName": "John Doe",
    "role": "TEEN"
  },
  "tracking": {
    "recentMoodLogs": [...],
    "recentSleepLogs": [...],
    "recentFoodLogs": [...]
  },
  "ai": {
    "totalSessions": 5,
    "lastSessionMessages": 12
  },
  "latencyMs": 245
}
```

---

### 2. Get User Context

**Endpoint:** `GET /api/v1/dashboard/context/{profileId}?days=7`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Query Parameters:**
- `days` (optional): Number of days (default: 7)

**Response:** `200 OK`
```json
{
  "profileId": "uuid",
  "period": "7 days",
  "mood": {
    "average": 6.5,
    "entries": [7, 6, 5, 7, 6, 7, 6]
  },
  "sleep": {
    "average_minutes": 450,
    "entries": [480, 420, 450, 480, 480]
  },
  "food": {
    "entries_count": 14,
    "recent": [...]
  },
  "diary": {
    "entries_count": 7,
    "recent": [...]
  }
}
```

---

### 3. Health Check

**Endpoint:** `GET /api/v1/dashboard/health`

**Response:** `200 OK`
```json
{
  "auth-service": true,
  "tracking-service": true,
  "ai-service": true
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid request",
  "message": "Email is required"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "You don't have access to this resource"
}
```

### 404 Not Found
```json
{
  "error": "Not found",
  "message": "Resource not found"
}
```

### 500 Server Error
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred"
}
```

---

## Rate Limiting

Currently no rate limiting is enforced. This may be added in production.

---

## Authentication Token Format

Tokens are JWT (JSON Web Tokens):
```
Header: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.9rWHwbvjiO7ZdFC_X1CJH7dBPewWXoU3XZqylRG112E
```

**Token Expiration:** 1 hour (3600 seconds)

**Refresh Token:** Use to get new access token before expiration

---

## Rate Limits & Quotas

| Feature | Limit |
|---------|-------|
| API Requests | No limit (for now) |
| File Upload | 10 MB max |
| Message Length | 4000 characters |
| Chat History | Unlimited |

---

## Examples

### Complete Login & Use Dashboard Flow

```bash
# 1. Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123","fullName":"User","role":"TEEN","accountType":"TEEN"}'

# 2. Get token from response
export TOKEN="<accessToken>"

# 3. Get current user
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/auth/me

# 4. Log mood
curl -X POST http://localhost:8080/api/v1/tracking/mood \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"moodScore":7,"notes":"Feeling good"}'

# 5. Get dashboard
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/dashboard/summary
```

---

## WebSocket (Future)

Real-time features like live chat notifications may use WebSocket in future versions.

Planned endpoint: `ws://localhost:8080/api/v1/notifications`

# Frontend Integration Guide

## API Overview

All frontend requests go through **Nginx Gateway** at `http://localhost:8080` (or your API URL).

### Base URL Configuration

```javascript
// For local development
const API_BASE_URL = "http://localhost:8080";

// For production
const API_BASE_URL = "https://api.yourdomain.com";
```

---

## Authentication Flow

### 1. Register New User

**Endpoint:** `POST /api/v1/auth/register`

```javascript
const register = async (email, password, name) => {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email,
      password,
      name,
    }),
  });

  const data = await response.json();
  if (data.success) {
    localStorage.setItem("token", data.data.token);
    localStorage.setItem("profileId", data.data.profileId);
    return data.data;
  }
  throw new Error(data.error);
};
```

### 2. Login

**Endpoint:** `POST /api/v1/auth/login`

```javascript
const login = async (email, password) => {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email,
      password,
    }),
  });

  const data = await response.json();
  if (data.success) {
    localStorage.setItem("token", data.data.token);
    localStorage.setItem("profileId", data.data.profileId);
    return data.data;
  }
  throw new Error(data.error);
};
```

### 3. Get Current User

**Endpoint:** `GET /api/v1/auth/me`

```javascript
const getCurrentUser = async (token) => {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.json();
};
```

### 4. Logout

**Endpoint:** `POST /api/v1/auth/logout`

```javascript
const logout = async (token) => {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/logout`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (response.ok) {
    localStorage.removeItem("token");
    localStorage.removeItem("profileId");
  }
};
```

---

## Core API Endpoints

### Dashboard (Main BFF)

**GET** `/api/v1/dashboard/summary`
- Get aggregated dashboard data (mood, sleep, diary, streaks, chat stats)
- Returns: `{profileId, auth, tracking, ai, latencyMs}`

```javascript
const getDashboard = async (token) => {
  const response = await fetch(`${API_BASE_URL}/api/v1/dashboard/summary`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.json();
};
```

**GET** `/api/v1/dashboard/context/{profileId}?days=7`
- Get user context (mood trends, sleep patterns, food, diary summary)
- Query params: `days` (default: 7)

```javascript
const getUserContext = async (token, profileId, days = 7) => {
  const response = await fetch(
    `${API_BASE_URL}/api/v1/dashboard/context/${profileId}?days=${days}`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return response.json();
};
```

---

## Tracking Service APIs

### Mood Logs

**POST** `/api/v1/tracking/moods`
```javascript
const createMoodLog = async (token, positivityScore, note) => {
  return fetch(`${API_BASE_URL}/api/v1/tracking/moods`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ positivityScore, note }),
  }).then(r => r.json());
};
```

**GET** `/api/v1/tracking/moods`
```javascript
const getMoods = async (token) => {
  return fetch(`${API_BASE_URL}/api/v1/tracking/moods`, {
    headers: { Authorization: `Bearer ${token}` },
  }).then(r => r.json());
};
```

**GET** `/api/v1/tracking/moods/{id}`
```javascript
const getMood = async (token, id) => {
  return fetch(`${API_BASE_URL}/api/v1/tracking/moods/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  }).then(r => r.json());
};
```

**PUT** `/api/v1/tracking/moods/{id}`
```javascript
const updateMood = async (token, id, positivityScore, note) => {
  return fetch(`${API_BASE_URL}/api/v1/tracking/moods/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ positivityScore, note }),
  }).then(r => r.json());
};
```

**DELETE** `/api/v1/tracking/moods/{id}`
```javascript
const deleteMood = async (token, id) => {
  return fetch(`${API_BASE_URL}/api/v1/tracking/moods/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
};
```

### Sleep Logs

**POST** `/api/v1/tracking/sleep`
```javascript
const createSleepLog = async (token, bedTime, wakeTime, sleepQuality, note) => {
  return fetch(`${API_BASE_URL}/api/v1/tracking/sleep`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ bedTime, wakeTime, sleepQuality, note }),
  }).then(r => r.json());
};
```

**GET** `/api/v1/tracking/sleep`
**GET** `/api/v1/tracking/sleep/{id}`
**PUT** `/api/v1/tracking/sleep/{id}`
**DELETE** `/api/v1/tracking/sleep/{id}`

### Food Logs

**POST** `/api/v1/tracking/food`
```javascript
const createFoodLog = async (token, waterGlasses, foodDescription, satietyLevel) => {
  return fetch(`${API_BASE_URL}/api/v1/tracking/food`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ waterGlasses, foodDescription, satietyLevel }),
  }).then(r => r.json());
};
```

**GET** `/api/v1/tracking/food`
**GET** `/api/v1/tracking/food/{id}`
**PUT** `/api/v1/tracking/food/{id}`
**DELETE** `/api/v1/tracking/food/{id}`

### Diary Entries

**POST** `/api/v1/tracking/diary` (with file upload)
```javascript
const createDiaryEntry = async (token, title, content, moodTag, positivityScore, files) => {
  const formData = new FormData();
  formData.append("title", title);
  formData.append("content", content);
  formData.append("moodTag", moodTag);
  formData.append("positivityScore", positivityScore);
  
  files?.forEach(file => formData.append("files", file));

  return fetch(`${API_BASE_URL}/api/v1/tracking/diary`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body: formData,
  }).then(r => r.json());
};
```

**GET** `/api/v1/tracking/diary`
**GET** `/api/v1/tracking/diary/{id}`
**PUT** `/api/v1/tracking/diary/{id}`
**DELETE** `/api/v1/tracking/diary/{id}`

### Streaks

**GET** `/api/v1/tracking/streaks`
```javascript
const getStreaks = async (token) => {
  return fetch(`${API_BASE_URL}/api/v1/tracking/streaks`, {
    headers: { Authorization: `Bearer ${token}` },
  }).then(r => r.json());
};
```

---

## AI Service APIs

### Chat

**POST** `/api/v1/ai/chat/send`
```javascript
const sendChatMessage = async (token, sessionId, message) => {
  return fetch(`${API_BASE_URL}/api/v1/ai/chat/send`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ sessionId, message }),
  }).then(r => r.json());
};
```

**GET** `/api/v1/ai/chat/sessions`
```javascript
const getChatSessions = async (token) => {
  return fetch(`${API_BASE_URL}/api/v1/ai/chat/sessions`, {
    headers: { Authorization: `Bearer ${token}` },
  }).then(r => r.json());
};
```

**GET** `/api/v1/ai/chat/history/{sessionId}`
```javascript
const getChatHistory = async (token, sessionId) => {
  return fetch(`${API_BASE_URL}/api/v1/ai/chat/history/${sessionId}`, {
    headers: { Authorization: `Bearer ${token}` },
  }).then(r => r.json());
};
```

---

## API Client Setup (React Example)

### Create Axios Instance

```javascript
// api/client.js
import axios from "axios";

const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

// Add token to all requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiration
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### Create API Service

```javascript
// api/services.js
import apiClient from "./client";

export const authAPI = {
  register: (email, password, name) =>
    apiClient.post("/api/v1/auth/register", { email, password, name }),
  login: (email, password) =>
    apiClient.post("/api/v1/auth/login", { email, password }),
  logout: () => apiClient.post("/api/v1/auth/logout"),
  getCurrentUser: () => apiClient.get("/api/v1/auth/me"),
};

export const dashboardAPI = {
  getSummary: () => apiClient.get("/api/v1/dashboard/summary"),
  getContext: (profileId, days = 7) =>
    apiClient.get(`/api/v1/dashboard/context/${profileId}`, { params: { days } }),
  getHealth: () => apiClient.get("/api/v1/dashboard/health"),
};

export const trackingAPI = {
  // Moods
  createMood: (data) => apiClient.post("/api/v1/tracking/moods", data),
  getMoods: () => apiClient.get("/api/v1/tracking/moods"),
  getMood: (id) => apiClient.get(`/api/v1/tracking/moods/${id}`),
  updateMood: (id, data) => apiClient.put(`/api/v1/tracking/moods/${id}`, data),
  deleteMood: (id) => apiClient.delete(`/api/v1/tracking/moods/${id}`),

  // Sleep
  createSleep: (data) => apiClient.post("/api/v1/tracking/sleep", data),
  getSleepLogs: () => apiClient.get("/api/v1/tracking/sleep"),
  getSleep: (id) => apiClient.get(`/api/v1/tracking/sleep/${id}`),
  updateSleep: (id, data) => apiClient.put(`/api/v1/tracking/sleep/${id}`, data),
  deleteSleep: (id) => apiClient.delete(`/api/v1/tracking/sleep/${id}`),

  // Food
  createFood: (data) => apiClient.post("/api/v1/tracking/food", data),
  getFoodLogs: () => apiClient.get("/api/v1/tracking/food"),
  getFood: (id) => apiClient.get(`/api/v1/tracking/food/${id}`),
  updateFood: (id, data) => apiClient.put(`/api/v1/tracking/food/${id}`, data),
  deleteFood: (id) => apiClient.delete(`/api/v1/tracking/food/${id}`),

  // Diary
  createDiary: (data) => apiClient.post("/api/v1/tracking/diary", data),
  getDiaryEntries: () => apiClient.get("/api/v1/tracking/diary"),
  getDiary: (id) => apiClient.get(`/api/v1/tracking/diary/${id}`),
  updateDiary: (id, data) => apiClient.put(`/api/v1/tracking/diary/${id}`, data),
  deleteDiary: (id) => apiClient.delete(`/api/v1/tracking/diary/${id}`),

  // Streaks
  getStreaks: () => apiClient.get("/api/v1/tracking/streaks"),
};

export const aiAPI = {
  sendMessage: (data) => apiClient.post("/api/v1/ai/chat/send", data),
  getSessions: () => apiClient.get("/api/v1/ai/chat/sessions"),
  getHistory: (sessionId) => apiClient.get(`/api/v1/ai/chat/history/${sessionId}`),
};
```

### Use in React Component

```javascript
// components/Dashboard.js
import React, { useEffect, useState } from "react";
import { dashboardAPI } from "../api/services";

const Dashboard = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardAPI
      .getSummary()
      .then((response) => setData(response.data.data))
      .catch((error) => console.error("Error:", error))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h1>Dashboard</h1>
      <p>Latest Mood: {data?.tracking?.latestMood}</p>
      <p>Sleep Time: {data?.tracking?.avgSleepMinutes}min</p>
      <p>Streak: {data?.tracking?.currentStreak} days</p>
      <p>Chat Sessions: {data?.ai?.totalSessions}</p>
      <p>API Response Time: {data?.latencyMs}ms</p>
    </div>
  );
};

export default Dashboard;
```

---

## Environment Variables (.env)

Create `.env.local` in your frontend project:

```bash
# For local development
REACT_APP_API_URL=http://localhost:8080

# For production
# REACT_APP_API_URL=https://api.yourdomain.com
```

---

## CORS Configuration

The backend allows requests from:
- `http://localhost:3000` (React dev)
- `http://localhost:8000` (Vue dev)
- `http://localhost:19006` (Expo/React Native)
- Production: Configure in environment

Nginx automatically handles CORS headers.

---

## Error Handling

### Standard Error Response

```json
{
  "success": false,
  "error": "Authentication failed",
  "statusCode": 401
}
```

### Handle Errors in Frontend

```javascript
const handleApiError = (error) => {
  if (error.response?.status === 401) {
    // Redirect to login
    window.location.href = "/login";
  } else if (error.response?.status === 403) {
    // Show permission denied
    alert("You don't have permission to access this resource");
  } else if (error.response?.status === 404) {
    // Show not found
    alert("Resource not found");
  } else {
    // Show generic error
    alert(error.response?.data?.error || "An error occurred");
  }
};
```

---

## Real-time Features (Optional - Phase 5.5)

The backend supports WebSocket for real-time updates. Add to `.env`:

```bash
REACT_APP_WS_URL=ws://localhost:8080/ws
```

---

## Testing with Postman/Insomnia

1. **Import OpenAPI spec:**
   - Auth: `http://localhost:8081/v3/api-docs`
   - AI: `http://localhost:8082/v3/api-docs`
   - Tracking: `http://localhost:8083/v3/api-docs`
   - Dashboard: `http://localhost:8084/v3/api-docs`

2. **Create environment variables:**
   ```
   api_url: http://localhost:8080
   token: <copy from login response>
   profile_id: <copy from register response>
   ```

3. **Test endpoints** with pre-configured requests

---

## Performance Tips

1. **Cache strategies:**
   - Store user profile in localStorage
   - Cache dashboard data for 5 minutes
   - Invalidate cache on logout

2. **Request optimization:**
   - Use `GET /api/v1/dashboard/summary` instead of multiple endpoint calls
   - Batch updates where possible
   - Use pagination for long lists

3. **File uploads:**
   - Compress images before upload
   - Set file size limits (max 10MB)
   - Show upload progress

---

## Troubleshooting

### CORS Error
```
Access to XMLHttpRequest blocked by CORS policy
```
**Solution:** Ensure `API_BASE_URL` doesn't include `/api/v1/`

### 401 Unauthorized
**Solution:** Check token in localStorage, verify it's not expired

### 404 Not Found
**Solution:** Check API_BASE_URL, verify service is running

### Network Timeout
**Solution:** Increase timeout in axios config
```javascript
apiClient.defaults.timeout = 10000; // 10 seconds
```

---

## Example Full App Flow

```javascript
// 1. Register
const user = await authAPI.register("user@example.com", "pass123", "John");

// 2. Get dashboard
const dashboard = await dashboardAPI.getSummary();

// 3. Create mood entry
await trackingAPI.createMood({ positivityScore: 8, note: "Feeling great" });

// 4. Send AI chat
await aiAPI.sendMessage({ sessionId: "abc", message: "I feel anxious" });

// 5. View updated context
const context = await dashboardAPI.getContext(user.profileId, 7);

// 6. Logout
await authAPI.logout();
```

---

## Support & Debugging

Enable debug logging:

```javascript
// In main.js/index.js
if (process.env.NODE_ENV === "development") {
  window.DEBUG = true;
  apiClient.interceptors.response.use((response) => {
    console.log("API Response:", response.config.url, response.data);
    return response;
  });
}
```

Check backend logs:
```bash
docker-compose logs -f dashboard-service
docker-compose logs -f tracking-service
```

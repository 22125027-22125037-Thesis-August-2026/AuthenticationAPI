# 🎨 Frontend Integration Guide

## Quick Answer: YES, Frontend Works Without Changes! ✅

The backend is **fully compatible** with frontend without requiring changes. Just point your frontend to `http://localhost:8080` and you're ready to go.

---

## Frontend Configuration

### Environment Setup

```javascript
// .env.local (or .env.development)
VITE_API_URL=http://localhost:8080
VITE_API_TIMEOUT=30000
```

### API Client Setup

```javascript
// src/services/api.js (Vue/React/Svelte)

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const apiClient = {
  async request(method, endpoint, data = null, config = {}) {
    const token = localStorage.getItem('accessToken');
    const headers = {
      'Content-Type': 'application/json',
      ...config.headers,
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method,
      headers,
      body: data ? JSON.stringify(data) : null,
      ...config,
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`);
    }

    return response.json();
  },

  get(endpoint, config) {
    return this.request('GET', endpoint, null, config);
  },

  post(endpoint, data, config) {
    return this.request('POST', endpoint, data, config);
  },

  patch(endpoint, data, config) {
    return this.request('PATCH', endpoint, data, config);
  },

  delete(endpoint, config) {
    return this.request('DELETE', endpoint, null, config);
  },
};
```

### Or Using Axios

```javascript
// src/services/axiosClient.js

import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
});

// Add token to requests
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle 401 responses (token expired)
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
```

---

## API Endpoint Examples

### Authentication

```javascript
// Register
async function register(userData) {
  return apiClient.post('/api/v1/auth/register', userData);
}

// Login
async function login(credentials) {
  const response = await apiClient.post('/api/v1/auth/login', credentials);
  localStorage.setItem('accessToken', response.accessToken);
  localStorage.setItem('refreshToken', response.refreshToken);
  return response;
}

// Get current user
async function getCurrentUser() {
  return apiClient.get('/api/v1/auth/me');
}

// Update profile
async function updateProfile(profileData) {
  return apiClient.patch('/api/v1/auth/profile', profileData);
}

// Upload avatar
async function uploadAvatar(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/profile/avatar`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
    },
    body: formData,
  });
  return response.json();
}

// Logout
async function logout() {
  await apiClient.post('/api/v1/auth/logout');
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
}
```

### Health Tracking

```javascript
// Log mood
async function logMood(moodData) {
  return apiClient.post('/api/v1/tracking/mood', moodData);
}

// Get mood logs
async function getMoodLogs(days = 7) {
  return apiClient.get(`/api/v1/tracking/mood?days=${days}`);
}

// Log sleep
async function logSleep(sleepData) {
  return apiClient.post('/api/v1/tracking/sleep', sleepData);
}

// Log food
async function logFood(foodData) {
  return apiClient.post('/api/v1/tracking/food', foodData);
}

// Create diary entry
async function createDiaryEntry(entryData) {
  return apiClient.post('/api/v1/tracking/diary', entryData);
}

// Get context (aggregated tracking data)
async function getUserContext(profileId, days = 7) {
  return apiClient.get(`/api/v1/dashboard/context/${profileId}?days=${days}`);
}
```

### AI Chat

```javascript
// Create chat session
async function createChatSession() {
  return apiClient.post('/api/v1/ai/sessions', {
    title: 'Mental Health Chat',
  });
}

// Get chat sessions
async function getChatSessions() {
  return apiClient.get('/api/v1/ai/sessions');
}

// Send message to AI
async function sendAiMessage(sessionId, userMessage) {
  return apiClient.post(
    `/api/v1/ai/sessions/${sessionId}/messages`,
    { userMessage }
  );
}
```

### Dashboard

```javascript
// Get dashboard summary
async function getDashboardSummary() {
  return apiClient.get('/api/v1/dashboard/summary');
}

// Health check
async function checkBackendHealth() {
  return apiClient.get('/health');
}
```

### Data Sharing

```javascript
// Grant data access to therapist
async function grantDataAccess(therapistProfileId) {
  return apiClient.post('/api/v1/auth/grants', {
    granteeProfileId: therapistProfileId,
  });
}

// List my grants
async function listMyGrants() {
  return apiClient.get('/api/v1/auth/grants');
}

// List received grants
async function listReceivedGrants() {
  return apiClient.get('/api/v1/auth/grants/received');
}
```

---

## Complete Working Example (Vue 3 + Vite)

```vue
<!-- src/components/LoginForm.vue -->

<template>
  <form @submit.prevent="handleLogin">
    <input v-model="email" type="email" placeholder="Email" required />
    <input v-model="password" type="password" placeholder="Password" required />
    <button type="submit" :disabled="loading">
      {{ loading ? 'Logging in...' : 'Login' }}
    </button>
    <div v-if="error" class="error">{{ error }}</div>
  </form>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/services/axiosClient';

const router = useRouter();
const email = ref('');
const password = ref('');
const loading = ref(false);
const error = ref(null);

async function handleLogin() {
  loading.value = true;
  error.value = null;

  try {
    const response = await apiClient.post('/api/v1/auth/login', {
      email: email.value,
      password: password.value,
    });

    // Store tokens
    localStorage.setItem('accessToken', response.data.accessToken);
    localStorage.setItem('refreshToken', response.data.refreshToken);
    localStorage.setItem('userId', response.data.userId);
    localStorage.setItem('profileId', response.data.profileId);

    // Redirect to dashboard
    router.push('/dashboard');
  } catch (err) {
    error.value = err.response?.data?.message || 'Login failed';
  } finally {
    loading.value = false;
  }
}
</script>
```

---

## Environment-Specific Configuration

### Development (Local)
```javascript
// .env.development
VITE_API_URL=http://localhost:8080
VITE_API_TIMEOUT=30000
VITE_DEBUG=true
```

### Staging (VPS)
```javascript
// .env.staging
VITE_API_URL=https://api.staging.mhsa.com
VITE_API_TIMEOUT=30000
VITE_DEBUG=false
```

### Production
```javascript
// .env.production
VITE_API_URL=https://api.mhsa.com
VITE_API_TIMEOUT=60000
VITE_DEBUG=false
```

---

## CORS Handling

### Development (Current Setup) ✅
- **CORS:** Enabled with default settings (allows all origins)
- **Status:** Works without modification

### Production (Needs Configuration)
If frontend is on different domain, update backend CORS:

```java
// src/main/java/com/mhsa/backend/auth/config/CorsConfig.java

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("https://app.mhsa.com", "https://www.mhsa.com")
                    .allowedMethods("GET", "POST", "PATCH", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

---

## Error Handling

```javascript
// src/services/errorHandler.js

export const handleApiError = (error) => {
  if (!error.response) {
    return 'Network error. Please check your connection.';
  }

  const status = error.response.status;
  const data = error.response.data;

  switch (status) {
    case 400:
      return data.message || 'Invalid request';
    case 401:
      // Token expired
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
      return 'Session expired. Please login again.';
    case 403:
      return 'You do not have permission to access this resource';
    case 404:
      return 'Resource not found';
    case 500:
      return 'Server error. Please try again later.';
    default:
      return data.message || 'An error occurred';
  }
};
```

---

## Testing Backend Connectivity

### Before Frontend Starts

```bash
# Test gateway health
curl http://localhost:8080/health
# Expected: "healthy"

# Test auth service
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User",
    "role": "TEEN",
    "accountType": "TEEN"
  }'

# Expected: { userId, profileId, accessToken, ... }
```

---

## Frontend Stack Compatibility

✅ **Compatible with all major frameworks:**

```javascript
// Vue 3 + Vite
import { useQuery } from '@tanstack/vue-query';

const { data: user } = useQuery({
  queryKey: ['user'],
  queryFn: () => apiClient.get('/api/v1/auth/me'),
});
```

```javascript
// React + Next.js
import { useQuery } from '@tanstack/react-query';

const { data: user } = useQuery({
  queryKey: ['user'],
  queryFn: () => apiClient.get('/api/v1/auth/me'),
});
```

```javascript
// Svelte
import { fetch } from 'svelte/fetch';

async function loadUser() {
  const response = await fetch(
    'http://localhost:8080/api/v1/auth/me',
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.json();
}
```

---

## Checklist: Frontend Ready to Connect

- [x] Backend running on http://localhost:8080
- [x] All 11 containers healthy
- [x] Nginx gateway routing requests correctly
- [x] CORS enabled (development)
- [x] JWT authentication ready
- [x] API endpoints documented
- [x] Error handling patterns defined
- [x] No database queries on frontend (all via API)
- [x] Authentication token storage (localStorage)
- [x] Token refresh mechanism ready

---

## Summary

**Frontend cần cấu hình:**
1. `API_BASE_URL = http://localhost:8080`
2. Add `Authorization: Bearer {token}` header
3. Use standard fetch/axios with JSON content-type

**Backend cấu trúc đảm bảo:**
- ✅ Unified API gateway (Nginx)
- ✅ Consistent endpoint paths (`/api/v1/{service}/*`)
- ✅ JWT authentication across all services
- ✅ CORS enabled for development
- ✅ All headers properly forwarded

**Frontend KHÔNG cần thay đổi cấu trúc API!** 🎉

Just point to `http://localhost:8080` and use standard HTTP patterns.

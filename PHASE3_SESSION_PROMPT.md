# Next Session Prompt - Phase 3: Tracking Service Extraction

**Current Status:** 2026-05-14  
**Phase 2:** ✅ COMPLETE - AI service extracted with 1,654 LOC, 32 files

---

## Phase 3 Overview

Extract **Tracking/Analytics Module** (48 Java classes) into standalone `tracking-service` (port 8083).

### What to Extract

- **Controllers (6):** DiaryEntry, FoodLog, MoodLog, SleepLog, Streak, MediaAttachment
- **Services (12):** Interface + Impl for each tracking type
- **Repositories (6):** JPA repositories for each entity
- **Entities (6):** DiaryEntry, FoodLog, MoodLog, SleepLog, Streak, MediaAttachment
- **DTOs (18):** Request/Response objects
- **Mappers (6):** Entity ↔ DTO conversion

### Key Difference from Phase 2
- **AI Service:** Single feature (chat) - straightforward extraction
- **Tracking Service:** Multi-feature module (5 tracking types + media) with event publishing

### Database
- Separate: `tracking_db` on `postgres-tracking:5434`
- Tables: diary_entries, food_logs, mood_logs, sleep_logs, streaks, media_attachments
- No FK to user tables (UUID references only)

### Services After Phase 3
```
auth-service (8081, auth_db)
ai-service (8082, ai_db) → calls tracking-service for context
tracking-service (8083, tracking_db) ← NEW
```

---

## 10-Step Implementation Plan

1. **Create module structure** (30 min) - pom.xml, directories
2. **Extract tracking code** (2 hrs) - 48 Java classes from monolith
3. **Create database schema** (45 min) - tracking tables + indexes
4. **Add context endpoint** (1 hr) - GET /internal/v1/tracking/context/{profileId}
5. **Implement JWT security** (30 min) - SecurityConfig, validation
6. **Setup event publishing** (1 hr) - RabbitMQ event producers
7. **Configuration files** (30 min) - application-docker.properties, Dockerfile
8. **Update docker-compose** (30 min) - postgres-tracking, tracking-service
9. **Update pom.xml** (15 min) - Add tracking-service module
10. **Update ai-service config** (15 min) - Point to tracking-service URL

**Total: ~7-9 hours (4-5 days)**

---

## Critical Implementation Notes

### ContextController (Step 4)
This is the key integration point for AI service:
```
GET /internal/v1/tracking/context/{profileId}?days=7
→ Aggregates last 7 days of sleep, mood, food, diary data
→ Returns human-readable summary for AI chat context
→ AI service calls this instead of accessing database directly
```

### Event Publishing (Step 6)
Create `TrackingEventPublisher` to publish when data changes:
- `tracking.diary.created`
- `tracking.mood.logged`
- `tracking.streak.updated`

Also subscribe to `auth.user.deleted` to clean up tracking data.

### Database Schema (Step 3)
Extract these tables from `app/src/main/resources/db/migration/V1__create_tables.sql`:
- diary_entries
- food_logs
- mood_logs
- sleep_logs
- streaks
- media_attachments

Include indexes:
- idx_food_logs_profile_entry_date
- idx_sleep_logs_profile_entry_date
- idx_streaks_profile_type

### Configuration Update (Step 10)
AI Service needs to change:
```properties
# OLD (Phase 2)
service.tracking.url=http://backend-app:8080

# NEW (Phase 3)
service.tracking.url=http://tracking-service:8083
```

---

## Git Workflow

```bash
git checkout -b feature/phase-3-tracking-service

# Commit after major steps
git commit -m "feat(phase-3): create tracking-service module structure"
git commit -m "feat(phase-3): extract 48 tracking classes from monolith"
git commit -m "feat(phase-3): implement context aggregation endpoint"
git commit -m "feat(phase-3): setup event publishing and JWT validation"
git commit -m "feat(phase-3): add tracking-service to docker-compose"

git push origin feature/phase-3-tracking-service
```

---

## Testing Checklist

- [ ] `mvn clean package -DskipTests` succeeds
- [ ] `docker-compose up -d` starts all 3 services + 3 databases
- [ ] Health checks pass for all services
- [ ] tracking-service creates tracking_db with all tables
- [ ] Context endpoint returns aggregated user data
- [ ] AI service calls tracking-service (not backend-app) for context
- [ ] JWT validation works for protected endpoints
- [ ] RabbitMQ events are published when data changes

---

## Start Here

1. Read this prompt thoroughly
2. Review Phase 2 completion at `PHASE2_COMPLETION.md`
3. Examine monolith tracking code: `app/src/main/java/com/mhsa/backend/tracking/`
4. Check database schema: `app/src/main/resources/db/migration/V1__create_tables.sql`
5. Use ai-service as template for structure and configuration
6. Follow the 10 steps in order

**Let's build the tracking service! 🚀**

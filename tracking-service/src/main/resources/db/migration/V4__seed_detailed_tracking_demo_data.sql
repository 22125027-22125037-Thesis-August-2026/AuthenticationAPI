-- Detailed demo/tracking data for teen001 (Anh Nguyen) - primary demo account
-- User UUID:    3ae449ca-3584-7d74-9f20-c34fb6036e8f
-- Profile UUID: e1d0add5-b9c8-57b5-36e6-059991832f17
--
-- V3 only seeded the last 7 days of diary/food/sleep and left mood_logs and
-- streaks completely empty. It also predates several mobile app changes to
-- how tracking fields are interpreted. This migration:
--   0) backfills the 7 days already inserted by V3 so their values match the
--      contracts the mobile app now expects (see per-section notes below),
--   1) extends diary_entries / food_logs / sleep_logs back 23 more days
--      (days -7 .. -29), giving a full 30-day history for trend charts,
--   2) seeds mood_logs (morning + evening check-in) for all 30 days,
--   3) seeds a DAILY_TRACKING streak row consistent with that history,
--   4) seeds a few media_attachments so the media gallery has content.
-- All inserts are idempotent (ON CONFLICT DO NOTHING) and safe to re-run.
-- The backfill UPDATEs in section 0 are guarded so re-running them is a
-- no-op once the data has already been normalized.

-- ==========================================================================
-- 0) Backfill correction for the 7 days already seeded by V3
-- --------------------------------------------------------------------------
-- Field contracts the mobile app now enforces (see src/constants/moods.ts,
-- src/constants/emotions.ts, src/screens/tracking/food/FoodMainScreen.tsx,
-- src/hooks/useHomeDashboardData.ts, src/constants/sleep.ts,
-- src/screens/tracking/diary/DiaryEntryScreen.tsx):
--   * diary_entries.mood_tag   must be one of TERRIBLE/BAD/NEUTRAL/GOOD/EXCELLENT
--   * diary_entries.positivity_score must be one of {1,2,3,4,6,7,8,10}
--     (the Plutchik 8-emotion scale; positivity_score wins over mood_tag
--     when both are present, so the two must describe the same emotion)
--   * diary_entries.content should carry "Tags: t1, t2 | Note: ..." so the
--     entry re-opens with its tag chips pre-selected
--   * diary_entries.created_at must fall on the same calendar day as
--     entry_date, since the Trophy/streak screen derives streaks from
--     created_at, not entry_date
--   * food_logs.water_glasses is now read as milliliters (water_glasses/1000)
--   * food_logs.satiety_level is now read as a numeric string meal count
--     (Number.parseInt(satietyLevel, 10))
--   * sleep_logs.sleep_quality must be within 1-5
-- ==========================================================================
UPDATE diary_entries
SET
    content = 'Tags: ' || (ARRAY[
        'Học tập, Nghỉ ngơi',
        'Nghỉ ngơi, Gia đình',
        'Học tập, Mệt mỏi',
        'Học tập, Trường học',
        'Bạn bè, Du lịch',
        'Học tập, Trường học',
        'Học tập, Bản thân'
    ])[(CURRENT_DATE - entry_date) + 1] || ' | Note: ' || content,
    mood_tag = (ARRAY['NEUTRAL','GOOD','GOOD','GOOD','EXCELLENT','BAD','GOOD'])[(CURRENT_DATE - entry_date) + 1],
    positivity_score = (ARRAY[6,7,8,8,10,4,8])[(CURRENT_DATE - entry_date) + 1],
    created_at = entry_date::timestamp + interval '20 hours',
    updated_at = now()
WHERE profile_id = 'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid
  AND entry_date BETWEEN CURRENT_DATE - 6 AND CURRENT_DATE
  AND content NOT LIKE 'Tags:%';

UPDATE food_logs
SET
    water_glasses = (ARRAY[1500,1800,2000,1500,1700,1200,1500])[(CURRENT_DATE - entry_date) + 1],
    satiety_level = (ARRAY['3','3','4','3','3','3','3'])[(CURRENT_DATE - entry_date) + 1],
    updated_at = now()
WHERE profile_id = 'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid
  AND entry_date BETWEEN CURRENT_DATE - 6 AND CURRENT_DATE
  AND satiety_level !~ '^\d+$';

UPDATE sleep_logs
SET
    sleep_quality = (ARRAY[4,5,4,2,5,3,5])[(CURRENT_DATE - entry_date) + 1],
    updated_at = now()
WHERE profile_id = 'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid
  AND entry_date BETWEEN CURRENT_DATE - 6 AND CURRENT_DATE
  AND sleep_quality > 5;

-- ==========================================================================
-- 1) Diary entries: days -7 .. -29 (23 additional days)
-- ==========================================================================
INSERT INTO diary_entries (diary_entry_id, profile_id, title, content, mood_tag, positivity_score, entry_date, created_at, updated_at)
SELECT
    (regexp_replace(md5('demo-teen001-diary-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    (ARRAY[
        'Ngay binh yen',
        'Ap luc bai tap nhom',
        'Niem vui nho',
        'Met moi sau buoi hoc them',
        'Lo lang truoc ky thi giua ky',
        'Hoat dong ngoai khoa',
        'Buon vi diem kem',
        'Mot ngay binh thuong',
        'Gap lai ban cu',
        'Cuoi tuan thu gian'
    ])[((n - 7) % 10) + 1],
    'Tags: ' || (ARRAY[
        'Nghỉ ngơi, Bản thân',
        'Học tập, Bạn bè',
        'Học tập, Trường học',
        'Học tập, Mệt mỏi',
        'Học tập, Trường học',
        'Tập thể dục, Bạn bè',
        'Học tập, Trường học',
        'Học tập, Nghỉ ngơi',
        'Bạn bè, Mua sắm',
        'Xem phim, Gia đình'
    ])[((n - 7) % 10) + 1] || ' | Note: ' || (ARRAY[
        'Hom nay khong co gi dac biet. Di hoc, ve nha, lam bai tap. Cam thay on dinh va binh yen.',
        'Lam viec nhom cho du an Sinh hoc. Bat dong y kien voi mot ban trong nhom khien minh hoi kho chiu. Van co gang hoan thanh dung han.',
        'Duoc co giao khen bai van hay truoc lop. Cam thay vui va tu tin hon han.',
        'Hoc them den 9 gio toi. Rat met nhung can phai co gang vi ky thi sap toi.',
        'Con vai ngay nua la thi giua ky mon Ly va Hoa. Cam thay ap luc va kho ngu duoc vi lo lang.',
        'Tham gia cau lac bo the thao sau gio hoc. Choi bong chuyen voi cac ban, cam thay sang khoai va vui ve.',
        'Nhan lai bai kiem tra 15 phut mon Hoa, diem khong nhu mong doi. Cam thay hoi buon nhung se co gang hon.',
        'Mot ngay hoc binh thuong, khong co gi noi bat. Hoan thanh het bai tap ve nha.',
        'Tinh co gap lai ban than hoi cap 2 o sieu thi. Noi chuyen ram ran, on lai ky niem cu, rat vui.',
        'Cuoi tuan duoc nghi ngoi, xem phim voi gia dinh va ngu bu. Cam thay thu gian hoan toan.'
    ])[((n - 7) % 10) + 1],
    (ARRAY['NEUTRAL','BAD','GOOD','BAD','BAD','EXCELLENT','BAD','NEUTRAL','EXCELLENT','GOOD'])[((n - 7) % 10) + 1],
    (ARRAY[6,4,8,3,4,10,3,6,10,8])[((n - 7) % 10) + 1],
    CURRENT_DATE - n,
    (CURRENT_DATE - n)::timestamp + interval '20 hours',
    now()
FROM generate_series(7, 29) AS s(n)
ON CONFLICT (diary_entry_id) DO NOTHING;

-- ==========================================================================
-- 2) Food logs: days -7 .. -29
--    water_glasses is stored as milliliters (mobile divides by 1000 to get
--    liters); satiety_level is stored as a numeric-string meal count.
-- ==========================================================================
INSERT INTO food_logs (food_id, profile_id, water_glasses, food_description, satiety_level, entry_date, created_at, updated_at)
SELECT
    (regexp_replace(md5('demo-teen001-food-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    (ARRAY[1500,1200,1800,900,1100,2200,900,1500,1900,2000])[((n - 7) % 10) + 1],
    (ARRAY[
        'Sang: Xoi xeo. Trua: Com suon nuong. Toi: Canh rau ngot voi thit bam',
        'Sang: Banh mi trung. Trua: Bun cha. Toi: Com voi ca kho',
        'Sang: Sua tuoi va banh quy. Trua: Com ga xoi mo. Toi: Lau nam gia dinh',
        'Sang: Bo qua bua sang vi day. Trua: Com hop cang tin truong. Toi: Mi goi voi trung',
        'Sang: Ngu coc va sua. Trua: An it vi lo lang. Toi: Chao voi thit bam',
        'Sang: Banh cuon. Trua: Com tam suon bi cha. Toi: Ga chien voi salad',
        'Sang: Banh mi ngot. Trua: Com voi trung chien. Toi: An it, khong ngon mieng',
        'Sang: Xoi dau xanh. Trua: Bun bo Hue. Toi: Rau muong xao va ca kho',
        'Sang: Banh bao. Trua: Com voi tom rang. Toi: Lau Thai cung ban',
        'Sang: Pancake gia dinh lam. Trua: Com chien hai san. Toi: Do nuong BBQ cuoi tuan'
    ])[((n - 7) % 10) + 1],
    (ARRAY['3','3','3','2','2','4','3','3','3','4'])[((n - 7) % 10) + 1],
    CURRENT_DATE - n,
    now(),
    now()
FROM generate_series(7, 29) AS s(n)
ON CONFLICT (profile_id, entry_date) DO NOTHING;

-- ==========================================================================
-- 3) Sleep logs: days -7 .. -29 (sleep_quality kept within the UI's 1-5 range)
-- ==========================================================================
INSERT INTO sleep_logs (sleep_log_id, profile_id, sleep_start_at, sleep_end_at, duration_minutes, sleep_quality, note, entry_date, logged_at, created_at, updated_at)
SELECT
    (regexp_replace(md5('demo-teen001-sleep-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    (CURRENT_DATE - n)::timestamp + ((ARRAY[22,23,21.5,0.5,1,20.5,23.5,22,21,20])[((n - 7) % 10) + 1]::text || ' hours')::interval,
    (CURRENT_DATE - n)::timestamp + ((ARRAY[6,6.5,5.5,7,7.5,5,7,6,5.5,6])[((n - 7) % 10) + 1]::text || ' hours')::interval,
    (ARRAY[480,420,510,360,300,540,330,450,480,510])[((n - 7) % 10) + 1],
    (ARRAY[5,3,5,2,1,5,2,3,4,5])[((n - 7) % 10) + 1],
    (ARRAY[
        'Ngu ngon, khong mo mong',
        'Kho ngu vi nghi lam viec nhom',
        'Ngu rat sau va thoai mai',
        'Ngu rat it do hoc bai den khuya',
        'Tran troc vi lo lang chuyen thi cu',
        'Ngu sau sau khi van dong the thao',
        'Ngu chap chon, thuc day nhieu lan',
        'Giac ngu binh thuong',
        'Ngu ngon sau khi gap ban cu',
        'Ngu bu, rat sau va dai'
    ])[((n - 7) % 10) + 1],
    CURRENT_DATE - n,
    now(),
    now(),
    now()
FROM generate_series(7, 29) AS s(n)
ON CONFLICT (sleep_log_id) DO NOTHING;

-- ==========================================================================
-- 4) Mood logs: two check-ins (morning + evening) per day for all 30 days
--    NOTE: no mobile screen currently reads mood_logs or calls
--    GET /tracking/moods (the Home mood check-in writes to diary_entries
--    instead). Kept for parity with the tracking-service API surface and any
--    future/parent-web consumer, but expect no visible effect in the app.
-- ==========================================================================
INSERT INTO mood_logs (mood_log_id, profile_id, mood_score, note, logged_at, created_at, updated_at)
SELECT
    (regexp_replace(md5('demo-teen001-mood-am-' || d::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    (ARRAY[6,4,7,5,3,8,4,6,7,7])[(d % 10) + 1],
    (ARRAY[
        'Thuc day cam thay binh thuong, san sang cho ngay moi',
        'Thuc day van con met, khong muon di hoc',
        'Thuc day rat vui ve va tran day nang luong',
        'Thuc day hoi mo mang vi ngu khong du',
        'Thuc day voi cam giac lo lang ve bai kiem tra',
        'Thuc day sang khoai sau giac ngu ngon',
        'Thuc day buon vi nho lai chuyen hom qua',
        'Thuc day binh thuong, khong co gi dac biet',
        'Thuc day vui ve vi hom nay co hen voi ban',
        'Thuc day thoai mai vi la ngay nghi'
    ])[(d % 10) + 1],
    (CURRENT_DATE - d)::timestamp + interval '7 hours',
    now(),
    now()
FROM generate_series(0, 29) AS s(d)
ON CONFLICT (mood_log_id) DO NOTHING;

INSERT INTO mood_logs (mood_log_id, profile_id, mood_score, note, logged_at, created_at, updated_at)
SELECT
    (regexp_replace(md5('demo-teen001-mood-pm-' || d::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    (ARRAY[7,5,8,4,2,9,3,7,8,8])[(d % 10) + 1],
    (ARRAY[
        'Truoc khi ngu cam thay hai long voi ngay hom nay',
        'Truoc khi ngu van con cang thang vi bai tap chua xong',
        'Truoc khi ngu rat vui vi mot ngay tuyet voi',
        'Truoc khi ngu cam thay kiet suc',
        'Truoc khi ngu lo lang kho ngu duoc',
        'Truoc khi ngu vui ve va man nguyen',
        'Truoc khi ngu buon va hoi that vong ve ban than',
        'Truoc khi ngu on dinh, san sang nghi ngoi',
        'Truoc khi ngu vui vi duoc gap ban than',
        'Truoc khi ngu rat thu gian sau ngay nghi'
    ])[(d % 10) + 1],
    (CURRENT_DATE - d)::timestamp + interval '21.5 hours',
    now(),
    now()
FROM generate_series(0, 29) AS s(d)
ON CONFLICT (mood_log_id) DO NOTHING;

-- ==========================================================================
-- 5) Streak: single DAILY_TRACKING streak consistent with the 30-day history
--    (current streak of 30 days, personal best of 42 days set earlier)
--    NOTE: the Trophy screen (ProfileScreen.tsx) does not read this table --
--    it derives the streak client-side from diary_entries.created_at
--    (see calculateLongestStreakFromCreatedAt in src/utils/diary.ts), since
--    there is no backend streak/achievement service wired up yet. Section 0
--    above backdates created_at for the V3 days precisely so that client-side
--    calculation lines up with the current_count/longest_count seeded here.
-- ==========================================================================
INSERT INTO streaks (streak_id, profile_id, streak_type, current_count, longest_count, last_logged_at, created_at, updated_at)
VALUES (
    (regexp_replace(md5('demo-teen001-streak-daily'), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    'DAILY_TRACKING',
    30,
    42,
    now(),
    now() - interval '90 days',
    now()
)
ON CONFLICT (profile_id, streak_type) DO NOTHING;

-- ==========================================================================
-- 6) Media attachments: sample photos/audio attached to a few diary entries
-- ==========================================================================
INSERT INTO media_attachments (media_attachment_id, profile_id, diary_entry_id, food_log_id, file_url, file_name, media_type, mime_type, file_size_bytes, created_at)
SELECT
    (regexp_replace(md5('demo-teen001-media-1'), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    diary_entry_id,
    NULL,
    'https://picsum.photos/seed/mhsa-demo-diary-today/800/600',
    'nhat-ky-hom-nay.jpg',
    'IMAGE',
    'image/jpeg',
    245678,
    now()
FROM diary_entries
WHERE profile_id = 'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid AND entry_date = CURRENT_DATE
LIMIT 1
ON CONFLICT (media_attachment_id) DO NOTHING;

INSERT INTO media_attachments (media_attachment_id, profile_id, diary_entry_id, food_log_id, file_url, file_name, media_type, mime_type, file_size_bytes, created_at)
SELECT
    (regexp_replace(md5('demo-teen001-media-2'), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    diary_entry_id,
    NULL,
    'https://picsum.photos/seed/mhsa-demo-picnic/800/600',
    'di-choi-cong-vien.jpg',
    'IMAGE',
    'image/jpeg',
    312450,
    now()
FROM diary_entries
WHERE profile_id = 'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid AND entry_date = CURRENT_DATE - 4
LIMIT 1
ON CONFLICT (media_attachment_id) DO NOTHING;

INSERT INTO media_attachments (media_attachment_id, profile_id, diary_entry_id, food_log_id, file_url, file_name, media_type, mime_type, file_size_bytes, created_at)
SELECT
    (regexp_replace(md5('demo-teen001-media-3'), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    diary_entry_id,
    NULL,
    'https://picsum.photos/seed/mhsa-demo-clb-thethao/800/600',
    'clb-the-thao.jpg',
    'IMAGE',
    'image/jpeg',
    289110,
    now()
FROM diary_entries
WHERE profile_id = 'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid AND entry_date = CURRENT_DATE - 15
LIMIT 1
ON CONFLICT (media_attachment_id) DO NOTHING;

INSERT INTO media_attachments (media_attachment_id, profile_id, diary_entry_id, food_log_id, file_url, file_name, media_type, mime_type, file_size_bytes, created_at)
SELECT
    (regexp_replace(md5('demo-teen001-media-4'), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid,
    diary_entry_id,
    NULL,
    'https://picsum.photos/seed/mhsa-demo-voice-memo/400/400',
    'ghi-am-tam-su.mp3',
    'AUDIO',
    'audio/mpeg',
    154032,
    now()
FROM diary_entries
WHERE profile_id = 'e1d0add5-b9c8-57b5-36e6-059991832f17'::uuid AND entry_date = CURRENT_DATE - 22
LIMIT 1
ON CONFLICT (media_attachment_id) DO NOTHING;

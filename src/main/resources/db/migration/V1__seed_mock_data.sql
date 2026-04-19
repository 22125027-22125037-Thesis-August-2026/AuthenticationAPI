-- Seeds 20 deterministic mock rows for each existing application table.

INSERT INTO users (
    user_id, email, password, role, full_name, dob, phone_number,
    credits_balance, created_at, updated_at, pin_code, parent_id
)
SELECT
    (
        substr(md5('user-' || n::text), 1, 8) || '-' ||
        substr(md5('user-' || n::text), 9, 4) || '-' ||
        substr(md5('user-' || n::text), 13, 4) || '-' ||
        substr(md5('user-' || n::text), 17, 4) || '-' ||
        substr(md5('user-' || n::text), 21, 12)
    )::uuid,
    'user' || n::text || '@mhsa.local',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoO5P6gF6jQvC6J6k0M0B4f2s7v2vQ6V0C',
    CASE (n % 4)
        WHEN 1 THEN 'PARENT'
        WHEN 2 THEN 'TEEN'
        WHEN 3 THEN 'THERAPIST'
        ELSE 'ADMIN'
    END,
    'Mock User ' || n::text,
    (DATE '2008-01-01' + (n * 90)),
    '+8412345' || lpad(n::text, 4, '0'),
    (n * 50),
    now() - (n || ' days')::interval,
    now() - (n || ' days')::interval,
    lpad((1000 + n)::text, 4, '0'),
    NULL
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO profiles (
    profile_id, user_id, full_name, avatar_url, date_of_birth,
    phone_number, created_at, updated_at, profile_type
)
SELECT
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('user-' || n::text), 1, 8) || '-' ||
        substr(md5('user-' || n::text), 9, 4) || '-' ||
        substr(md5('user-' || n::text), 13, 4) || '-' ||
        substr(md5('user-' || n::text), 17, 4) || '-' ||
        substr(md5('user-' || n::text), 21, 12)
    )::uuid,
    'Mock Profile ' || n::text,
    'https://picsum.photos/seed/profile' || n::text || '/200/200',
    (DATE '2008-01-01' + (n * 90)),
    '+8498765' || lpad(n::text, 4, '0'),
    now() - (n || ' days')::interval,
    now() - (n || ' days')::interval,
    CASE (n % 3)
        WHEN 1 THEN 'PARENT'
        WHEN 2 THEN 'TEEN'
        ELSE 'THERAPIST'
    END
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO parent_profile (profile_id, linked_teen_id)
SELECT
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('user-' || (((n % 20) + 1))::text), 1, 8) || '-' ||
        substr(md5('user-' || (((n % 20) + 1))::text), 9, 4) || '-' ||
        substr(md5('user-' || (((n % 20) + 1))::text), 13, 4) || '-' ||
        substr(md5('user-' || (((n % 20) + 1))::text), 17, 4) || '-' ||
        substr(md5('user-' || (((n % 20) + 1))::text), 21, 12)
    )::uuid
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO teen_profile (profile_id, school, emergency_contact)
SELECT
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    'Mock High School ' || n::text,
    '+840000' || lpad(n::text, 5, '0')
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO therapist_profile (
    profile_id, specialization, bio, years_of_experience, consultation_fee, is_verified
)
SELECT
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    CASE (n % 4)
        WHEN 1 THEN 'CBT'
        WHEN 2 THEN 'Family Therapy'
        WHEN 3 THEN 'Trauma Care'
        ELSE 'Adolescent Counseling'
    END,
    'Mock therapist bio for profile ' || n::text,
    (2 + (n % 15)),
    (300000 + (n * 10000))::numeric(12,2),
    (n % 2 = 0)
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO diary_entries (
    diary_entry_id, profile_id, title, content, mood_tag,
    positivity_score, entry_date, created_at, updated_at
)
SELECT
    (
        substr(md5('diary-' || n::text), 1, 8) || '-' ||
        substr(md5('diary-' || n::text), 9, 4) || '-' ||
        substr(md5('diary-' || n::text), 13, 4) || '-' ||
        substr(md5('diary-' || n::text), 17, 4) || '-' ||
        substr(md5('diary-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    'Diary Day ' || n::text,
    'Today I practiced mindfulness and wrote a gratitude list #' || n::text,
    CASE (n % 5)
        WHEN 1 THEN 'calm'
        WHEN 2 THEN 'hopeful'
        WHEN 3 THEN 'anxious'
        WHEN 4 THEN 'motivated'
        ELSE 'tired'
    END,
    (60 + (n % 41)),
    (current_date - n),
    now() - (n || ' days')::interval,
    now() - (n || ' days')::interval
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (diary_entry_id) DO NOTHING;

INSERT INTO food_logs (
    food_id, profile_id, meal_type, food_description, satiety_level, created_at
)
SELECT
    (
        substr(md5('food-' || n::text), 1, 8) || '-' ||
        substr(md5('food-' || n::text), 9, 4) || '-' ||
        substr(md5('food-' || n::text), 13, 4) || '-' ||
        substr(md5('food-' || n::text), 17, 4) || '-' ||
        substr(md5('food-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    CASE (n % 4)
        WHEN 1 THEN 'Breakfast'
        WHEN 2 THEN 'Lunch'
        WHEN 3 THEN 'Dinner'
        ELSE 'Snack'
    END,
    'Mock meal description #' || n::text,
    CASE (n % 3)
        WHEN 1 THEN 'Satisfied'
        WHEN 2 THEN 'Neutral'
        ELSE 'Very Full'
    END,
    now() - (n || ' days')::interval
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (food_id) DO NOTHING;

INSERT INTO media_attachments (
    media_attachment_id, profile_id, diary_entry_id, food_log_id,
    file_url, file_name, media_type, mime_type, file_size_bytes, created_at
)
SELECT
    (
        substr(md5('media-' || n::text), 1, 8) || '-' ||
        substr(md5('media-' || n::text), 9, 4) || '-' ||
        substr(md5('media-' || n::text), 13, 4) || '-' ||
        substr(md5('media-' || n::text), 17, 4) || '-' ||
        substr(md5('media-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('diary-' || n::text), 1, 8) || '-' ||
        substr(md5('diary-' || n::text), 9, 4) || '-' ||
        substr(md5('diary-' || n::text), 13, 4) || '-' ||
        substr(md5('diary-' || n::text), 17, 4) || '-' ||
        substr(md5('diary-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('food-' || n::text), 1, 8) || '-' ||
        substr(md5('food-' || n::text), 9, 4) || '-' ||
        substr(md5('food-' || n::text), 13, 4) || '-' ||
        substr(md5('food-' || n::text), 17, 4) || '-' ||
        substr(md5('food-' || n::text), 21, 12)
    )::uuid,
    'https://cdn.mhsa.local/mock/media/' || n::text,
    'mock-file-' || n::text || '.bin',
    CASE (n % 4)
        WHEN 1 THEN 'IMAGE'
        WHEN 2 THEN 'VIDEO'
        WHEN 3 THEN 'AUDIO'
        ELSE 'OTHER'
    END,
    CASE (n % 4)
        WHEN 1 THEN 'image/jpeg'
        WHEN 2 THEN 'video/mp4'
        WHEN 3 THEN 'audio/mpeg'
        ELSE 'application/octet-stream'
    END,
    (100000 + n * 1024),
    now() - (n || ' days')::interval
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (media_attachment_id) DO NOTHING;

INSERT INTO mood_logs (
    mood_log_id, profile_id, mood_score, note, logged_at, created_at, updated_at
)
SELECT
    (
        substr(md5('mood-' || n::text), 1, 8) || '-' ||
        substr(md5('mood-' || n::text), 9, 4) || '-' ||
        substr(md5('mood-' || n::text), 13, 4) || '-' ||
        substr(md5('mood-' || n::text), 17, 4) || '-' ||
        substr(md5('mood-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    (1 + (n % 10)),
    'Mood tracking note #' || n::text,
    now() - (n || ' hours')::interval,
    now() - (n || ' days')::interval,
    now() - (n || ' days')::interval
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (mood_log_id) DO NOTHING;

INSERT INTO sleep_logs (
    sleep_log_id, profile_id, sleep_start_at, sleep_end_at,
    duration_minutes, sleep_quality, note, logged_at, created_at, updated_at
)
SELECT
    (
        substr(md5('sleep-' || n::text), 1, 8) || '-' ||
        substr(md5('sleep-' || n::text), 9, 4) || '-' ||
        substr(md5('sleep-' || n::text), 13, 4) || '-' ||
        substr(md5('sleep-' || n::text), 17, 4) || '-' ||
        substr(md5('sleep-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    (now() - ((n + 8) || ' hours')::interval),
    (now() - (n || ' hours')::interval),
    (420 + (n % 120)),
    (1 + (n % 5)),
    'Sleep note #' || n::text,
    now() - (n || ' hours')::interval,
    now() - (n || ' days')::interval,
    now() - (n || ' days')::interval
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (sleep_log_id) DO NOTHING;

INSERT INTO streaks (
    streak_id, profile_id, streak_type, current_count,
    longest_count, last_logged_at, created_at, updated_at
)
SELECT
    (
        substr(md5('streak-' || n::text), 1, 8) || '-' ||
        substr(md5('streak-' || n::text), 9, 4) || '-' ||
        substr(md5('streak-' || n::text), 13, 4) || '-' ||
        substr(md5('streak-' || n::text), 17, 4) || '-' ||
        substr(md5('streak-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    'DAILY_CHECKIN',
    (n % 30),
    (30 + (n % 60)),
    now() - (n || ' days')::interval,
    now() - (n || ' days')::interval,
    now() - (n || ' days')::interval
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (streak_id) DO NOTHING;

INSERT INTO chat_sessions (session_id, profile_id, created_at)
SELECT
    (
        substr(md5('session-' || n::text), 1, 8) || '-' ||
        substr(md5('session-' || n::text), 9, 4) || '-' ||
        substr(md5('session-' || n::text), 13, 4) || '-' ||
        substr(md5('session-' || n::text), 17, 4) || '-' ||
        substr(md5('session-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('profile-' || n::text), 1, 8) || '-' ||
        substr(md5('profile-' || n::text), 9, 4) || '-' ||
        substr(md5('profile-' || n::text), 13, 4) || '-' ||
        substr(md5('profile-' || n::text), 17, 4) || '-' ||
        substr(md5('profile-' || n::text), 21, 12)
    )::uuid,
    now() - (n || ' days')::interval
FROM generate_series(1, 20) AS g(n)
ON CONFLICT (session_id) DO NOTHING;

INSERT INTO chat_messages (message_id, session_id, sender, content, sent_at)
SELECT
    (
        substr(md5('message-' || n::text), 1, 8) || '-' ||
        substr(md5('message-' || n::text), 9, 4) || '-' ||
        substr(md5('message-' || n::text), 13, 4) || '-' ||
        substr(md5('message-' || n::text), 17, 4) || '-' ||
        substr(md5('message-' || n::text), 21, 12)
    )::uuid,
    (
        substr(md5('session-' || n::text), 1, 8) || '-' ||
        substr(md5('session-' || n::text), 9, 4) || '-' ||
        substr(md5('session-' || n::text), 13, 4) || '-' ||
        substr(md5('session-' || n::text), 17, 4) || '-' ||
        substr(md5('session-' || n::text), 21, 12)
    )::uuid,
    CASE WHEN n % 2 = 0 THEN 'AI' ELSE 'USER' END,
    encrypted_payload,
    now() - (n || ' hours')::interval
FROM (
    VALUES
        (1, 'K3p4qY2YOzrM/z9lDwmqvOq+/3DjN+hyqbqNGZduqTik2QErfi6mZDt4BCxINLg='),
        (2, '87dGYjkkTgcUGIpuoN8r5VufLDaJJwhXXG0/Vl6YR2T2CswyZmZsv4/O3qBGm+g='),
        (3, 'CZ+0XMlHGb5guM4y7yS0BCLqUdCsbrPGUry2jnjqL1q3Sj1nV6ddVbaRY7Dksdo='),
        (4, 'F4OI1KhkRkvNBSqemRl0j92SkDRMLgomvTpvYrJImZqB9wP3DZ75mVAYOeo+6Gs='),
        (5, 'wYo+Su4Zsjk6xxwXcM0Isv36c5cwfkechruhsdVaMo86FwkYSQ4gsOaZIUTsWpE='),
        (6, 'hie6nShNGdaC1/AmEK0DtmlPuyJUpkCAQrwidm3uGeL9pSvxgstOU49Rm6T89gM='),
        (7, 'EAuLNRFLCgAxB7a90PFrxcLVUgBeKbrNVFSABqtD94PzjR7cKnJVVnY06srdbBU='),
        (8, '/obPe6qT5vc5CC7VXSHL4LavUM7qtVjX1wkXyDsU5C3HJOHD3ysJ8mYxVzk5DsU='),
        (9, '2ayvIdhOMOjlVleg678cxJ/Ye1eCnDkG4DeSY+UlUOc7DdQU8FAJmO6oG8HyF0w='),
        (10, 'uFkkhNtdIyWTYv/5F0UfiZdIC++jAhh6C6XmuvJ2Opeh2zs97TsEBwTQHNmLR151'),
        (11, 'imlwf+tRkL7dWLF+AbP+OGAN4nyLss12VBbG40/oT/fsdhNFivOCt0CCen+xO1xt'),
        (12, 'ODx0w/hqrCoZNLoDns/yA4xNdn3i+9cNswYT0oMhj/kgi1vH0I+XueqtT67tEXq/'),
        (13, '4a1wcCVpce9+xrwrp8uhxbyhYsofKI5OMOg34wcAjITN7a8EAOBY1/Z/UDIzkoq4'),
        (14, 'v8hGLNCBnd672bhlBwfo8uUH3LgWSuqRoNYl/8Ysd13yV/UgYNEWwTeM4c/glFo5'),
        (15, '8O83bSSwu9nlZb1uAz0zM11F2R0tUB539KY7hMskbnTwBRKugvgtgCrQk/JCiEL0'),
        (16, 'rGLZGpBUJPwFV+RVdEk6hKVbtfD0zPxTHa0Cg1ZwZ9Ks5YadgYWKX7Pg0GThpH5K'),
        (17, 'qDxYCUowaR8UC4OY4oEsp6R4USSTejwMTIsrD4KgSl3FkRc5MhXZq4+QjcmXOeiK'),
        (18, '3YdQfDjQklfe72lv0VZTTKfZVGFQU9enF6X10KAUKK1uDdXJuJ7ZvJ76qtz3yLjv'),
        (19, 'mdKp5D6CDKxTP2jUwtV3LrwOEpKfLYn55VKUugAVyXsqaQfQMd7QbmK5iYIWIZ0i'),
        (20, '3f2XUHA40csG09x4mgbwdsrj1sK1H+YO+mhca8EhAZHvfWf/3mHBbXvQh1TAC56V')
) AS payloads(n, encrypted_payload)
ON CONFLICT (message_id) DO NOTHING;

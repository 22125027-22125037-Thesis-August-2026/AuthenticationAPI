-- Seed deterministic development/testing accounts for auth and profile tables.
-- Plaintext password: password
-- BCrypt hash: $2a$10$r0BNJBQr0qlwmLZhGHLD0OwGtDROevFNInQbie2qi8/OyVuy/TzeS

-- 30 TEEN users
INSERT INTO users (
    user_id,
    email,
    password,
    role,
    full_name,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-user-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'teen' || lpad(n::text, 3, '0') || '.dev@mhsa.local',
    '$2a$10$r0BNJBQr0qlwmLZhGHLD0OwGtDROevFNInQbie2qi8/OyVuy/TzeS',
    'TEEN',
    (
        (ARRAY['Anh','Binh','Chi','Dung','Giang','Hoa','Khanh','Linh','Minh','Nam','Oanh','Phuc','Quang','Trang','Vy'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 3) % 10) + 1]
    ),
    now() - ((90 - n) || ' days')::interval,
    now() - ((90 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (user_id) DO NOTHING;

-- 30 PARENT users
INSERT INTO users (
    user_id,
    email,
    password,
    role,
    full_name,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-user-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'parent' || lpad(n::text, 3, '0') || '.dev@mhsa.local',
    '$2a$10$r0BNJBQr0qlwmLZhGHLD0OwGtDROevFNInQbie2qi8/OyVuy/TzeS',
    'PARENT',
    (
        (ARRAY['An','Bao','Cuong','Diep','Hanh','Hung','Kiet','Lan','My','Nghia','Phuong','Quyen','Son','Thao','Yen'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 5) % 10) + 1]
    ),
    now() - ((120 - n) || ' days')::interval,
    now() - ((120 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (user_id) DO NOTHING;

-- 30 THERAPIST users
INSERT INTO users (
    user_id,
    email,
    password,
    role,
    full_name,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-user-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    'therapist' || lpad(n::text, 3, '0') || '.dev@mhsa.local',
    '$2a$10$r0BNJBQr0qlwmLZhGHLD0OwGtDROevFNInQbie2qi8/OyVuy/TzeS',
    'THERAPIST',
    (
        (ARRAY['Dr. Bao','Dr. Chau','Dr. Duy','Dr. Ha','Dr. Khoa','Dr. Lam','Dr. Mai','Dr. Nhi','Dr. Phat','Dr. Thu'])[(n % 10) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 7) % 10) + 1]
    ),
    now() - ((150 - n) || ' days')::interval,
    now() - ((150 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (user_id) DO NOTHING;

-- Base profiles for 30 TEEN accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    profile_type,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-profile-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-teen-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['Anh','Binh','Chi','Dung','Giang','Hoa','Khanh','Linh','Minh','Nam','Oanh','Phuc','Quang','Trang','Vy'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 3) % 10) + 1]
    ),
    'TEEN',
    now() - ((90 - n) || ' days')::interval,
    now() - ((90 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Base profiles for 30 PARENT accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    profile_type,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-profile-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-parent-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['An','Bao','Cuong','Diep','Hanh','Hung','Kiet','Lan','My','Nghia','Phuong','Quyen','Son','Thao','Yen'])[(n % 15) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 5) % 10) + 1]
    ),
    'PARENT',
    now() - ((120 - n) || ' days')::interval,
    now() - ((120 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

-- Base profiles for 30 THERAPIST accounts
INSERT INTO profiles (
    profile_id,
    user_id,
    full_name,
    profile_type,
    created_at,
    updated_at
)
SELECT
    (regexp_replace(md5('dev-profile-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (regexp_replace(md5('dev-user-therapist-' || n::text), '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'))::uuid,
    (
        (ARRAY['Dr. Bao','Dr. Chau','Dr. Duy','Dr. Ha','Dr. Khoa','Dr. Lam','Dr. Mai','Dr. Nhi','Dr. Phat','Dr. Thu'])[(n % 10) + 1]
        || ' ' ||
        (ARRAY['Nguyen','Tran','Le','Pham','Hoang','Phan','Vu','Vo','Dang','Bui'])[((n * 7) % 10) + 1]
    ),
    'THERAPIST',
    now() - ((150 - n) || ' days')::interval,
    now() - ((150 - n) || ' days')::interval
FROM generate_series(1, 30) AS s(n)
ON CONFLICT (profile_id) DO NOTHING;

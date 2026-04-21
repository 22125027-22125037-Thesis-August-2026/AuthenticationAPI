-- Add a dedicated teen test account with realistic one-week tracking data.

INSERT INTO users (
    user_id, email, password, role, full_name, dob, phone_number,
    credits_balance, created_at, updated_at
)
VALUES (
    '11111111-1111-4111-8111-111111111111'::uuid,
    'testuser@gmail.com',
    '$2a$10$.0bJohgRAL9pD3v.SCh6XuFITHBFTgODBVni7RhUCuPyPkGF3dyD2',
    'TEEN',
    'Nguyễn Văn Trọng',
    DATE '2008-05-15',
    '+84909999999',
    500,
    now() - interval '7 days',
    now()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO profiles (
    profile_id, user_id, full_name, avatar_url, date_of_birth,
    phone_number, created_at, updated_at, profile_type
)
VALUES (
    '22222222-2222-4222-8222-222222222222'::uuid,
    '11111111-1111-4111-8111-111111111111'::uuid,
    'Nguyễn Văn Trọng',
    'https://api.dicebear.com/7.x/notionists/svg?seed=Trong',
    DATE '2008-05-15',
    '+84909999999',
    now() - interval '7 days',
    now(),
    'TEEN'
)
ON CONFLICT (profile_id) DO NOTHING;

INSERT INTO teen_profile (profile_id, school, emergency_contact)
VALUES (
    '22222222-2222-4222-8222-222222222222'::uuid,
    'THPT Nguyễn Trãi',
    '+84901111222'
)
ON CONFLICT (profile_id) DO NOTHING;

-- Dữ liệu Nhật ký (Diary Entries) - Câu chuyện 7 ngày
WITH diary_data AS (
    SELECT * FROM (VALUES
        (0, 'Khởi đầu tuần mới', 'Hôm nay trường có buổi sinh hoạt dưới cờ khá chán. Trưa đi ăn với hội bạn thân vui phết.', 'Bình yên', 65),
        (1, 'Áp lực học hành', 'Chưa thuộc hết công thức Toán, cảm thấy hơi lo lắng. Hy vọng ngày mai đề không quá khó.', 'Căng thẳng', 40),
        (2, 'Thoát nạn!', 'Phù, cuối cùng cũng qua bài kiểm tra 1 tiết Toán. Chiều nay tự thưởng cho bản thân một ly trà sữa size L.', 'Vui vẻ', 85),
        (3, 'Tối thứ Sáu thảnh thơi', 'Thức khuya cày một bộ phim Netflix mới ra. Cảm giác không phải lo nghĩ về bài tập thật tuyệt.', 'Thoải mái', 75),
        (4, 'Phá đảo khu vui chơi', 'Cả nhóm đi chơi bowling và ăn lẩu. Cười đau cả bụng. Những ngày thế này làm mình thấy rất năng lượng.', 'Hào hứng', 95),
        (5, 'Ngày lười biếng', 'Cả ngày chỉ nằm nghe nhạc và lướt TikTok. Lâu lâu cũng cần một ngày reset lại bản thân.', 'Bình yên', 60),
        (6, 'Blue Monday', 'Hơi oải vì phải dậy sớm đi học lại, nhưng hôm nay có tiết Thể dục nên cũng đỡ chán.', 'Bình thường', 55)
    ) AS t(d, title, content, mood_tag, positivity_score)
)
INSERT INTO diary_entries (
    diary_entry_id, profile_id, title, content, mood_tag,
    positivity_score, entry_date, created_at, updated_at
)
SELECT
    (substr(md5('testuser-diary-' || d::text), 1, 8) || '-' || substr(md5('testuser-diary-' || d::text), 9, 4) || '-' || substr(md5('testuser-diary-' || d::text), 13, 4) || '-' || substr(md5('testuser-diary-' || d::text), 17, 4) || '-' || substr(md5('testuser-diary-' || d::text), 21, 12))::uuid,
    '22222222-2222-4222-8222-222222222222'::uuid,
    title, content, mood_tag, positivity_score,
    (current_date - (6 - d)),
    now() - ((6 - d) || ' days')::interval,
    now() - ((6 - d) || ' days')::interval
FROM diary_data
ON CONFLICT (diary_entry_id) DO NOTHING;


-- Dữ liệu Ăn uống & Nước (Food & Water Logs) - Tích hợp theo DTO mới
WITH food_data AS (
    SELECT * FROM (VALUES
        (0, 5, 'Sáng xôi mặn, trưa cơm sườn căn tin, chiều khát nước nên uống trà sữa.', 'COMFORTABLY_FULL'),
        (1, 3, 'Sáng bỏ bữa để ôn bài, trưa ăn vội ổ bánh mì, tối ăn cơm nhà.', 'HUNGRY'),
        (2, 6, 'Sáng bún bò, trưa cơm phần, chiều đi học về ăn vặt bánh tráng trộn.', 'COMFORTABLY_FULL'),
        (3, 4, 'Sáng phở gà, trưa ăn cơm mang theo, tối đặt gà rán KFC ăn xem phim.', 'STUFFED'),
        (4, 8, 'Đi ăn lẩu Thái sinh viên với đám bạn, uống khá nhiều nước lọc và nước ngọt.', 'STUFFED'),
        (5, 4, 'Ngủ nướng nên gộp bữa sáng và trưa. Tối làm tô mì tôm thêm quả trứng.', 'COMFORTABLY_FULL'),
        (6, 7, 'Sáng bánh cuốn, trưa cơm thịt luộc, mang theo bình nước bự đi học.', 'COMFORTABLY_FULL')
    ) AS t(d, water_glasses, food_description, satiety_level)
)
INSERT INTO food_logs (
    food_id, profile_id, water_glasses, food_description, satiety_level, entry_date, created_at
)
SELECT
    (substr(md5('testuser-food-' || d::text), 1, 8) || '-' || substr(md5('testuser-food-' || d::text), 9, 4) || '-' || substr(md5('testuser-food-' || d::text), 13, 4) || '-' || substr(md5('testuser-food-' || d::text), 17, 4) || '-' || substr(md5('testuser-food-' || d::text), 21, 12))::uuid,
    '22222222-2222-4222-8222-222222222222'::uuid,
    water_glasses, food_description, satiety_level,
    (current_date - (6 - d)),
    now() - ((6 - d) || ' days')::interval
FROM food_data
ON CONFLICT (food_id) DO NOTHING;


-- Dữ liệu Cảm xúc (Mood Logs)
WITH mood_data AS (
    SELECT * FROM (VALUES
        (0, 6, 'Đầu tuần khá nhiều bài tập.'),
        (1, 4, 'Hơi lo lắng cho bài kiểm tra ngày mai.'),
        (2, 8, 'Làm bài Toán tốt, nhẹ nhõm hẳn!'),
        (3, 7, 'Cuối tuần đến rồi, xõa thôi!'),
        (4, 9, 'Đi chơi với đám bạn cực vui.'),
        (5, 5, 'Chỉ muốn nằm ườn ở nhà, hơi lười.'),
        (6, 6, 'Bắt đầu tuần mới, cảm giác bình thường.')
    ) AS t(d, mood_score, note)
)
INSERT INTO mood_logs (
    mood_log_id, profile_id, mood_score, note, logged_at, created_at, updated_at
)
SELECT
    (substr(md5('testuser-mood-' || d::text), 1, 8) || '-' || substr(md5('testuser-mood-' || d::text), 9, 4) || '-' || substr(md5('testuser-mood-' || d::text), 13, 4) || '-' || substr(md5('testuser-mood-' || d::text), 17, 4) || '-' || substr(md5('testuser-mood-' || d::text), 21, 12))::uuid,
    '22222222-2222-4222-8222-222222222222'::uuid,
    mood_score, note,
    now() - ((6 - d) || ' days')::interval,
    now() - ((6 - d) || ' days')::interval,
    now() - ((6 - d) || ' days')::interval
FROM mood_data
ON CONFLICT (mood_log_id) DO NOTHING;


-- Dữ liệu Giấc ngủ (Sleep Logs) - Tạo biến động để vẽ biểu đồ cho đẹp
WITH sleep_data AS (
    SELECT * FROM (VALUES
        (0, '23:00', '06:30', 450, 3, 'Ngủ bình thường'),
        (1, '00:30', '06:00', 330, 2, 'Thức khuya ôn bài Toán, ngủ chập chờn'),
        (2, '22:30', '06:00', 450, 4, 'Thi xong ngủ cực kì ngon'),
        (3, '23:30', '07:00', 450, 3, 'Xem phim khuya một chút'),
        (4, '01:00', '09:30', 510, 4, 'Đi chơi về trễ, ngủ nướng cuối tuần'),
        (5, '23:00', '08:00', 540, 4, 'Ngủ thẳng giấc'),
        (6, '22:30', '06:15', 465, 3, 'Ngủ sớm để chuẩn bị đi học')
    ) AS t(d, start_time, end_time, duration, quality, note)
)
INSERT INTO sleep_logs (
    sleep_log_id, profile_id, sleep_start_at, sleep_end_at,
    duration_minutes, sleep_quality, note, entry_date, logged_at, created_at, updated_at
)
SELECT
    (substr(md5('testuser-sleep-' || d::text), 1, 8) || '-' || substr(md5('testuser-sleep-' || d::text), 9, 4) || '-' || substr(md5('testuser-sleep-' || d::text), 13, 4) || '-' || substr(md5('testuser-sleep-' || d::text), 17, 4) || '-' || substr(md5('testuser-sleep-' || d::text), 21, 12))::uuid,
    '22222222-2222-4222-8222-222222222222'::uuid,
    (date_trunc('day', current_date - (7 - d)) + start_time::time), -- Giờ ngủ tối hôm trước
    (date_trunc('day', current_date - (6 - d)) + end_time::time),   -- Giờ thức sáng hôm nay
    duration, quality, note,
    (current_date - (6 - d)),
    now() - ((6 - d) || ' days')::interval,
    now() - ((6 - d) || ' days')::interval,
    now() - ((6 - d) || ' days')::interval
FROM sleep_data
ON CONFLICT (sleep_log_id) DO NOTHING;


-- Giữ nguyên chuỗi hoạt động 7 ngày
INSERT INTO streaks (
    streak_id, profile_id, streak_type, current_count,
    longest_count, last_logged_at, created_at, updated_at
)
VALUES (
    '33333333-3333-4333-8333-333333333333'::uuid,
    '22222222-2222-4222-8222-222222222222'::uuid,
    'DAILY_CHECKIN',
    7,
    7,
    now(),
    now() - interval '7 days',
    now()
)
ON CONFLICT (streak_id) DO NOTHING;
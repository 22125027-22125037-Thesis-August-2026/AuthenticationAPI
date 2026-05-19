-- Seed tracking data for teen001 (Anh Nguyen) - 7 days comprehensive data
-- User UUID: 3ae449ca-3584-7d74-9f20-c34fb6036e8f
-- Profile UUID: e1d0add5-b9c8-57b5-36e6-059991832f17

-- Diary entries (5 detailed entries over 7 days)
INSERT INTO diary_entries (diary_entry_id, profile_id, title, content, mood_tag, positivity_score, entry_date, created_at, updated_at)
VALUES
    ('3a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'Tuan moi, tam trang moi', 'Hom nay la ngay dau cua tuan. Toi cam thay tot sau khi ngu du 8 tieng. Hoan thanh bai tap Toan va tieng Anh. Cam thay tu tin voi ky thi sap toi.', 'HAPPY', 80, CURRENT_DATE - 6, now(), now()),
    ('3a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5e', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'Ap luc tu viec hoc', 'Hom nay ban rong voi nhieu bai kiem tra. Cam thay lo lang va cang thang. Da chuan bi ky luong nhung van cam thay khong tu tin. Buoi toi hoc them voi ban cung lop.', 'ANXIOUS', 50, CURRENT_DATE - 3, now(), now()),
    ('3a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5f', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'Mot ngay tuyet voi', 'Gap cac ban than nhat. Di choi cong vien sau gio hoc. An trua o quan ua thich. Cam thay vui ve, thu gian va hanh phuc. Lo mat tich chut lo au.', 'EXCITED', 90, CURRENT_DATE - 1, now(), now()),
    ('3a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'Ket qua thi va phan tu', 'Hom nay nhan ket qua thi mon Toan - Diem 8.5! Cam thay thich thu va tu hao. Dieu nay khuyen khich toi tiep tuc hoc tap cham chi.', 'HAPPY', 85, CURRENT_DATE - 2, now(), now()),
    ('3a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5e', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'Buoi cuoi cua tuan', 'Cuoi cung tuan cung qua roi. Toi cam thay vua met vua thoa man voi nhung gi da hoan thanh. Ke hoach tuan sau: on tap toan, viet bai luan tieng Anh.', 'HAPPY', 75, CURRENT_DATE, now(), now());

-- Mood logs (21 entries - 3 per day)
INSERT INTO mood_logs (mood_log_id, profile_id, mood_score, note, logged_at, created_at, updated_at)
VALUES
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6a', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 80, 'Sang: Tinh day thoai mai, cam thay nang luong tot', now() - interval '6 days 8 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6b', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 75, 'Trua: Ban voi bai kiem tra, cam thay can co grang', now() - interval '6 days 12 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6c', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 78, 'Toi: Thu gian sau bai tap, cam thay thuc hien tot', now() - interval '6 days 20 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6d', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 72, 'Sang: Hoi met moi sau buoi hoc dem hom truoc', now() - interval '5 days 8 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 68, 'Trua: Buoi do an nhom, cam thay kho phoi hop', now() - interval '5 days 12 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6f', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 70, 'Toi: Ve nha, an com ngon, cam thay tot hon', now() - interval '5 days 20 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d70', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 76, 'Sang: Thuc day sau khi ngu du, tam trang tot', now() - interval '4 days 8 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d71', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 74, 'Trua: Bai kiem tra Ly, cam thay binh tinh', now() - interval '4 days 12 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d72', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 77, 'Toi: Gap ban, di bo, cam thay vui ve', now() - interval '4 days 20 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d73', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 65, 'Sang: Ngu khong du, cam thay met moi', now() - interval '3 days 8 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d74', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 55, 'Trua: Lo lang ve ky thi cuoi ky, cang thang', now() - interval '3 days 12 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d75', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 62, 'Toi: Hoc ky, co grang chuan bi tot', now() - interval '3 days 20 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d76', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 82, 'Sang: Tinh day, nghe tin vui - ket qua Toan tot', now() - interval '2 days 8 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d77', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 88, 'Trua: Chia se ket qua voi ban be, cam thay kieu hanh', now() - interval '2 days 12 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d78', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 85, 'Toi: Kheo leo lam cong viec, cam thay tu tin', now() - interval '2 days 20 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d79', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 85, 'Sang: Cam thay vui ve, san sang cho mot ngay moi', now() - interval '1 day 8 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d7a', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 90, 'Trua: Gap ban be, di choi cong vien, cuc ky vui', now() - interval '1 day 12 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d7b', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 88, 'Toi: Quay ve nha vui ve, an com ngon', now() - interval '1 day 20 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d7c', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 78, 'Sang: Ngu du, tam trang on dinh', now() - interval '8 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d7d', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 75, 'Trua: Hoan thanh bai tap Van, cam thay binh tinh', now() - interval '12 hours', now(), now()),
    ('4b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d7e', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 80, 'Toi: Rut kinh nghiem tuan vua qua, chuan bi cho tuan sau', now() - interval '20 hours', now(), now());

-- Food logs (1 per day x 7 days)
INSERT INTO food_logs (food_id, profile_id, water_glasses, food_description, satiety_level, entry_date, created_at, updated_at)
VALUES
    ('5c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7a', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 6, 'Sang: Com trang, trung chien, rau. Trua: Com ga voi dua chuot. Toi: Mi tom voi rau cai', 'SATISFIED', CURRENT_DATE - 6, now(), now()),
    ('5c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7b', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 5, 'Sang: Banh mi nong voi trung. Trua: Com + ca hap. Toi: Canh chua voi tom', 'SATISFIED', CURRENT_DATE - 5, now(), now()),
    ('5c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7c', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 7, 'Sang: Sua chua Hy Lap + ngu coc. Trua: Banh mi thit lon. Toi: Com voi thit bo va rau', 'SATISFIED', CURRENT_DATE - 4, now(), now()),
    ('5c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7d', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 6, 'Sang: Com trang. Trua: Banh pizza. Toi: Ga nuong voi khoai lang', 'SATISFIED', CURRENT_DATE - 3, now(), now()),
    ('5c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7e', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 8, 'Sang: Bot yen mach + qua dau tay. Trua: Salad ga + bo. Toi: Com nuc voi gio lua', 'VERY_SATISFIED', CURRENT_DATE - 2, now(), now()),
    ('5c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7f', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 7, 'Sang: Banh xeo voi nuoc cham. Trua: Mi lanh kieu Nhat. Toi: Bop luoc voi nuoc leo', 'SATISFIED', CURRENT_DATE - 1, now(), now()),
    ('5c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e80', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 6, 'Sang: Banh nuoc + sua dac. Trua: Com chien duong chau. Toi: Ca nuong + rau luoc', 'SATISFIED', CURRENT_DATE, now(), now());

-- Sleep logs (1 per day x 7 days)
INSERT INTO sleep_logs (sleep_log_id, profile_id, sleep_start_at, sleep_end_at, duration_minutes, sleep_quality, note, entry_date, logged_at, created_at, updated_at)
VALUES
    ('6d4e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8a', 'e1d0add5-b9c8-57b5-36e6-059991832f17', (CURRENT_DATE - 6)::timestamp + interval '22 hours', (CURRENT_DATE - 6)::timestamp + interval '6 hours', 480, 8, 'Ngu sau, tinh day thoai mai', CURRENT_DATE - 6, now(), now(), now()),
    ('6d4e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8b', 'e1d0add5-b9c8-57b5-36e6-059991832f17', (CURRENT_DATE - 5)::timestamp + interval '23 hours', (CURRENT_DATE - 5)::timestamp + interval '6.5 hours', 420, 6, 'Ngu on, thuc day vai lan', CURRENT_DATE - 5, now(), now(), now()),
    ('6d4e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8c', 'e1d0add5-b9c8-57b5-36e6-059991832f17', (CURRENT_DATE - 4)::timestamp + interval '21.5 hours', (CURRENT_DATE - 4)::timestamp + interval '5.5 hours', 480, 8, 'Ngu sau, giac ngu chat luong cao', CURRENT_DATE - 4, now(), now(), now()),
    ('6d4e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8d', 'e1d0add5-b9c8-57b5-36e6-059991832f17', (CURRENT_DATE - 3)::timestamp + interval '0.5 hours', (CURRENT_DATE - 3)::timestamp + interval '6.5 hours', 360, 5, 'Ngu khong du do hoc khuya', CURRENT_DATE - 3, now(), now(), now()),
    ('6d4e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8e', 'e1d0add5-b9c8-57b5-36e6-059991832f17', (CURRENT_DATE - 2)::timestamp + interval '21 hours', (CURRENT_DATE - 2)::timestamp + interval '5 hours', 480, 7, 'Ngu tot, du giac', CURRENT_DATE - 2, now(), now(), now()),
    ('6d4e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f8f', 'e1d0add5-b9c8-57b5-36e6-059991832f17', (CURRENT_DATE - 1)::timestamp + interval '20.5 hours', (CURRENT_DATE - 1)::timestamp + interval '6 hours', 540, 9, 'Ngu rat tot, giac ngu sau va yen tinh', CURRENT_DATE - 1, now(), now(), now()),
    ('6d4e5f6a-7b8c-9d0e-1f2a-3b4c5d6e7f90', 'e1d0add5-b9c8-57b5-36e6-059991832f17', (CURRENT_DATE)::timestamp + interval '21.5 hours', (CURRENT_DATE)::timestamp + interval '5.5 hours', 480, 8, 'Ngu binh thuong, tinh day luc 5h30', CURRENT_DATE, now(), now(), now());

-- Streaks
INSERT INTO streaks (streak_id, profile_id, streak_type, current_count, longest_count, last_logged_at, created_at, updated_at)
VALUES
    ('7e5f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9b', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'MOOD_LOGGING', 7, 15, now(), now(), now()),
    ('7e5f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9c', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'FOOD_LOGGING', 7, 20, now(), now(), now()),
    ('7e5f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9d', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'SLEEP_LOGGING', 7, 25, now(), now(), now()),
    ('7e5f6a7b-8c9d-0e1f-2a3b-4c5d6e7f8a9e', 'e1d0add5-b9c8-57b5-36e6-059991832f17', 'DIARY_WRITING', 5, 12, now(), now(), now());

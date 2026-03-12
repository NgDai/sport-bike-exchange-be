-- Thêm cột phí (fee) vào bảng transaction (chạy nếu bảng đã tồn tại trước khi thêm field fee)
-- PostgreSQL:
ALTER TABLE public.transaction ADD COLUMN IF NOT EXISTS fee double precision DEFAULT 0;

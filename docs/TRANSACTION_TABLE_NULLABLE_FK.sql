-- Cho phép các cột FK tùy chọn của bảng transaction được NULL.
-- Chạy script này trên PostgreSQL (Supabase) khi gặp lỗi 1030/1031 khi POST /api/transactions.
--
-- 1) Nếu bảng giao dịch của bạn tên là "transaction" (app mặc định), chạy khối dưới đây:

ALTER TABLE "transaction" ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE "transaction" ALTER COLUMN listing_id DROP NOT NULL;
ALTER TABLE "transaction" ALTER COLUMN seller_id DROP NOT NULL;
ALTER TABLE "transaction" ALTER COLUMN deposit_id DROP NOT NULL;
ALTER TABLE "transaction" ALTER COLUMN reservation_id DROP NOT NULL;

-- 2) Nếu trong DB bạn có bảng tên "transaction_fl", chạy thêm (hoặc thay bằng) khối sau:

-- ALTER TABLE transaction_fl ALTER COLUMN event_id DROP NOT NULL;
-- ALTER TABLE transaction_fl ALTER COLUMN listing_id DROP NOT NULL;
-- ALTER TABLE transaction_fl ALTER COLUMN seller_id DROP NOT NULL;
-- ALTER TABLE transaction_fl ALTER COLUMN deposit_id DROP NOT NULL;
-- ALTER TABLE transaction_fl ALTER COLUMN reservation_id DROP NOT NULL;

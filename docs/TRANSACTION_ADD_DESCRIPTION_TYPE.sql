-- Thêm cột description và type vào bảng transaction (giống wallet_transaction)
-- để API có thể trả về đủ trường, không null.
-- Chạy trên PostgreSQL (Supabase) trước khi deploy entity mới.

ALTER TABLE "transaction"
  ADD COLUMN IF NOT EXISTS description VARCHAR(500),
  ADD COLUMN IF NOT EXISTS type VARCHAR(100);

COMMENT ON COLUMN "transaction".description IS 'Mô tả giao dịch (giống wallet_transaction)';
COMMENT ON COLUMN "transaction".type IS 'Loại giao dịch: Deposit, ListingFee, Sale, Fee, ...';

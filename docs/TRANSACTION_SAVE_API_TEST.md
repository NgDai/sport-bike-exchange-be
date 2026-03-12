# Test API lưu Transaction bằng Postman (POST /transactions)

Hướng dẫn **test API tạo/lưu transaction** (POST `/api/transactions`) **bằng Postman**.

---

## Thông tin chung

| Mục | Giá trị |
|-----|---------|
| **Base URL** | `http://localhost:8080/api` |
| **Endpoint** | `POST /transactions` |
| **Quyền** | Đã đăng nhập (Bearer token) |
| **Content-Type** | `application/json` |

---

## Bước 1: Lấy token trong Postman

1. Tạo request mới trong Postman.
2. **Method:** `POST`
3. **URL:** `http://localhost:8080/api/auth/login`
4. Vào tab **Headers** → đảm bảo có `Content-Type: application/json` (hoặc chọn Body → raw → JSON thì Postman tự thêm).
5. Vào tab **Body** → chọn **raw** → **JSON**, dán:

```json
{
  "username": "admin",
  "password": "1"
}
```

6. Bấm **Send**.
7. Trong response, copy giá trị `result.token` (chuỗi JWT) để dùng cho Bước 2.

---

## Bước 2: Test lưu Transaction trong Postman

### Cấu hình request

1. Tạo request mới (hoặc duplicate request từ Bước 1 rồi sửa).
2. **Method:** `POST`
3. **URL:** `http://localhost:8080/api/transactions`
4. **Authorization:** Tab **Authorization** → Type chọn **Bearer Token** → ô Token dán token đã copy ở Bước 1.
5. **Body:** Tab **Body** → chọn **raw** → **JSON**, dán một trong hai mẫu dưới.

### Quy ước khi test API

- **Chỉ cần gửi:** `amount`, `actualPrice` (và tùy chọn `fee`).
- **Không gửi:** các trường Id (`eventId`, `listingId`, `buyerId`, `sellerId`, `depositId`, `reservationId`) và **không gửi** `status`.
- **Status** do backend lấy từ database: nếu có `depositId` → lấy từ Deposit; nếu có `reservationId` (không có deposit) → lấy từ Reservation; không có cả hai → mặc định `PENDING`.

### Body mẫu – Chỉ amount và actualPrice

```json
{
  "amount": 5000000,
  "actualPrice": 5000000
}
```

### Body mẫu – Có thêm phí nền tảng (tùy chọn)

```json
{
  "amount": 5000000,
  "actualPrice": 5000000,
  "fee": 0
}
```

6. Bấm **Send** → kỳ vọng **200 OK**.

---

## Response thành công (200)

```json
{
  "code": 1000,
  "message": "Transaction created successfully",
  "result": {
    "transactionId": 1,
    "buyerId": null,
    "sellerId": null,
    "listingId": null,
    "depositId": null,
    "reservationId": null,
    "amount": 5000000.0,
    "actualPrice": 5000000.0,
    "fee": 0.0,
    "status": "PENDING",
    "createdAt": "2026-03-12T...",
    "updatedAt": "2026-03-12T..."
  }
}
```

- Transaction đã được **lưu** vào DB. **Status** do backend set: lấy từ Deposit/Reservation nếu có gửi `depositId`/`reservationId`, không thì mặc định `PENDING`.
- Ghi lại `result.transactionId` để test GET/PUT sau (ví dụ GET `http://localhost:8080/api/transactions/1`).

---

## Body request khi test API (Postman)

| Field | Bắt buộc khi test | Ghi chú |
|-------|-------------------|--------|
| amount | Có | Số tiền (double) |
| actualPrice | Có | Giá thực tế (double) |
| fee | Không | Tùy chọn; mặc định 0 |
| eventId, listingId, buyerId, sellerId, depositId, reservationId | Không | Khi test không gửi; backend để null |
| status | Không gửi | Backend lấy từ DB (Deposit/Reservation) hoặc mặc định PENDING |

---

## Lỗi thường gặp (Postman)

| HTTP | Ý nghĩa |
|------|---------|
| **401** | Thiếu token hoặc token hết hạn → Bước 1 login lại, copy token mới. |
| **403** | User không có quyền (endpoint yêu cầu đăng nhập). Gửi đúng **Authorization: Bearer &lt;token&gt;** từ login. |
| **400** | Dữ liệu không hợp lệ (VD: deposit đã có transaction). |
| **400 (code 1031)** | **Không thể lưu giao dịch** – bảng `transaction` đang bắt buộc NOT NULL với `event_id`, `listing_id`, `seller_id`, … Trong trường hợp này **body request hợp lệ**, nhưng DB từ chối insert. **Cách sửa:** chạy script SQL trong `docs/TRANSACTION_TABLE_NULLABLE_FK.sql` trên database (Supabase/PostgreSQL). |
| **500** | Lỗi server. Xem console/log ứng dụng Spring. |

- **Lỗi "Yêu cầu không hợp lệ" (1030/1031) khi chỉ gửi amount/actualPrice/fee:** thường do bảng `transaction` có cột FK đang NOT NULL. **Ứng dụng tự chạy ALTER TABLE khi khởi động** (xem `TransactionTableMigrationRunner`). Nếu vẫn lỗi, chạy thủ công file `docs/TRANSACTION_TABLE_NULLABLE_FK.sql` trên DB (đúng bảng `transaction`) rồi **restart app** và gửi lại request.

---

## Test thêm: Luồng Đặt cọc (Postman)

Nếu muốn test **luồng đặt cọc** (tạo deposit, backend tự tạo transaction):

1. **POST** `http://localhost:8080/api/deposits` (cùng token).
2. Tab **Body** → **raw** → **JSON**:

```json
{
  "type": "COC",
  "amount": 5000000,
  "status": "PENDING",
  "listingId": 1,
  "actualPrice": 5000000
}
```

3. **Send** → backend lưu deposit và tự tạo transaction (PENDING). Không cần gọi POST `/api/transactions` riêng trong luồng này.

---

## Checklist test nhanh (Postman)

1. **POST** `/api/auth/login` trong Postman → copy `result.token`.
2. **POST** `/api/transactions`: Authorization = Bearer Token (dán token), Body raw JSON `{"amount": 5000000, "actualPrice": 5000000}` → **Send**.
3. Kiểm tra response **200** và `result.transactionId` có giá trị.
4. **GET** `http://localhost:8080/api/transactions/{transactionId}` (cùng token) → kiểm tra dữ liệu trùng với vừa tạo.

# Hướng dẫn test API Transaction

Base URL: **`http://localhost:8080/api`**

Tất cả API Transaction (trừ **POST tạo mới**) yêu cầu đăng nhập với user có **role ADMIN**. User mặc định: **username `admin`, password `1`**.

---

## 1. Chuẩn bị: Đăng nhập lấy token

Dùng token cho mọi request bên dưới (header `Authorization: Bearer <token>`).

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/auth/login` |
| **Headers** | `Content-Type: application/json` |
| **Body (JSON)** | `{"username":"admin","password":"1"}` |

**Response mẫu:** Lấy `result.token` (JWT).

```json
{
  "code": 1000,
  "message": null,
  "result": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

Trong Postman: Tab **Authorization** → Type **Bearer Token** → dán token vào ô Token (hoặc dùng biến `{{auth_token}}` nếu đã set từ request Login).

---

## 2. Các endpoint và quyền

| Method | Endpoint | Quyền | Mô tả |
|--------|----------|--------|--------|
| POST | `/transactions` | Đã đăng nhập | Tạo transaction mới |
| GET | `/transactions` | **ADMIN** | Lấy danh sách tất cả transaction |
| GET | `/transactions/{transactionId}` | **ADMIN** | Lấy chi tiết một transaction |
| PUT | `/transactions/{transactionId}` | **ADMIN** | Cập nhật transaction |
| DELETE | `/transactions/{transactionId}` | **ADMIN** | Xóa transaction |
| GET | `/transactions/status/{status}` | **ADMIN** | Lấy danh sách theo trạng thái (PENDING, COMPLETED, …) |

---

## 3. Test từng API

### 3.1. Tạo transaction (POST /transactions)

**Cách test API:** Chỉ cần gửi **amount** và **actualPrice**. Không gửi các trường Id (`eventId`, `listingId`, `buyerId`, `sellerId`, `depositId`, `reservationId`) và **không gửi** `status`. Backend lấy status từ database (Deposit/Reservation nếu có, mặc định PENDING).

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/transactions` |
| **Headers** | `Content-Type: application/json`<br>`Authorization: Bearer <token>` |
| **Body (raw, JSON)** | Xem bên dưới |

**Body mẫu – Test API (chỉ amount và actualPrice):**

```json
{
  "amount": 5000000,
  "actualPrice": 5000000
}
```

**Body mẫu – Có fee (tùy chọn):**

```json
{
  "amount": 5000000,
  "actualPrice": 5000000,
  "fee": 0
}
```

- Khi test: không gửi bất kỳ Id nào và không gửi status; backend để các Id = null, status = PENDING (hoặc lấy từ Deposit/Reservation nếu sau này gửi depositId/reservationId).
- Nếu gửi `depositId`: deposit phải tồn tại và **chưa** có transaction (nếu đã có → 400). Khi có depositId, status transaction sẽ lấy từ `Deposit.status` trong DB.
- Nếu gửi `reservationId` (không có deposit): status lấy từ `Reservation.status` trong DB.

**Response thành công (200):**

```json
{
  "code": 1000,
  "message": "Transaction created successfully",
  "result": {
    "transactionId": 153,
    "buyerId": 1452,
    "depositId": 11,
    "eventId": 652,
    "reservationId": 15,
    "sellerId": 1602,
    "listingId": 1,
    "amount": 5000000.0,
    "actualPrice": 5000000.0,
    "createdAt": "2026-03-09T...",
    "updatedAt": "2026-03-09T...",
    "status": "PENDING"
  }
}
```

Ghi lại **`result.transactionId`** (ví dụ `153`) để dùng cho GET/PUT/DELETE bên dưới.

---

### 3.2. Lấy tất cả transaction (GET /transactions) – ADMIN

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/transactions` |
| **Headers** | `Authorization: Bearer <token>` (user **admin**) |

**Response thành công (200):**

```json
{
  "code": 1000,
  "message": "Transactions fetched successfully",
  "result": [
    {
      "transactionId": 152,
      "buyerId": 1452,
      "depositId": 10,
      ...
    },
    {
      "transactionId": 153,
      "buyerId": 1452,
      "depositId": 11,
      ...
    }
  ]
}
```

---

### 3.3. Lấy một transaction theo ID (GET /transactions/{transactionId}) – ADMIN

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/transactions/153` |
| **Headers** | `Authorization: Bearer <token>` (admin) |

Thay `153` bằng `transactionId` thật (từ bước 3.1 hoặc từ GET all).

**Response thành công (200):**

```json
{
  "code": 1000,
  "message": "Transaction fetched successfully",
  "result": {
    "transactionId": 153,
    "buyerId": 1452,
    "depositId": 11,
    "eventId": 652,
    "reservationId": 15,
    "sellerId": 1602,
    "listingId": 1,
    "amount": 5000000.0,
    "actualPrice": 5000000.0,
    "createdAt": "2026-03-09T...",
    "updatedAt": "2026-03-09T...",
    "status": "PENDING"
  }
}
```

**Lỗi khi không tìm thấy (404 hoặc 4xx):** Message kiểu "Không tìm thấy giao dịch" (TRANSACTION_NOT_FOUND).

---

### 3.4. Cập nhật transaction (PUT /transactions/{transactionId}) – ADMIN

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `PUT` |
| **URL** | `http://localhost:8080/api/transactions/153` |
| **Headers** | `Content-Type: application/json`<br>`Authorization: Bearer <token>` (admin) |
| **Body (JSON)** | Chỉ gửi các field cần đổi (có thể gửi một phần). |

**Body mẫu – đổi status sang COMPLETED:**

```json
{
  "status": "COMPLETED"
}
```

**Body mẫu – đổi nhiều field:**

```json
{
  "status": "COMPLETED",
  "amount": 5500000,
  "actualPrice": 5500000
}
```

Các field có thể cập nhật: `eventId`, `listingId`, `buyerId`, `sellerId`, `depositId`, `reservationId`, `status`, `amount`, `actualPrice`. Nếu đổi `depositId` sang deposit **đã có transaction khác** sẽ lỗi duplicate (DB constraint).

**Response thành công (200):**

```json
{
  "code": 1000,
  "message": "Transaction updated successfully",
  "result": {
    "transactionId": 153,
    "buyerId": 1452,
    "depositId": 11,
    "eventId": 652,
    "reservationId": 15,
    "sellerId": 1602,
    "listingId": 1,
    "amount": 5500000.0,
    "actualPrice": 5500000.0,
    "createdAt": "2026-03-09T...",
    "updatedAt": "2026-03-09T...",
    "status": "COMPLETED"
  }
}
```

---

### 3.5. Lấy transaction theo trạng thái (GET /transactions/status/{status}) – ADMIN

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/transactions/status/COMPLETED` |
| **Headers** | `Authorization: Bearer <token>` (admin) |

Có thể thay `COMPLETED` bằng `PENDING` hoặc status khác tùy dữ liệu.

**Response thành công (200):** Mảng transaction có `status` tương ứng.

```json
{
  "code": 1000,
  "message": "Transactions fetched successfully",
  "result": [
    {
      "transactionId": 152,
      "status": "COMPLETED",
      ...
    },
    {
      "transactionId": 153,
      "status": "COMPLETED",
      ...
    }
  ]
}
```

---

### 3.6. Xóa transaction (DELETE /transactions/{transactionId}) – ADMIN

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `DELETE` |
| **URL** | `http://localhost:8080/api/transactions/153` |
| **Headers** | `Authorization: Bearer <token>` (admin) |

**Response thành công (200):**

```json
{
  "code": 1000,
  "message": null,
  "result": "Transaction deleted successfully"
}
```

Sau khi xóa, gọi lại GET `/transactions/153` sẽ trả lỗi (transaction không tồn tại).

---

## 4. Checklist test nhanh (Postman)

1. **POST** `/api/auth/login` với `{"username":"admin","password":"1"}` → copy token.
2. **POST** `/api/transactions` với body **chỉ** `{"amount": 5000000, "actualPrice": 5000000}` → kiểm tra 200, lưu `result.transactionId`.
3. **GET** `/api/transactions` (cùng token admin) → kiểm tra list có transaction vừa tạo.
4. **GET** `/api/transactions/{transactionId}` → kiểm tra chi tiết đúng.
5. **PUT** `/api/transactions/{transactionId}` với `{"status":"COMPLETED"}` → kiểm tra 200 và `result.status` = COMPLETED.
6. **GET** `/api/transactions/status/COMPLETED` → kiểm tra có transaction vừa cập nhật.
7. (Tùy chọn) **DELETE** `/api/transactions/{transactionId}` → kiểm tra 200; GET lại theo id → lỗi không tìm thấy.

**Test validation:**

- **POST** với `depositId` đã có transaction → mong đợi **400**, code 1028, message *"Đặt cọc này đã có giao dịch, không thể tạo trùng"*.

---

## 5. Dữ liệu test gợi ý (POST /transactions)

Khi **test API** chỉ cần gửi:

| Field | Gợi ý | Ghi chú |
|-------|--------|--------|
| amount | 5000000 | Bắt buộc |
| actualPrice | 5000000 | Bắt buộc |
| fee | 0 | Tùy chọn; mặc định 0 |

**Không gửi khi test:** eventId, listingId, buyerId, sellerId, depositId, reservationId, status. Backend để các Id = null và lấy status từ DB (hoặc mặc định PENDING). Khi tích hợp thật có thể gửi thêm depositId/reservationId để gắn transaction và lấy status từ Deposit/Reservation.

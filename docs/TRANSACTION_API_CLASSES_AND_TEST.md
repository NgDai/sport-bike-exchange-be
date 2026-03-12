# Transaction – Các class liên quan và hướng dẫn test API

Tài liệu này liệt kê **các class có chứa/liên quan Transaction** và **cách test API Transaction** (Postman/cURL). Các **đường link test** bên dưới dùng đúng **transaction_id trong DB** (2, 52) – copy nguyên vào Postman là dùng được.

---

## 1. Các class chứa Transaction

### Entity
| Class | Đường dẫn | Mô tả |
|-------|-----------|--------|
| **Transaction** | `entities/Transaction.java` | Entity giao dịch: transactionId, event, listing, buyer, seller, deposit, reservation, amount, actualPrice, status, createAt, updateAt. |

### Controller
| Class | Đường dẫn | Mô tả |
|-------|-----------|--------|
| **TransactionController** | `controller/TransactionController.java` | REST API: base path **`/transactions`**. Các endpoint: POST (tạo), PUT /{id} (cập nhật), GET (danh sách), GET /{id}, DELETE /{id}, GET /status/{status}. |

### Service
| Class | Đường dẫn | Mô tả |
|-------|-----------|--------|
| **TransactionService** | `services/TransactionService.java` | Nghiệp vụ CRUD: tạo/cập nhật Transaction từ các ID (eventId, listingId, buyerId, sellerId, depositId, reservationId), lấy danh sách, theo ID, theo status, xóa. |

### Repository
| Class | Đường dẫn | Mô tả |
|-------|-----------|--------|
| **ITransactionRepository** | `repository/ITransactionRepository.java` | JpaRepository&lt;Transaction, Integer&gt;, `findAllByStatus(String status)`. |

### DTO & Mapper
| Class | Đường dẫn | Mô tả |
|-------|-----------|--------|
| **TransactionCreationRequest** | `dto/request/TransactionCreationRequest.java` | Body tạo giao dịch: khi test chỉ cần amount, actualPrice (tùy chọn fee); các Id và status không bắt buộc, status lấy từ DB. |
| **TransactionUpdateRequest** | `dto/request/TransactionUpdateRequest.java` | Body cập nhật: cùng các trường trên (nullable). |
| **TransactionResponse** | `dto/response/TransactionResponse.java` | Response: transactionId, buyerId, sellerId, eventId, listingId, depositId, reservationId, amount, actualPrice, createdAt, updatedAt, status. |
| **TransactionMapper** | `mapper/TransactionMapper.java` | Map Entity ↔ Response và áp dụng UpdateRequest lên Entity. |

### Class khác tham chiếu Transaction
| Class | Đường dẫn | Mô tả |
|-------|-----------|--------|
| **Dispute** | `entities/Dispute.java` | Có quan hệ với Transaction. |
| **PaymentController** | `controller/PaymentController.java` | Luồng VNPay; khi thanh toán deal thành công có thể gọi TransactionService để tạo Transaction (xem TRANSACTION.md). |
| **VNPayService** | `services/VNPayService.java` | Xử lý return URL VNPay; kết quả thành công có thể dùng để tạo/cập nhật Transaction. |

---

## 2. Base URL và bảo mật

- **Base URL:** `http://localhost:8080/api` (project dùng **context-path** `server.servlet.context-path=/api` trong `application.properties`).
- **Base path API Transaction:** `http://localhost:8080/api/transactions`.
- **Lưu ý:** Thiếu `/api` trong URL sẽ bị **404 Not Found** (ví dụ `http://localhost:8080/transactions` là sai).
- **Xác thực:** Tất cả endpoint Transaction hiện yêu cầu **đăng nhập** (JWT). Riêng GET `/payments/vnpay-payment` được permitAll (VNPay redirect).
- **Quyền:** Các endpoint trong `TransactionController` dùng `@PreAuthorize("hasRole('ADMIN')")` → cần user có role **ADMIN** và gửi header:
  - `Authorization: Bearer <access_token>`

---

## 2.1. Link test API Transaction – Copy trực tiếp vào Postman

Dùng **user admin** (username `admin`, password `1`) để lấy token, sau đó copy từng URL dưới đây vào ô URL của Postman. **Đã dùng đúng transaction_id trong DB của bạn (2 và 52).**

### Bước 0 – Lấy token (bắt buộc)

| Method | URL đầy đủ (copy nguyên) |
|--------|--------------------------|
| POST | `http://localhost:8080/api/auth/login` |

**Body (raw JSON):**
```json
{"username":"admin","password":"1"}
```

Lấy `result.token` → Postman: **Authorization** → **Bearer Token** → dán token.

---

### Transaction – URL copy từng request

| Mục đích | Method | URL đầy đủ (copy nguyên) |
|----------|--------|--------------------------|
| Tạo giao dịch mới | POST | `http://localhost:8080/api/transactions` |
| Lấy tất cả giao dịch | GET | `http://localhost:8080/api/transactions` |
| Lấy giao dịch theo ID (ví dụ id = 2) | GET | `http://localhost:8080/api/transactions/2` |
| Lấy giao dịch theo ID (ví dụ id = 52) | GET | `http://localhost:8080/api/transactions/52` |
| Cập nhật giao dịch id = 2 | PUT | `http://localhost:8080/api/transactions/2` |
| Cập nhật giao dịch id = 52 | PUT | `http://localhost:8080/api/transactions/52` |
| Lấy giao dịch theo status COMPLETED | GET | `http://localhost:8080/api/transactions/status/COMPLETED` |
| Lấy giao dịch theo status PENDING | GET | `http://localhost:8080/api/transactions/status/PENDING` |
| Xóa giao dịch id = 2 | DELETE | `http://localhost:8080/api/transactions/2` |
| Xóa giao dịch id = 52 | DELETE | `http://localhost:8080/api/transactions/52` |

**Lưu ý:** PUT và DELETE cần **Body** (PUT: JSON; DELETE không cần body). GET không cần Body.

---

## 2.5. Nếu bảng Deposit / Reservation trống – tạo dữ liệu test trước

Khi **test API**: **POST /api/transactions** chỉ cần body `{"amount": ..., "actualPrice": ...}`; không cần depositId, reservationId hay các Id khác. Khi tích hợp thật có thể gửi thêm depositId, reservationId để gắn transaction với deposit/reservation (khi đó status lấy từ DB).

### Cách 1 – Tạo qua API (Postman)

**Bước 1 – Tạo 1 Deposit**

- **POST** `http://localhost:8080/api/deposits`
- **Headers:** `Content-Type: application/json`, `Authorization: Bearer <token>`
- **Body:**

```json
{
  "type": "COC",
  "amount": 1000000,
  "status": "PENDING",
  "createAt": null
}
```

- Response sẽ có `result.depositId` (ví dụ `1`). Ghi lại **depositId** này.

**Bước 2 – Tạo 1 Reservation**

- **POST** `http://localhost:8080/api/reservations`
- **Headers:** `Content-Type: application/json`, `Authorization: Bearer <token>`
- **Body:**

```json
{
  "status": "PENDING",
  "reservedAt": null
}
```

- Response sẽ có `result.reservationId` (ví dụ `1`). Ghi lại **reservationId** này.

**Bước 3 – Tạo Transaction**

- Dùng **depositId** và **reservationId** vừa tạo trong body **POST /api/transactions** (xem mục 3.1).

### Cách 2 – Insert trực tiếp trong Supabase

1. **Bảng `deposit`:** Insert 1 dòng, điền ít nhất: `amount` (số), `status` (vd: `PENDING`), `type` (vd: `COC`). Cột `deposit_id` thường auto-increment. `user_id`, `listing_id` có thể để null nếu bảng cho phép.
2. **Bảng `reservation`:** Insert 1 dòng, điền ít nhất: `status` (vd: `PENDING`). Cột `reservation_id` thường auto-increment. `listing_id`, `buyer_id` có thể để null nếu bảng cho phép.
3. Trong Postman, gửi **POST /api/transactions** với `depositId` và `reservationId` đúng với id vừa tạo.

---

## 3. Full JSON – Test tất cả API liên quan Transaction

Bộ JSON dưới đây dùng **cùng bộ id** để test tuần tự: Deposit → Reservation → Transaction. Thay `depositId`, `reservationId`, `transactionId` bằng id thật nếu khác.

**Bộ id mẫu (đã tạo trong DB):** `eventId: 652`, `listingId: 1`, `buyerId: 1452`, `sellerId: 1602`, `depositId: 10`, `reservationId: 15`. **Transaction đã có:** `transactionId: 2` (amount 5.500.000, actualPrice 5.000.000, COMPLETED), `transactionId: 52` (amount 5.000.000, actualPrice 5.000.000, COMPLETED).

**Headers chung:** `Content-Type: application/json`, `Authorization: Bearer <access_token>`

---

### A. API Deposit (tạo trước khi test Transaction)

| Method | URL | Mô tả |
|--------|-----|--------|
| POST | `/api/deposits` | Tạo đặt cọc |
| PUT | `/api/deposits/{depositId}` | Cập nhật đặt cọc |
| GET | `/api/deposits` | Danh sách đặt cọc |
| GET | `/api/deposits/{depositId}` | Chi tiết một đặt cọc |
| GET | `/api/deposits/status/{status}` | Đặt cọc theo status |
| DELETE | `/api/deposits/{depositId}` | Xóa đặt cọc |

**A.1 – POST /api/deposits (Request)**

```json
{
  "type": "COC",
  "amount": 1000000,
  "status": "PENDING",
  "createAt": null
}
```

**A.2 – POST /api/deposits (Response 200)**

```json
{
  "code": 1000,
  "message": "Deposit created successfully",
  "result": {
    "depositId": 10,
    "userId": 0,
    "listingId": 0,
    "type": "COC",
    "amount": 1000000.0,
    "status": "PENDING",
    "createAt": "2025-03-09T12:00:00.000+00:00"
  }
}
```

**A.3 – PUT /api/deposits/10 (Request)**

```json
{
  "type": "COC",
  "amount": 1500000,
  "status": "CONFIRMED",
  "createAt": null
}
```

**A.4 – GET /api/deposits/10 (Response 200)**

```json
{
  "code": 1000,
  "message": "Deposit fetched successfully",
  "result": {
    "depositId": 10,
    "userId": 1452,
    "listingId": 1,
    "type": "COC",
    "amount": 1000000.0,
    "status": "PENDING",
    "createAt": "2025-03-09T12:00:00.000+00:00"
  }
}
```

---

### B. API Reservation (tạo trước khi test Transaction)

| Method | URL | Mô tả |
|--------|-----|--------|
| POST | `/api/reservations` | Tạo đặt chỗ |
| PUT | `/api/reservations/{reservationId}` | Cập nhật đặt chỗ |
| GET | `/api/reservations` | Danh sách đặt chỗ |
| GET | `/api/reservations/{reservationId}` | Chi tiết một đặt chỗ |
| GET | `/api/reservations/status/{status}` | Đặt chỗ theo status |
| DELETE | `/api/reservations/{reservationId}` | Xóa đặt chỗ |

**B.1 – POST /api/reservations (Request)**

```json
{
  "status": "PENDING",
  "reservedAt": null
}
```

**B.2 – POST /api/reservations (Response 200)**

```json
{
  "code": 1000,
  "message": "Reservation created successfully",
  "result": {
    "reservationId": 15,
    "bikeListingId": 1,
    "buyerId": 1452,
    "status": "PENDING",
    "reservedAt": "2025-03-09T12:00:00.000+00:00"
  }
}
```

**B.3 – PUT /api/reservations/15 (Request)**

```json
{
  "status": "CONFIRMED",
  "reservedAt": null
}
```

**B.4 – GET /api/reservations/15 (Response 200)**

```json
{
  "code": 1000,
  "message": "Reservation fetched successfully",
  "result": {
    "reservationId": 15,
    "bikeListingId": 1,
    "buyerId": 1452,
    "status": "PENDING",
    "reservedAt": "2025-03-09T12:00:00.000+00:00"
  }
}
```

---

### C. API Transaction (test: chỉ cần amount, actualPrice)

| Method | URL copy (full) | Mô tả |
|--------|-----------------|--------|
| POST | `http://localhost:8080/api/transactions` | Tạo giao dịch |
| PUT | `http://localhost:8080/api/transactions/2` hoặc `.../52` | Cập nhật giao dịch |
| GET | `http://localhost:8080/api/transactions` | Danh sách giao dịch |
| GET | `http://localhost:8080/api/transactions/2` hoặc `.../52` | Chi tiết một giao dịch |
| GET | `http://localhost:8080/api/transactions/status/COMPLETED` | Giao dịch theo status |
| DELETE | `http://localhost:8080/api/transactions/2` hoặc `.../52` | Xóa giao dịch |

**C.1 – POST /api/transactions (Request) – Test API**

**URL copy:** `http://localhost:8080/api/transactions`

Khi test chỉ gửi **amount** và **actualPrice**; không gửi các trường Id và không gửi status (status do backend lấy từ DB).

```json
{
  "amount": 5000000,
  "actualPrice": 5000000
}
```

Tùy chọn: thêm `"fee": 0`. Các trường eventId, listingId, buyerId, sellerId, depositId, reservationId, status không cần gửi khi test.

**C.2 – POST /api/transactions (Response 200)**

Khi test với body chỉ amount + actualPrice, các Id trong result sẽ null; status do backend set (PENDING hoặc lấy từ Deposit/Reservation nếu có gửi).

```json
{
  "code": 1000,
  "message": "Transaction created successfully",
  "result": {
    "transactionId": 52,
    "buyerId": null,
    "depositId": null,
    "eventId": null,
    "reservationId": null,
    "sellerId": null,
    "listingId": null,
    "amount": 5000000.0,
    "actualPrice": 5000000.0,
    "createdAt": "...",
    "updatedAt": "...",
    "status": "PENDING"
  }
}
```

**C.3 – PUT /api/transactions/2 hoặc /api/transactions/52 (Request – chỉ gửi trường cần đổi)**

**URL copy:** `http://localhost:8080/api/transactions/2` hoặc `http://localhost:8080/api/transactions/52`

```json
{
  "status": "COMPLETED",
  "amount": 5500000,
  "actualPrice": 5000000
}
```

**C.4 – PUT /api/transactions/52 (Request – đầy đủ)**

**URL copy:** `http://localhost:8080/api/transactions/52`

```json
{
  "eventId": 652,
  "listingId": 1,
  "buyerId": 1452,
  "sellerId": 1602,
  "depositId": 10,
  "reservationId": 15,
  "status": "COMPLETED",
  "amount": 6000000,
  "actualPrice": 6000000
}
```

**C.5 – GET /api/transactions/2 hoặc GET /api/transactions/52 (Response 200)**

**URL copy (theo ID):**  
- `http://localhost:8080/api/transactions/2`  
- `http://localhost:8080/api/transactions/52`

Ví dụ response transaction id = 2 (đã cập nhật amount):
```json
{
  "code": 1000,
  "message": "Transaction fetched successfully",
  "result": {
    "transactionId": 2,
    "buyerId": 1452,
    "depositId": 10,
    "eventId": 652,
    "reservationId": 15,
    "sellerId": 1602,
    "listingId": 1,
    "amount": 5500000.0,
    "actualPrice": 5000000.0,
    "createdAt": "2026-03-09T09:25:27.863+00:00",
    "updatedAt": "2026-03-09T15:47:09.748+00:00",
    "status": "COMPLETED"
  }
}
```

Ví dụ response transaction id = 52:
```json
{
  "code": 1000,
  "message": "Transaction fetched successfully",
  "result": {
    "transactionId": 52,
    "buyerId": 1452,
    "depositId": 10,
    "eventId": 652,
    "reservationId": 15,
    "sellerId": 1602,
    "listingId": 1,
    "amount": 5000000.0,
    "actualPrice": 5000000.0,
    "createdAt": "2026-03-09T09:43:39.908+00:00",
    "updatedAt": "2026-03-09T09:43:39.908+00:00",
    "status": "COMPLETED"
  }
}
```

**C.6 – GET /api/transactions (Response 200 – danh sách)**

**URL copy:** `http://localhost:8080/api/transactions`

```json
{
  "code": 1000,
  "message": "Transactions fetched successfully",
  "result": [
    {
      "transactionId": 2,
      "buyerId": 1452,
      "depositId": 10,
      "eventId": 652,
      "reservationId": 15,
      "sellerId": 1602,
      "listingId": 1,
      "amount": 5500000.0,
      "actualPrice": 5000000.0,
      "createdAt": "2026-03-09T09:25:27.863+00:00",
      "updatedAt": "2026-03-09T15:47:09.748+00:00",
      "status": "COMPLETED"
    },
    {
      "transactionId": 52,
      "buyerId": 1452,
      "depositId": 10,
      "eventId": 652,
      "reservationId": 15,
      "sellerId": 1602,
      "listingId": 1,
      "amount": 5000000.0,
      "actualPrice": 5000000.0,
      "createdAt": "2026-03-09T09:43:39.908+00:00",
      "updatedAt": "2026-03-09T09:43:39.908+00:00",
      "status": "COMPLETED"
    }
  ]
}
```

**C.7 – GET /api/transactions/status/COMPLETED (Response 200)**

**URL copy:** `http://localhost:8080/api/transactions/status/COMPLETED`

```json
{
  "code": 1000,
  "message": "Transactions fetched successfully",
  "result": [
    {
      "transactionId": 2,
      "buyerId": 1452,
      "depositId": 10,
      "eventId": 652,
      "reservationId": 15,
      "sellerId": 1602,
      "listingId": 1,
      "amount": 5500000.0,
      "actualPrice": 5000000.0,
      "createdAt": "2026-03-09T09:25:27.863+00:00",
      "updatedAt": "2026-03-09T15:47:09.748+00:00",
      "status": "COMPLETED"
    },
    {
      "transactionId": 52,
      "buyerId": 1452,
      "depositId": 10,
      "eventId": 652,
      "reservationId": 15,
      "sellerId": 1602,
      "listingId": 1,
      "amount": 5000000.0,
      "actualPrice": 5000000.0,
      "createdAt": "2026-03-09T09:43:39.908+00:00",
      "updatedAt": "2026-03-09T09:43:39.908+00:00",
      "status": "COMPLETED"
    }
  ]
}
```

**C.8 – DELETE /api/transactions/2 hoặc /api/transactions/52 (Response 200)**

**URL copy:** `http://localhost:8080/api/transactions/2` hoặc `http://localhost:8080/api/transactions/52`

```json
{
  "code": 1000,
  "message": null,
  "result": "Transaction deleted successfully"
}
```

**Thứ tự test gợi ý (copy URL từ bảng mục 2.1):**  
1) **POST** `http://localhost:8080/api/auth/login` → lấy token.  
2) **POST** `http://localhost:8080/api/transactions` (body C.1: chỉ amount, actualPrice) → lấy `result.transactionId`.  
3) **GET** `http://localhost:8080/api/transactions/2`, **GET** `http://localhost:8080/api/transactions/52`, **GET** `http://localhost:8080/api/transactions`, **GET** `http://localhost:8080/api/transactions/status/COMPLETED`.  
4) **PUT** `http://localhost:8080/api/transactions/2` hoặc `http://localhost:8080/api/transactions/52` (body C.3 hoặc C.4) nếu cần.  
5) **DELETE** `http://localhost:8080/api/transactions/2` hoặc `http://localhost:8080/api/transactions/52` (C.8) nếu cần.

---

## 3B. Tất cả JSON đầy đủ để test Transaction (tham chiếu nhanh)

Dùng các JSON sau trong Postman/Insomnia (thay giá trị id bằng dữ liệu thật trong DB của bạn).

### 3.1 Request – Tạo giao dịch (POST)

**URL copy (dán vào Postman):** `http://localhost:8080/api/transactions`

```json
{
  "eventId": 652,
  "listingId": 1,
  "buyerId": 1452,
  "sellerId": 1602,
  "depositId": 10,
  "reservationId": 15,
  "status": "COMPLETED",
  "amount": 5000000,
  "actualPrice": 5000000
}
```

| Trường | Bắt buộc | Ghi chú |
|--------|----------|--------|
| eventId | Không | ID sự kiện (bảng Events). Có thể `null`. |
| listingId | Không | ID bài đăng (bảng BikeListing). Có thể `null`. |
| buyerId | Không | ID người mua (user_id bảng Users). Có thể `null`. |
| sellerId | Không | ID người bán (user_id bảng Users). Có thể `null`. |
| **depositId** | **Có** | ID đặt cọc (bảng Deposit). Không được `null`. |
| **reservationId** | **Có** | ID đặt chỗ (bảng Reservation). Không được `null`. |
| status | Không | Ví dụ: `PENDING`, `COMPLETED`. Mặc định `PENDING` nếu null. |
| amount | Có (số) | Số tiền giao dịch. |
| actualPrice | Có (số) | Giá thực tế. |

---

### 3.2 Request – Cập nhật giao dịch (PUT)

**URL copy (thay số 2 hoặc 52 bằng transactionId thật):**  
- `http://localhost:8080/api/transactions/2`  
- `http://localhost:8080/api/transactions/52`

Chỉ gửi các trường cần đổi (các trường không gửi giữ nguyên).

**Ví dụ 1 – Chỉ đổi status và amount:**

```json
{
  "status": "COMPLETED",
  "amount": 5500000
}
```

**Ví dụ 2 – Đổi đầy đủ (tất cả trường tùy chọn):**

```json
{
  "eventId": 652,
  "listingId": 1,
  "buyerId": 1452,
  "sellerId": 1602,
  "depositId": 10,
  "reservationId": 15,
  "status": "COMPLETED",
  "amount": 6000000,
  "actualPrice": 6000000
}
```

---

### 3.3 Response – Tạo thành công (POST 200)

```json
{
  "code": 1000,
  "message": "Transaction created successfully",
  "result": {
    "transactionId": 52,
    "buyerId": 1452,
    "depositId": 10,
    "eventId": 652,
    "reservationId": 15,
    "sellerId": 1602,
    "listingId": 1,
    "amount": 5000000.0,
    "actualPrice": 5000000.0,
    "createdAt": null,
    "updatedAt": null,
    "status": "COMPLETED"
  }
}
```

---

### 3.4 Response – Lấy một giao dịch (GET by id 200)

```json
{
  "code": 1000,
  "message": "Transaction fetched successfully",
  "result": {
    "transactionId": 52,
    "buyerId": 1452,
    "depositId": 10,
    "eventId": 652,
    "reservationId": 15,
    "sellerId": 1602,
    "listingId": 1,
    "amount": 5000000.0,
    "actualPrice": 5000000.0,
    "createdAt": null,
    "updatedAt": null,
    "status": "COMPLETED"
  }
}
```

---

### 3.5 Response – Danh sách giao dịch (GET list 200)

```json
{
  "code": 1000,
  "message": "Transactions fetched successfully",
  "result": [
    {
      "transactionId": 52,
      "buyerId": 1452,
      "depositId": 10,
      "eventId": 652,
      "reservationId": 15,
      "sellerId": 1602,
      "listingId": 1,
      "amount": 5000000.0,
      "actualPrice": 5000000.0,
      "createdAt": null,
      "updatedAt": null,
      "status": "COMPLETED"
    }
  ]
}
```

---

### 3.6 Response – Lỗi (400 / 404)

**Thiếu depositId hoặc reservationId (400):**

```json
{
  "code": 1004,
  "message": "Mã đặt cọc (depositId) là bắt buộc khi tạo giao dịch"
}
```

**Không tìm thấy tài nguyên (404 / 400):**

```json
{
  "code": 1002,
  "message": "Không tìm thấy người dùng"
}
```

```json
{
  "code": 1011,
  "message": "Không tìm thấy bản ghi đặt cọc"
}
```

```json
{
  "code": 1015,
  "message": "Không tìm thấy đặt chỗ"
}
```

```json
{
  "code": 1016,
  "message": "Không tìm thấy giao dịch"
}
```

**Đặt cọc đã có giao dịch – không thể tạo trùng (400):**

```json
{
  "code": 1028,
  "message": "Đặt cọc này đã có giao dịch, không thể tạo trùng"
}
```

---

### 3.7 Response – Xóa giao dịch (DELETE 200)

```json
{
  "code": 1000,
  "message": null,
  "result": "Transaction deleted successfully"
}
```

---

## 4. Cách test từng endpoint (step-by-step)

### Bước 0 – Lấy JWT

Gọi API login, lấy `access_token` → header:

```
Authorization: Bearer <access_token>
```

Postman: **Authorization** → Type **Bearer Token** → dán token.

---

### 1) Tạo Transaction – POST /api/transactions

**Method:** `POST`  
**URL (copy nguyên):** `http://localhost:8080/api/transactions`  
**Headers:** `Content-Type: application/json`, `Authorization: Bearer <access_token>`

**Body:** Dùng JSON ở **mục C.1** (chỉ amount, actualPrice khi test).

**cURL (test – chỉ amount, actualPrice):**

```bash
curl -X POST "http://localhost:8080/api/transactions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d "{\"amount\":5000000,\"actualPrice\":5000000}"
```

---

### 2) Cập nhật Transaction – PUT /api/transactions/{transactionId}

**Method:** `PUT`  
**URL (copy – dùng id 2 hoặc 52):** `http://localhost:8080/api/transactions/2` hoặc `http://localhost:8080/api/transactions/52`  
**Headers:** `Content-Type: application/json`, `Authorization: Bearer <access_token>`

**Body:** Dùng JSON ở **mục 3.2** (chỉ gửi trường cần đổi hoặc đầy đủ).

**cURL:**

```bash
curl -X PUT "http://localhost:8080/api/transactions/52" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d "{\"status\":\"COMPLETED\",\"amount\":5500000}"
```

---

### 3) Lấy tất cả Transaction – GET /api/transactions

**Method:** `GET`  
**URL (copy nguyên):** `http://localhost:8080/api/transactions`  
**Headers:** `Authorization: Bearer <access_token>`

**cURL mẫu:**

```bash
curl -X GET "http://localhost:8080/api/transactions" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 4) Lấy một Transaction theo ID – GET /api/transactions/{transactionId}

**Method:** `GET`  
**URL (copy – id 2 hoặc 52):** `http://localhost:8080/api/transactions/2` hoặc `http://localhost:8080/api/transactions/52`  
**Headers:** `Authorization: Bearer <access_token>`

**cURL mẫu:**

```bash
curl -X GET "http://localhost:8080/api/transactions/52" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 5) Lấy Transaction theo status – GET /api/transactions/status/{status}

**Method:** `GET`  
**URL (copy – COMPLETED hoặc PENDING):** `http://localhost:8080/api/transactions/status/COMPLETED` hoặc `http://localhost:8080/api/transactions/status/PENDING`  
**Headers:** `Authorization: Bearer <access_token>`

**cURL mẫu:**

```bash
curl -X GET "http://localhost:8080/api/transactions/status/COMPLETED" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 6) Xóa Transaction – DELETE /api/transactions/{transactionId}

**Method:** `DELETE`  
**URL (copy – id 2 hoặc 52):** `http://localhost:8080/api/transactions/2` hoặc `http://localhost:8080/api/transactions/52`  
**Headers:** `Authorization: Bearer <access_token>`

**cURL mẫu:**

```bash
curl -X DELETE "http://localhost:8080/api/transactions/52" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 5. Response chung

- **Thành công:** Body bọc trong `ApiResponse`: `code`, `message`, `result` (xem mục 3.3, 3.4, 3.5).
- **Lỗi:** HTTP 4xx/5xx, body có `code` và `message` (xem mục 3.6).

---

## 6. Luồng kết nối VNPay → Transaction

Khi triển khai đầy đủ theo `TRANSACTION.md` và `TRANSACTION_IMPLEMENTATION_PLAN.md`:

- **POST /payments/submitOrder:** Tạo đơn thanh toán (PaymentOrder) + URL VNPay.
- **GET /payments/vnpay-payment:** VNPay redirect về; khi thanh toán thành công (paymentStatus == 1), backend có thể gọi `TransactionService.createTransaction(...)` với dữ liệu từ đơn (listingId, buyerId, sellerId, depositId, reservationId, eventId, amount, …).

Test Transaction trực tiếp qua **POST /api/transactions** và **PUT /api/transactions/{id}** như trên là đủ để kiểm tra CRUD Transaction độc lập; test luồng VNPay cần thêm PaymentOrder và xử lý callback như trong tài liệu luồng.

# API test các luồng (flow) chính Transaction

Tài liệu này liệt kê **API cần gọi theo thứ tự** để test từng bước trong flow giao dịch. **Xe đã lên sàn (BikeListing) = seller đã đồng ý bán**, nên không có bước "seller đồng ý/từ chối". Flow: người mua gửi yêu cầu + đặt cọc → Admin cập nhật giao dịch.

---

## Chuẩn bị

| Mục | Giá trị |
|-----|----------|
| **Base URL** | `http://localhost:8080/api` |
| **Context path** | Đã gồm trong Base URL (`/api`) |
| **Auth** | JWT: header `Authorization: Bearer <access_token>` |
| **Lấy token** | `POST /api/auth/login` với body `{"username":"...","password":"..."}` → dùng `result.token` |

**Dữ liệu cần có sẵn trong DB (hoặc tạo trước):**

- Một **BikeListing** (listingId) do seller đăng.
- Một **Reservation** (reservationId) của buyer cho listing đó (nếu backend hỗ trợ tạo qua API).
- Một **Deposit** (depositId) của buyer cho listing đó (tạo qua `POST /api/deposits` nếu có).
- **buyerId**, **sellerId** (userId của người mua và người bán).
- (Tuỳ chọn) **eventId** nếu gắn event.
- User **ADMIN** (để test admin update).

---

## Flow 1a: Đặt cọc → tự tạo Transaction (một bước)

**Luồng mới:** Khi buyer gọi **POST /api/deposits** với `listingId` và `reservationId`, backend sẽ:
1. Tạo Deposit (gắn user = người đăng nhập, listing từ `listingId`).
2. **Tự tạo Transaction** với deposit vừa tạo, reservationId, listing, buyer (current user), seller (từ listing), amount/actualPrice; status = PENDING.

| Method | URL | Quyền |
|--------|-----|--------|
| **POST** | `http://localhost:8080/api/deposits` | Đã đăng nhập (Buyer) |

**Body (JSON):**

```json
{
  "type": "COC",
  "amount": 5000000,
  "status": "PENDING",
  "listingId": 1,
  "reservationId": 1,
  "actualPrice": 5000000
}
```

- **Bắt buộc cho luồng tạo Transaction:** `listingId`, `reservationId`. Nếu thiếu một trong hai thì chỉ tạo Deposit, không tạo Transaction.
- **actualPrice** tùy chọn; không gửi thì lấy từ giá của BikeListing.

Sau khi gọi thành công, Deposit được tạo và **một Transaction** (PENDING) được tạo và gắn với deposit đó.

---

## Flow 1: Người mua gửi yêu cầu mua + tạo giao dịch (đặt cọc) – tạo Transaction thủ công

Mục đích: Tạo transaction bằng cách gọi trực tiếp POST /transactions (khi đã có sẵn depositId, reservationId). **Status do Admin duyệt** qua PUT /transactions/{id}; khi tạo backend luôn set PENDING.

### API

| Method | URL | Quyền |
|--------|-----|--------|
| **POST** | `http://localhost:8080/api/transactions` | Đã đăng nhập (thường là Buyer) |

**Headers:**

- `Content-Type: application/json`
- `Authorization: Bearer <access_token_buyer>`

**Body khi test API (chỉ cần 2 trường):**

```json
{
  "amount": 5000000,
  "actualPrice": 5000000
}
```

- **Khi test:** Chỉ gửi `amount`, `actualPrice` (và tùy chọn `fee`). Không gửi các Id và không gửi `status`. Backend để các Id = null; status lấy từ DB (Deposit/Reservation nếu có, mặc định PENDING).
- **Khi tích hợp thật:** Có thể gửi thêm `eventId`, `listingId`, `buyerId`, `sellerId`, `depositId`, `reservationId` để gắn transaction với dữ liệu có sẵn; khi có `depositId`/`reservationId`, status được lấy từ bảng Deposit/Reservation trong DB.

**Ví dụ response (200):**

```json
{
  "result": {
    "transactionId": 1,
    "buyerId": 2,
    "sellerId": 1,
    "listingId": 1,
    "depositId": 1,
    "reservationId": 1,
    "amount": 5000000,
    "actualPrice": 5000000,
    "fee": 0,
    "status": "PENDING",
    "createdAt": "2025-03-11T...",
    "updatedAt": "2025-03-11T..."
  },
  "message": "Transaction created successfully"
}
```

**Ghi chú:** Sau bước này, client có thể gọi `POST /api/payments/submitOrder` để chuyển sang VNPay. Khi VNPay return success, backend (Giai đoạn E) sẽ tạo/cập nhật Transaction theo nghiệp vụ.

---

## Flow 2: Xem trạng thái giao dịch (Buyer / Seller / Admin)

Dùng để kiểm tra thông tin giao dịch và **status** (vd. PENDING, CONFIRMED, COMPLETED).

### API

| Method | URL | Quyền |
|--------|-----|--------|
| **GET** | `http://localhost:8080/api/transactions/{transactionId}` | Đã đăng nhập; chỉ **Buyer**, **Seller** của giao dịch đó hoặc **ADMIN** |

**Ví dụ:** `GET http://localhost:8080/api/transactions/1`

**Headers:**

- `Authorization: Bearer <access_token>` (token của Buyer, Seller hoặc Admin)

**Ví dụ response (200):**

```json
{
  "result": {
    "transactionId": 1,
    "buyerId": 2,
    "sellerId": 1,
    "listingId": 1,
    "depositId": 1,
    "reservationId": 1,
    "amount": 5000000,
    "actualPrice": 5000000,
    "fee": 0,
    "status": "PENDING",
    "createdAt": "2025-03-11T...",
    "updatedAt": "2025-03-11T..."
  },
  "message": "Transaction fetched successfully"
}
```

---

## Flow 3: Admin cập nhật giao dịch

Admin cập nhật các field có sẵn của Transaction (event, listing, buyer, seller, deposit, reservation, status, amount, actualPrice, fee). **Không có field meetingAddress, meetingTime, inspector** trên entity; có thể dùng **eventId** (Events có address, startDate) nếu cần địa điểm/thời gian.

### API

| Method | URL | Quyền |
|--------|-----|--------|
| **PUT** | `http://localhost:8080/api/transactions/{transactionId}` | **ADMIN** |

**Ví dụ:** `PUT http://localhost:8080/api/transactions/1`

**Headers:**

- `Content-Type: application/json`
- `Authorization: Bearer <access_token_admin>`

**Body (JSON) – các field có thể cập nhật:**

```json
{
  "eventId": 1,
  "status": "CONFIRMED",
  "amount": 5000000,
  "actualPrice": 5000000,
  "fee": 0
}
```

- Có thể gửi: eventId, listingId, buyerId, sellerId, depositId, reservationId, status, amount, actualPrice, fee (chỉ gửi field cần đổi).

---

## Flow 4: Admin – Danh sách và lọc theo trạng thái

### 4.1. Lấy tất cả giao dịch

| Method | URL | Quyền |
|--------|-----|--------|
| **GET** | `http://localhost:8080/api/transactions` | **ADMIN** |

**Headers:** `Authorization: Bearer <access_token_admin>`

**Response:** Mảng `result` chứa danh sách TransactionResponse.

---

### 4.2. Lấy giao dịch theo status

| Method | URL | Quyền |
|--------|-----|--------|
| **GET** | `http://localhost:8080/api/transactions/status/{status}` | **ADMIN** |

**Ví dụ:** `GET http://localhost:8080/api/transactions/status/PENDING`

**Headers:** `Authorization: Bearer <access_token_admin>`

**Response:** Mảng `result` chỉ gồm giao dịch có `status` tương ứng.

---

## Flow 5: Admin – Xóa giao dịch (tuỳ nghiệp vụ)

| Method | URL | Quyền |
|--------|-----|--------|
| **DELETE** | `http://localhost:8080/api/transactions/{transactionId}` | **ADMIN** |

**Ví dụ:** `DELETE http://localhost:8080/api/transactions/1`

**Headers:** `Authorization: Bearer <access_token_admin>`

**Response (200):** `result` là chuỗi "Transaction deleted successfully".

---

## Test các API còn lại (sau khi đã tạo & update transaction)

Sau khi đã gọi **POST /api/transactions** (tạo) và **PUT /api/transactions/{id}** (cập nhật), lần lượt test các API dưới đây.

### Link API cụ thể (transactionId = 202, localhost:8080)

Copy trực tiếp vào ô URL trong Postman:

| Mục đích | Method | URL (copy nguyên) |
|----------|--------|--------------------|
| Xem giao dịch 202 | GET | `http://localhost:8080/api/transactions/202` |
| Admin xem tất cả | GET | `http://localhost:8080/api/transactions` |
| Admin lọc COMPLETED | GET | `http://localhost:8080/api/transactions/status/COMPLETED` |
| Admin xóa 202 | DELETE | `http://localhost:8080/api/transactions/202` |

**Lưu ý:** GET theo ID dùng token Buyer/Seller/Admin đều được; GET all, GET status, DELETE cần token **Admin**. Tab **Authorization** chọn **Bearer Token** và dán token.

### 1. GET theo ID – Xem một giao dịch

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/transactions/202` (thay `202` bằng transactionId của bạn)
- **Auth:** Tab **Authorization** → Type **Bearer Token** → dán token (token **Buyer**, **Seller** của giao dịch đó, hoặc **Admin** đều được)
- **Body:** Không cần (để **none**)
- **Send** → Kỳ vọng **200 OK**, response có `result` chứa chi tiết transaction (transactionId, buyerId, sellerId, amount, status, ...)

---

### 2. GET tất cả – Admin xem danh sách giao dịch

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/transactions`
- **Auth:** Chỉ **Admin** (Bearer Token của user admin). User thường không gọi được → 403.
- **Body:** Không cần
- **Send** → Kỳ vọng **200 OK**, `result` là **mảng** các transaction (trong đó có transaction 202 nếu vừa tạo/update).

---

### 3. GET theo status – Admin lọc theo trạng thái

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/transactions/status/COMPLETED` (hoặc `PENDING`, … tùy status bạn đã set)
- **Auth:** **Admin** (Bearer Token)
- **Body:** Không cần
- **Send** → Kỳ vọng **200 OK**, `result` là mảng chỉ gồm giao dịch có `status` = `COMPLETED` (hoặc status bạn gửi trong URL).

---

### 4. DELETE – Admin xóa giao dịch (cẩn thận)

- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/transactions/202` (thay bằng id cần xóa; xóa xong giao dịch mất khỏi DB)
- **Auth:** **Admin**
- **Body:** Không cần
- **Send** → Kỳ vọng **200 OK**, `result` = `"Transaction deleted successfully"`. Sau đó gọi lại **GET /api/transactions/202** sẽ **404**.

**Lưu ý:** Chỉ test DELETE khi bạn chấp nhận mất bản ghi đó, hoặc dùng một transactionId tạo riêng để xóa.

---

## Thứ tự test gợi ý (end-to-end)

1. **Lấy token Buyer** → `POST /api/auth/login` (user buyer).
2. **Tạo Transaction** → `POST /api/transactions` (body có depositId, reservationId, listingId, buyerId, sellerId, amount, actualPrice) → lưu `transactionId`.
3. **Xem trạng thái (Buyer)** → `GET /api/transactions/{transactionId}` (token Buyer).
4. **Lấy token Admin** → `POST /api/auth/login` (user admin).
5. **Admin cập nhật giao dịch** → `PUT /api/transactions/{transactionId}` với eventId, status, amount... (token Admin).
6. **Xem lại (Admin)** → `GET /api/transactions/{transactionId}` hoặc `GET /api/transactions`.

---

## Bảng tóm tắt API theo flow

| Flow | Mục đích | Method | Endpoint | Vai trò |
|------|----------|--------|----------|---------|
| 1 | Người mua tạo giao dịch (yêu cầu + đặt cọc) | POST | `/api/transactions` | Buyer (authenticated) |
| 2 | Xem trạng thái giao dịch | GET | `/api/transactions/{id}` | Buyer / Seller / Admin |
| 3 | Admin cập nhật giao dịch | PUT | `/api/transactions/{id}` | Admin |
| 4a | Admin xem tất cả giao dịch | GET | `/api/transactions` | Admin |
| 4b | Admin lọc theo status | GET | `/api/transactions/status/{status}` | Admin |
| 5 | Admin xóa giao dịch | DELETE | `/api/transactions/{id}` | Admin |

---

## Lỗi thường gặp

| HTTP | Ý nghĩa |
|------|---------|
| 401 | Chưa gửi token hoặc token hết hạn → login lại lấy token mới. |
| 403 | Không đủ quyền (vd: không phải Admin gọi PUT, GET all, GET by status, DELETE). |
| 404 | transactionId / depositId / reservationId / listingId / userId không tồn tại. |
| 400 | Body sai (thiếu depositId/reservationId khi tạo transaction). |

Khi test, đảm bảo **depositId** và **reservationId** đã tồn tại và chưa gắn transaction khác (một deposit chỉ gắn một transaction).

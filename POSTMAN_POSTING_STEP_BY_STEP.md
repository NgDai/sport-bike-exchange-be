# Step-by-step: Test Posting API thành công

Base URL: `http://localhost:8080/api`  
Làm **đúng thứ tự** từ Bước 1 → 4. Sau mỗi bước, lưu **token** hoặc **userId** / **eventId** từ response để dùng bước sau.

---

## Bước 1: Login lấy token

**Method:** `POST`  
**URL:** `http://localhost:8080/api/auth/login`  
**Authorization:** Không cần (No Auth)  
**Body** → chọn **raw** → **JSON**:

```json
{
  "username": "admin",
  "password": "1"
}
```

**Gửi request** → Response thành công (200 OK) sẽ có dạng:
```json
{
  "code": 0,
  "result": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**Làm:** Copy toàn bộ chuỗi trong `result.token` (từ `eyJ` đến hết). Dùng làm **Bearer Token** cho các bước sau.

---

## Bước 2: Tạo User (để có sellerId)

**Method:** `POST`  
**URL:** `http://localhost:8080/api/users`  
**Authorization:** Không cần (No Auth)  
**Body** → **raw** → **JSON**:

```json
{
  "username": "seller_post_test",
  "password": "123456",
  "fullName": "Seller for Posting Test",
  "email": "sellerpost@test.com",
  "phone": "0901234567",
  "walletBalance": "0",
  "status": "ACTIVE"
}
```

**Gửi request** → Nếu 200 OK, response có `result.userId` (ví dụ `903`).  
**Làm:** Ghi lại **userId** (ví dụ `903`). Nếu báo "Username already exists" → dùng user khác: gọi **GET** `http://localhost:8080/api/users` (Bước 2b, cần Bearer Token từ Bước 1), xem list và chọn một **userId** có sẵn.

---

## Bước 2b (nếu đã có user sẵn): Lấy danh sách User

**Method:** `GET`  
**URL:** `http://localhost:8080/api/users`  
**Authorization:** Tab **Authorization** → **Type:** Bearer Token → **Token:** dán token từ Bước 1.  
**Body:** Không gửi.

**Gửi request** → Trong `result` xem các user, chọn một **userId** (ví dụ `902`) để dùng làm **sellerId** ở Bước 4.

---

## Bước 3: Tạo Event (để có eventId)

**Method:** `POST`  
**URL:** `http://localhost:8080/api/events`  
**Authorization:** **Bearer Token** → dán token từ Bước 1.  
**Body** → **raw** → **JSON**:

```json
{
  "name": "Bike Fair Test 2026",
  "location": "Ho Chi Minh City",
  "startDate": "2026-03-01",
  "endDate": "2026-03-03",
  "sellerDepositRate": 0.1,
  "buyerDepositRate": 0.1,
  "platformFeeRate": 0.05,
  "status": "ACTIVE"
}
```

**Gửi request** → Nếu 200 OK, response có `result.eventId` (ví dụ `353`).  
**Làm:** Ghi lại **eventId** (ví dụ `353`).

**Nếu đã có event:** **GET** `http://localhost:8080/api/events` (cùng Bearer Token) → chọn một **eventId** từ `result`.

---

## Bước 4: Tạo Posting (test thành công)

**Method:** `POST`  
**URL:** `http://localhost:8080/api/postings`  
**Authorization:** **Bearer Token** → dán token từ Bước 1.  
**Body** → **raw** → **JSON**:

Thay `SELLER_ID` và `EVENT_ID` bằng **userId** (Bước 2) và **eventId** (Bước 3) bạn đã ghi lại.

```json
{
  "sellerId": 903,
  "eventId": 353,
  "title": "Giant TCR Advanced Pro 2023",
  "brand": "Giant",
  "model": "TCR Advanced Pro",
  "category": "Road",
  "frameSize": "M",
  "wheelSize": "700c",
  "manufactureYear": 2023,
  "brakeType": "Disc",
  "transmission": "Shimano 105",
  "weight": 8.2,
  "imageUrl": "https://example.com/bike1.jpg",
  "description": "Like new.",
  "price": 1500.0,
  "status": "AVAILABLE"
}
```

**Gửi request** → Nếu 200 OK, posting đã tạo thành công; `result` là thông tin posting (không có listingId, seller, event trong JSON theo cấu hình hiện tại).

---

## Tóm tắt thứ tự

| Bước | Method | URL | Auth | Mục đích |
|------|--------|-----|------|----------|
| 1 | POST | /api/auth/login | Không | Lấy **token** |
| 2 | POST | /api/users | Không | Lấy **userId** (sellerId) |
| 3 | POST | /api/events | Bearer Token | Lấy **eventId** |
| 4 | POST | /api/postings | Bearer Token | Tạo posting với sellerId + eventId |

---

## Lưu ý

- **Bước 2:** Nếu username đã tồn tại, đổi `"username"` (ví dụ `seller_post_test2`) hoặc dùng **GET /api/users** (có token) để lấy **userId** có sẵn.
- **Bước 3, 4:** Bắt buộc có **Authorization: Bearer &lt;token&gt;** (token từ Bước 1).
- **Bước 4:** `sellerId` và `eventId` phải tồn tại trong DB; dùng đúng số từ Bước 2 và 3.

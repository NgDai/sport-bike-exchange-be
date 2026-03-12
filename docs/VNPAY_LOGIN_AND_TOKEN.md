# Đăng nhập lấy token để gọi API VNPay (submitOrder)

Vì **SecurityConfig** yêu cầu đăng nhập cho mọi API (trừ login/register), để gọi **POST /api/payments/submitOrder** bạn cần **đăng nhập trước**, lấy **JWT**, rồi gửi kèm header **Authorization**.

---

## Bước 1: Đăng nhập lấy token

Có **2 cách** đăng nhập (đều **không cần** token):

### Cách 1: Đăng nhập bằng **username** + **password**

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/login`
- **Headers:** `Content-Type: application/json`
- **Body (raw – JSON):**

```json
{
  "username": "admin",
  "password": "1"
}
```

*(Theo code, user mặc định có username `admin`, password `1` — đổi nếu bạn đã tạo user khác.)*

### Cách 2: Đăng nhập bằng **email** + **password**

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/loginEmail`
- **Headers:** `Content-Type: application/json`
- **Body (raw – JSON):**

```json
{
  "email": "testuser01@gmail.com",
  "password": "123456"
}
```

*(Dùng email/password của user đã đăng ký qua **POST /api/users**.)*

---

## Bước 2: Response đăng nhập thành công

Ví dụ response:

```json
{
  "code": 1000,
  "message": null,
  "result": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Lấy giá trị `result.token`** — đó là JWT dùng cho các request cần đăng nhập.

---

## Bước 3: Gọi API submitOrder kèm token

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/payments/submitOrder`
- **Headers:**
  - `Content-Type`: `application/json`
  - **`Authorization`**: `Bearer <token>`  
    *(thay `<token>` bằng chuỗi trong `result.token` ở bước 2)*
- **Body (raw – JSON):**

```json
{
  "amount": 100000,
  "orderInfo": "Test don hang 01"
}
```

Ví dụ header Authorization:

```text
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIs...
```

---

## Cấu hình nhanh trong Postman

### Request 1 – Login

1. Tạo request **POST** `http://localhost:8080/api/auth/login`.
2. Body → **raw** → **JSON** → dán `{"username":"admin","password":"1"}`.
3. **Send** → mở tab **Body** của response → copy toàn bộ giá trị của **`result.token`** (không copy dấu ngoặc kép).

### Request 2 – Submit order (VNPay)

1. Tạo request **POST** `http://localhost:8080/api/payments/submitOrder`.
2. Tab **Authorization**:
   - Type: **Bearer Token**
   - Token: dán chuỗi token vừa copy (Postman tự thêm chữ `Bearer `).
3. Hoặc tab **Headers** thêm:
   - Key: `Authorization`
   - Value: `Bearer <dán_token_vào_đây>`
4. Tab **Body** → **raw** → **JSON**:

```json
{
  "amount": 100000,
  "orderInfo": "Test don hang 01"
}
```

5. **Send** → response sẽ có **200** và **`redirectUrl`** (URL thanh toán VNPay) thay vì 401.

---

## Dùng token tự động (Postman)

Để không phải copy token tay mỗi lần:

1. Request **Login** → tab **Tests** thêm:

```javascript
var json = pm.response.json();
if (json.result && json.result.token) {
    pm.environment.set("auth_token", json.result.token);
}
```

2. Trong **Environment** (hoặc Globals) tạo biến `auth_token` (để trống lúc đầu).
3. Request **Submit order** → **Authorization** chọn **Bearer Token**, ô Token điền: `{{auth_token}}`.

Mỗi lần chạy request Login, `auth_token` được cập nhật; request Submit order sẽ dùng token mới.  
*(Lưu ý: token có thời hạn, hết hạn thì gọi lại Login lấy token mới.)*

---

## Tóm tắt

| Bước | API | Mục đích |
|------|-----|----------|
| 1 | **POST** `/api/auth/login` hoặc `/api/auth/loginEmail` | Đăng nhập, lấy `result.token` |
| 2 | **POST** `/api/payments/submitOrder` với header `Authorization: Bearer <token>` | Tạo đơn VNPay, nhận `redirectUrl` |

Nếu không gửi token (hoặc token sai/hết hạn), **POST /api/payments/submitOrder** sẽ trả **401 Unauthorized**.

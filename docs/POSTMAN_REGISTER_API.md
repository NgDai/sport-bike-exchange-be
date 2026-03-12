# Test API Register (Postman)

## API Đăng ký user (role USER)

**Endpoint:** `POST /users`  
**Base URL:** `http://localhost:8080/api` (có **context-path** `/api` trong `application.properties`)  
**Authentication:** Không cần — endpoint public (dùng để đăng ký).

---

## Cấu hình trong Postman

1. **Method:** `POST`
2. **URL:** `http://localhost:8080/api/users`  ← **Bắt buộc có `/api`**
3. **Headers:**
   - `Content-Type`: `application/json`
4. **Body:** chọn **raw** → **JSON**, dán nội dung mẫu bên dưới.

---

## Request body mẫu (JSON)

```json
{
  "username": "testuser01",
  "password": "123456",
  "fullName": "Nguyen Van A",
  "email": "testuser01@gmail.com",
  "phone": "0901234567",
  "address": "123 Nguyen Hue, Q1, HCM"
}
```

### Trường bắt buộc (validation)

| Trường     | Bắt buộc | Validation |
|------------|----------|------------|
| username   | Có       | 3–50 ký tự, chỉ chữ, số, `_`, `-` |
| password   | Có       | 6–100 ký tự |
| fullName   | Có       | Tối đa 100 ký tự |
| email      | Có       | Đúng format email |
| phone      | Có       | 10–20 ký tự, chỉ số/dấu cách/dấu ngoặc (vd: 0901234567, +84901234567) |

### Trường không bắt buộc

- `status`
- `address`

---

## Response thành công (ví dụ)

- **Status:** `200 OK`
- **Body:** dạng `ApiResponse` với `result` là thông tin user vừa tạo (có thể có `id`, `username`, `fullName`, `email`, `phone`, …).

```json
{
  "code": 0,
  "message": "...",
  "result": {
    "id": 1,
    "username": "testuser01",
    "fullName": "Nguyen Van A",
    "email": "testuser01@gmail.com",
    "phone": "0901234567",
    "status": "Active",
    "address": "123 Nguyen Hue, Q1, HCM",
    "role": ["USER"]
  }
}
```

*(Cấu trúc thực tế phụ thuộc vào `ApiResponse` và entity `Users` trong code.)*

---

## Một số lỗi thường gặp

| Tình huống | Code / Message có thể |
|------------|------------------------|
| Username đã tồn tại | `USERNAME_ALREADY_EXISTS` |
| Thiếu / sai format field (username, email, phone, …) | `INVALID_KEY` hoặc message validation (vd: `USERNAME_REQUIRED`, `EMAIL_INVALID`, `PHONE_REQUIRED`, `PHONE_INVALID_FORMAT`) |
| Password quá ngắn (< 6) | `PASSWORD_INVALID` |

---

## Checklist test nhanh

1. Gọi `POST http://localhost:8080/api/users` với body mẫu trên → trả về 200 và có `result`.
2. Gọi lại cùng `username` → trả về lỗi `USERNAME_ALREADY_EXISTS`.
3. Bỏ trống `email` hoặc `phone` → trả về lỗi validation (email/phone required).
4. Sai format email (vd: `abc`) hoặc phone (vd: `abc`) → trả về lỗi format tương ứng.

---

## Test validation email bằng Postman

API Register dùng **`POST /api/users`** với validation email:
- **Bắt buộc:** `@NotBlank` → thiếu/trống sẽ báo **EMAIL_REQUIRED**.
- **Format:** `@Email` → sai format sẽ báo **EMAIL_INVALID**.

**Quan trọng:** Nếu bạn gửi **email hợp lệ** (vd: `user@gmail.com`) thì API trả **200 OK** — lúc đó bạn *không thấy* lỗi validation. Để **chứng tỏ** validation email hoạt động, bạn phải **cố tình gửi email sai** (trống hoặc sai format) thì mới thấy response **400** với message lỗi email.

---

### Cách chứng tỏ validation email (làm từng bước)

**Bước 1.** Trong Postman: Method **POST**, URL **`http://localhost:8080/api/users`**, Body **raw** → **JSON**.

**Bước 2.** Dán **đúng** body bên dưới (chú ý `"email": "abc"` — sai format, không có `@`):

```json
{
  "username": "newuser999",
  "password": "123456",
  "fullName": "Nguyen Van A",
  "email": "abc",
  "phone": "0901234567",
  "address": "123 Nguyen Hue"
}
```

**Bước 3.** Bấm **Send**.

**Bước 4.** Kết quả cần thấy (chứng tỏ validation email):
- **Status:** `400 Bad Request`
- **Body:** JSON có `"code": 1016` và `"message": "Email format is invalid"`

Nếu thấy đúng như vậy → validation email (format) đã hoạt động.

**Thêm:** Để chứng tỏ validation "email bắt buộc", thay `"email": "abc"` bằng `"email": ""` hoặc xóa hẳn dòng `"email": "..."` trong body, gửi lại → kỳ vọng **400** với `"code": 1015`, `"message": "Email is required"`.

---

### Cấu hình chung

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/users`
- **Headers:** `Content-Type: application/json`
- **Body:** raw → JSON (chỉ thay đổi trường `email` theo từng case bên dưới).

---

### Case 1: Email hợp lệ (pass validation)

**Body mẫu:**

```json
{
  "username": "testemail01",
  "password": "123456",
  "fullName": "Nguyen Van A",
  "email": "testemail01@gmail.com",
  "phone": "0901234567",
  "address": "123 Nguyen Hue"
}
```

**Kỳ vọng:** Status **200 OK**, response JSON có `result` (user vừa tạo, có `email` đúng).

---

### Case 2: Thiếu email (trống hoặc không gửi)

**Body mẫu (email rỗng):**

```json
{
  "username": "testemail02",
  "password": "123456",
  "fullName": "Nguyen Van A",
  "email": "",
  "phone": "0901234567",
  "address": "123 Nguyen Hue"
}
```

**Hoặc bỏ hẳn key `email`:**

```json
{
  "username": "testemail02",
  "password": "123456",
  "fullName": "Nguyen Van A",
  "phone": "0901234567",
  "address": "123 Nguyen Hue"
}
```

**Kỳ vọng:**
- Status: **400 Bad Request**
- Body JSON ví dụ: `{ "code": 1015, "message": "Email is required", "result": null }`

---

### Case 3: Sai format email

**Body mẫu (các giá trị sai format để thử):**

| Thử với `email`   | Lý do sai format        |
|-------------------|-------------------------|
| `"abc"`           | Không có `@`            |
| `"abc@"`          | Thiếu phần sau `@`     |
| `"@gmail.com"`    | Thiếu phần trước `@`   |
| `"abc@gmail"`     | Thiếu phần sau dấu chấm|
| `"abc @gmail.com"`| Có khoảng trắng         |

**Ví dụ body (email = "abc"):**

```json
{
  "username": "testemail03",
  "password": "123456",
  "fullName": "Nguyen Van A",
  "email": "abc",
  "phone": "0901234567",
  "address": "123 Nguyen Hue"
}
```

**Kỳ vọng:**
- Status: **400 Bad Request**
- Body JSON ví dụ: `{ "code": 1016, "message": "Email format is invalid", "result": null }`

---

### Case 4: Email hợp lệ (một số format thường dùng)

Các giá trị sau **nên pass** validation `@Email`:

- `user@gmail.com`
- `user.name@domain.com`
- `user+tag@company.vn`

Dùng một trong các giá trị trên cho trường `email` trong body, giữ nguyên các trường khác hợp lệ → kỳ vọng **200 OK**.

---

### Tóm tắt checklist validation email

| # | Hành động              | Kỳ vọng Status | Kỳ vọng message/code        |
|---|------------------------|-----------------|-----------------------------|
| 1 | Email đúng format      | 200 OK          | Có `result`, tạo user thành công |
| 2 | Email trống hoặc thiếu | 400 Bad Request | code 1015, "Email is required"  |
| 3 | Email sai format (vd: `abc`) | 400 Bad Request | code 1016, "Email format is invalid" |

---

**Lưu ý:**  
- **URL phải có `/api`:** `http://localhost:8080/api/users` (vì `server.servlet.context-path=/api`). Thiếu `/api` sẽ bị **404 Not Found** và trả về trang HTML lỗi.  
- Đảm bảo backend đang chạy (vd: `mvn spring-boot:run` hoặc chạy từ IDE) và port `8080` không bị ứng dụng khác chiếm.

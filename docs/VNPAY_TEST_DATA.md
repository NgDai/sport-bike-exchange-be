# Dữ liệu test VNPay – Sport Bike Exchange

Tài liệu tổng hợp **datatest** để test luồng VNPay (Postman / cURL / frontend).

**Base URL:** `http://localhost:8080/api`  
**Context path:** `/api`

---

## 1. Đăng nhập lấy token (bắt buộc trước khi gọi submitOrder)

Endpoint **POST /payments/submitOrder** yêu cầu JWT. Cần đăng nhập trước, lấy `result.token`, gửi header `Authorization: Bearer <token>`.

### 1.1. Login bằng username/password

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/auth/login` |
| **Headers** | `Content-Type: application/json` |
| **Body (JSON)** | Xem bảng dưới |

**Datatest đăng nhập:**

| Test case | username | password | Ghi chú |
|-----------|----------|----------|--------|
| User mặc định | `admin` | `1` | Dùng cho test nhanh |
| User khác | *(username đã tạo)* | *(password)* | Nếu đã đăng ký user mới |

**Body mẫu:**

```json
{
  "username": "admin",
  "password": "1"
}
```

**Response mẫu (200):**

```json
{
  "code": 1000,
  "message": null,
  "result": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

→ Copy `result.token` để dùng cho **Bước 2**.

### 1.2. Login bằng email/password (tùy chọn)

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/auth/loginEmail` |
| **Headers** | `Content-Type: application/json` |

**Body mẫu:**

```json
{
  "email": "testuser01@gmail.com",
  "password": "123456"
}
```

*(Thay bằng email/password của user đã đăng ký trong hệ thống.)*

---

## 2. Tạo đơn VNPay (submitOrder) – Request body test

| Thuộc tính | Giá trị |
|------------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/payments/submitOrder` |
| **Headers** | `Content-Type: application/json`<br>`Authorization: Bearer <token từ bước 1>` |

### 2.1. Datatest body (VNPayRequest)

- **amount**: Số tiền **VND** (integer). Backend gửi sang VNPay dạng xu (×100).
- **orderInfo**: Nội dung đơn (string), hiển thị trên trang VNPay.

**Các bộ data test gợi ý:**

| # | amount (VND) | orderInfo | Mục đích test |
|---|--------------|-----------|----------------|
| 1 | `100000` | `Thanh toan don hang test so 01` | Test cơ bản 100k |
| 2 | `50000` | `Postman test order` | Test nhanh 50k |
| 3 | `250000` | `Mua phu tung xe dap so 123` | Test đơn 250k |
| 4 | `1000000` | `Thanh toan xe dap Sport Bike Exchange` | Test đơn 1 triệu |
| 5 | `1` | `Test so tien nho` | Test số tiền tối thiểu (tùy VNPay) |

**Body mẫu (dùng trong Postman / cURL):**

```json
{
  "amount": 100000,
  "orderInfo": "Thanh toan don hang test so 01"
}
```

**Ví dụ khác:**

```json
{
  "amount": 50000,
  "orderInfo": "Postman test order"
}
```

```json
{
  "amount": 1000000,
  "orderInfo": "Thanh toan xe dap Sport Bike Exchange"
}
```

### 2.2. Response mẫu (200)

```json
{
  "redirectUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&vnp_Command=pay&...",
  "paymentStatus": null,
  "orderInfo": null,
  "paymentTime": null,
  "transactionId": null,
  "totalPrice": null,
  "message": null
}
```

→ Dùng `redirectUrl` mở trên trình duyệt để vào trang VNPay Sandbox và thanh toán.

---

## 3. Thẻ test VNPay Sandbox (khi thanh toán trên redirectUrl)

**Quan trọng:** Chọn ngân hàng **NCB** (Ngân hàng TMCP Quốc Dân). Các ngân hàng khác trên sandbox có thể báo "Số thẻ chưa hợp lệ".

### 3.1. Bảng thẻ test

| Kịch bản | Số thẻ | OTP | Ghi chú |
|----------|--------|-----|--------|
| Thanh toán thành công | `9704198526191432198` | `123456` | Dùng cho flow happy path |
| Không đủ số dư | `9704195798459170488` | - | Test giao dịch lỗi |
| Thẻ chưa kích hoạt | `9704192181368742` | - | Test lỗi thẻ |
| Thẻ bị khóa | `9704193370791314` | - | Test lỗi thẻ |
| Thẻ hết hạn | `9704194841945513` | - | Test lỗi thẻ |

### 3.2. Thông tin bổ sung (khi VNPay yêu cầu)

- **Tên chủ thẻ:** `NGUYEN VAN A` (hoặc tên bất kỳ trong test)
- **Ngày phát hành:** `07/15` hoặc `03/07`, `10/26`

---

## 4. Return URL – Response sau khi VNPay redirect

Sau khi thanh toán trên VNPay, trình duyệt được redirect về:

- **URL:** `GET http://localhost:8080/api/payments/vnpay-payment?vnp_TransactionStatus=00&vnp_Amount=...&vnp_SecureHash=...`  
  *(Các tham số do VNPay thêm, không cần tạo tay.)*

**Không cần** header `Authorization` cho GET này (endpoint `permitAll`).

### 4.1. Ý nghĩa paymentStatus trong response

| paymentStatus | Ý nghĩa |
|---------------|---------|
| `1` | Thanh toán thành công |
| `0` | Giao dịch thất bại / hủy / lỗi |
| `-1` | Chữ ký không hợp lệ |

### 4.2. Response mẫu (thanh toán thành công)

```json
{
  "redirectUrl": null,
  "paymentStatus": 1,
  "orderInfo": "Thanh toan don hang test so 01",
  "paymentTime": "20250303120000",
  "transactionId": "14234567",
  "totalPrice": "10000000",
  "message": "Thanh toán thành công"
}
```

**Lưu ý:** `totalPrice` từ VNPay là **đơn vị xu** (ví dụ `10000000` = 100.000 VND).

---

## 5. Checklist test nhanh (Postman)

1. **POST** `http://localhost:8080/api/auth/login`  
   Body: `{"username":"admin","password":"1"}`  
   → Copy `result.token`.

2. **POST** `http://localhost:8080/api/payments/submitOrder`  
   Header: `Authorization: Bearer <token>`  
   Body: `{"amount": 100000, "orderInfo": "Thanh toan don hang test so 01"}`  
   → Copy `redirectUrl`.

3. Mở `redirectUrl` trong trình duyệt → chọn **NCB** → nhập thẻ `9704198526191432198`, OTP `123456` → hoàn tất.

4. Trình duyệt redirect về `/api/payments/vnpay-payment?...` → kiểm tra response JSON: `paymentStatus: 1`, `message: "Thanh toán thành công"`.

---

## 6. cURL / PowerShell (copy-paste)

### 6.1. Login (PowerShell)

```powershell
$loginBody = '{"username":"admin","password":"1"}'
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$token = $loginResponse.result.token
Write-Host "Token: $token"
```

### 6.2. Submit order (PowerShell) – thay `<TOKEN>` bằng token từ bước trên

```powershell
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer <TOKEN>"
}
$body = '{"amount": 100000, "orderInfo": "Thanh toan don hang test so 01"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/payments/submitOrder" -Method Post -Headers $headers -Body $body
```

### 6.3. cURL (Linux/macOS) – Login

```bash
curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"1"}'
```

### 6.4. cURL – Submit order (thay YOUR_TOKEN)

```bash
curl -X POST "http://localhost:8080/api/payments/submitOrder" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"amount": 100000, "orderInfo": "Thanh toan don hang test so 01"}'
```

---

## 7. Tóm tắt datatest chính

| Mục | Dữ liệu |
|-----|---------|
| **Login** | `username: "admin"`, `password: "1"` |
| **SubmitOrder body** | `amount: 100000`, `orderInfo: "Thanh toan don hang test so 01"` |
| **Thẻ test (thành công)** | Số thẻ: `9704198526191432198`, OTP: `123456`, ngân hàng: **NCB** |
| **Return URL** | GET `/api/payments/vnpay-payment` (params do VNPay gửi, không cần auth) |

Nếu cần thêm kịch bản test (số tiền khác, orderInfo khác, thẻ lỗi), dùng các bộ data trong **mục 2.1** và **mục 3.1**.

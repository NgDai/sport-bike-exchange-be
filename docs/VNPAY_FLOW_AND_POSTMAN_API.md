# Luồng VNPay & API test Postman

Tài liệu mô tả **luồng thanh toán VNPay** phù hợp với project, **cấu trúc code** và **API cụ thể** để test bằng Postman.

---

## 1. Luồng tổng quan (VNPay flow)

```
┌──────────┐    ① POST /payments/submitOrder      ┌─────────────┐
│  Client  │ ──────────────────────────────────► │   Backend   │
│(Postman/ │     Body: amount, orderInfo         │  (Spring)   │
│  Web)    │     Header: Authorization: Bearer   │             │
└────┬─────┘                                    └──────┬──────┘
     │                                                  │
     │  ② Response: redirectUrl (URL VNPay)            │
     │◄────────────────────────────────────────────────┘
     │
     │  ③ User mở redirectUrl trong trình duyệt
     ▼
┌──────────────┐
│  VNPay       │  User chọn ATM/QR/Ví, nhập thẻ test, OTP
│  Sandbox     │
└──────┬───────┘
       │  ④ VNPay redirect GET về Return URL (kèm query params + vnp_SecureHash)
       ▼
┌─────────────┐     GET /api/payments/vnpay-payment?vnp_TransactionStatus=00&vnp_Amount=...
│   Backend   │ ◄── (không cần JWT, endpoint public)
│             │
│  - Verify   │  ⑤ Response JSON: paymentStatus, orderInfo, transactionId, ...
│    chữ ký   │
│  - Trả JSON │
└─────────────┘
```

### Các bước chi tiết

| Bước | Ai thực hiện | Mô tả |
|------|----------------|-------|
| 1 | Client | Gọi **POST /api/payments/submitOrder** với `amount` (VND), `orderInfo`. Gửi kèm **JWT** (trừ khi cấu hình public). |
| 2 | Backend | Tạo tham số VNPay (vnp_Amount = amount×100, vnp_ReturnUrl, vnp_TxnRef, ...), tạo **chữ ký HMAC SHA512** (không URL-encode dữ liệu hash), build URL redirect. Trả JSON chứa **redirectUrl**. |
| 3 | User | Mở **redirectUrl** trên trình duyệt → vào trang VNPay Sandbox → chọn phương thức, nhập thẻ test, OTP. |
| 4 | VNPay | Sau khi thanh toán, VNPay **redirect** trình duyệt về **Return URL** của merchant với query params (vnp_TransactionStatus, vnp_Amount, vnp_TxnRef, vnp_SecureHash, ...). |
| 5 | Backend | Nhận GET tại **/api/payments/vnpay-payment**. Thu thập params (đã decode), **verify chữ ký** (hash với giá trị gốc, không encode). Trả JSON: `paymentStatus` (1=thành công, 0=thất bại, -1=chữ ký lỗi), orderInfo, transactionId, totalPrice, message. |

---

## 2. Cấu trúc code (VNPay)

```
sport-bike-exchange-be/
├── src/main/java/com/bicycle/marketplace/
│   ├── config/
│   │   ├── VNPayConfig.java          # URL VNPay, TmnCode, secretKey, HMAC/hash
│   │   └── SecurityConfig.java      # Cho phép GET /payments/vnpay-payment không auth
│   ├── controller/
│   │   └── PaymentController.java   # submitOrder (POST), handleVnPayReturn (GET)
│   ├── services/
│   │   └── VNPayService.java         # createOrder(), orderReturn()
│   └── dto/
│       ├── request/
│       │   └── VNPayRequest.java    # amount, orderInfo
│       └── response/
│           └── VNPayResponse.java   # redirectUrl, paymentStatus, orderInfo, ...
```

### Vai trò từng class

- **VNPayConfig**: Hằng số (vnp_PayUrl, vnp_ReturnUrl, vnp_TmnCode, secretKey), hàm **hmacSHA512(key, data)** và **hashAllFields(Map)**. Quy tắc: chuỗi đưa vào hash là `key=value` nối theo thứ tự alphabet, **không URL-encode**.
- **VNPayService**:
  - **createOrder(total, orderInfo, baseUrl)**: Tạo map tham số VNPay, sort key, build chuỗi hash (giá trị gốc) → HMAC → build query (có URL-encode) + vnp_SecureHash → trả về URL thanh toán.
  - **orderReturn(request)**: Lấy toàn bộ query params (đã decode), bỏ vnp_SecureHash/vnp_SecureHashType, gọi **hashAllFields** để verify; so sánh với vnp_SecureHash. Trả 1/0/-1.
- **PaymentController**: Gọi service, build **VNPayResponse** (redirectUrl hoặc paymentStatus + orderInfo + message...).
- **SecurityConfig**: `GET /payments/vnpay-payment` được **permitAll()** vì VNPay redirect không gửi JWT.

---

## 3. Luồng xử lý chi tiết & luồng code (class → class, method → method)

### 3.1. Luồng tạo đơn (POST /api/payments/submitOrder)

| Bước | Class | Method / Hành động | Mô tả |
|------|--------|---------------------|--------|
| 1 | DispatcherServlet | (Spring) | Nhận POST `/api/payments/submitOrder`, map vào `PaymentController.submitOrder`. |
| 2 | **PaymentController** | `submitOrder(VNPayRequest request, HttpServletRequest httpRequest)` | Bind body vào `VNPayRequest`. Lấy `baseUrl` = scheme + serverName + port. |
| 3 | **PaymentController** | `VNPayConfig.getIpAddress(httpRequest)` | Gọi **VNPayConfig** lấy IP khách. |
| 4 | **VNPayConfig** | `getIpAddress(HttpServletRequest)` | Trả về IP (X-Forwarded-For hoặc RemoteAddr). |
| 5 | **PaymentController** | `vnPayService.createOrder(amount, orderInfo, baseUrl, clientIp)` | Gọi **VNPayService** tạo URL thanh toán. |
| 6 | **VNPayService** | `createOrder(...)` | Tạo map VNPay (vnp_Amount=total×100, vnp_ReturnUrl, vnp_TxnRef, ...). Dùng `VNPayConfig.getRandomNumber(8)`, `vnp_TmnCode`, `vnp_ReturnUrl`. |
| 7 | **VNPayService** | (trong createOrder) | Sort params, build chuỗi hash (key=value **đã URL-encode**), build query. Gọi **VNPayConfig.hmacSHA512(secretKey, hashData)** → `vnp_SecureHash`. |
| 8 | **VNPayConfig** | `hmacSHA512(key, data)` | Trả chuỗi hex HMAC-SHA512. |
| 9 | **VNPayService** | (trong createOrder) | Nối `vnp_SecureHash` vào query, return `vnp_PayUrl + "?" + queryUrl`. |
| 10 | **PaymentController** | (sau createOrder) | Tạo **VNPayResponse**, set `redirectUrl`, return JSON. |

**Sơ đồ luồng code – Tạo đơn:**

```
Client POST /api/payments/submitOrder (body: amount, orderInfo)
    │
    ▼
PaymentController.submitOrder(...)
    ├──► VNPayConfig.getIpAddress(httpRequest)  → clientIp
    └──► VNPayService.createOrder(amount, orderInfo, baseUrl, clientIp)
              ├──► VNPayConfig.getRandomNumber(8), vnp_TmnCode, vnp_ReturnUrl
              ├──► Build vnp_Params, sort, URL-encode → hashData, query
              ├──► VNPayConfig.hmacSHA512(secretKey, hashData) → vnp_SecureHash
              └──► return vnp_PayUrl + "?" + queryUrl
    │
    └──► VNPayResponse.setRedirectUrl(vnpayUrl); return response
```

### 3.2. Luồng Return URL (GET /api/payments/vnpay-payment)

| Bước | Class | Method / Hành động | Mô tả |
|------|--------|---------------------|--------|
| 1 | SecurityConfig | (filter) | GET `/payments/vnpay-payment` → **permitAll()**, không JWT. |
| 2 | **PaymentController** | `handleVnPayReturn(HttpServletRequest request)` | Gọi `vnPayService.orderReturn(request)`. |
| 3 | **VNPayService** | `orderReturn(request)` | Thu thập query params vào Map, bỏ `vnp_SecureHash`, `vnp_SecureHashType`. |
| 4 | **VNPayService** | (trong orderReturn) | Gọi **VNPayConfig.hashAllFieldsEncoded(fields)** → signValueEncoded. |
| 5 | **VNPayService** | (trong orderReturn) | Gọi **VNPayConfig.hashAllFields(fields)** → signValueRaw. |
| 6 | **VNPayService** | (trong orderReturn) | Gọi **VNPayConfig.hashQueryString(getQueryString())** → signValueQuery. |
| 7 | **VNPayService** | (trong orderReturn) | So sánh `vnp_SecureHash` với 3 giá trị; nếu khớp → valid. `vnp_TransactionStatus == "00"` → return 1, else valid → 0, else → -1. |
| 8 | **PaymentController** | (sau orderReturn) | Tạo **VNPayResponse**, set paymentStatus, orderInfo, paymentTime, transactionId, totalPrice, message. Return JSON. |

**Sơ đồ luồng code – Return URL:**

```
Browser GET /api/payments/vnpay-payment?vnp_Amount=...&vnp_SecureHash=...
    │
    ▼
SecurityConfig → permitAll()
    ▼
PaymentController.handleVnPayReturn(request)
    └──► VNPayService.orderReturn(request)
              ├──► request.getParameterNames() / getParameter() → Map fields (bỏ vnp_SecureHash, vnp_SecureHashType)
              ├──► VNPayConfig.hashAllFieldsEncoded(fields)  → signValueEncoded
              ├──► VNPayConfig.hashAllFields(fields)         → signValueRaw
              ├──► VNPayConfig.hashQueryString(getQueryString()) → signValueQuery
              ├──► So sánh vnp_SecureHash với 3 giá trị → valid ?
              └──► return 1 (thành công) / 0 (thất bại) / -1 (chữ ký lỗi)
    │
    └──► VNPayResponse: setPaymentStatus, setOrderInfo, setMessage, ... ; return response
```

### 3.3. Phụ thuộc giữa các class

| Class | Gọi đến | Method |
|-------|---------|--------|
| PaymentController | VNPayService | `createOrder(...)`, `orderReturn(request)` |
| PaymentController | VNPayConfig | `getIpAddress(httpRequest)` |
| PaymentController | VNPayRequest, VNPayResponse | getAmount/getOrderInfo; setRedirectUrl, setPaymentStatus, ... |
| VNPayService | VNPayConfig | `getRandomNumber(8)`, `hmacSHA512`, `hashAllFields`, `hashAllFieldsEncoded`, `hashQueryString`, đọc vnp_TmnCode, vnp_ReturnUrl, vnp_PayUrl, secretKey |
| VNPayConfig | — | Chỉ dùng Mac, SecretKeySpec, URLEncoder (không gọi class VNPay khác) |

### 3.4. Luồng đi của code (class → function từng bước)

#### Luồng 1: Tạo đơn (POST submitOrder)

Thứ tự gọi: **Class → Function** (đi từ entry point đến từng function được gọi).

```
1. PaymentController.submitOrder(request, httpRequest)
       │
       ├─► 2. VNPayConfig.getIpAddress(httpRequest)     → String clientIp
       │
       ├─► 3. request.getAmount() / request.getOrderInfo()   (VNPayRequest)
       │
       ├─► 4. VNPayService.createOrder(amount, orderInfo, baseUrl, clientIp)
       │         │
       │         ├─► 5. VNPayConfig.getRandomNumber(8)        → vnp_TxnRef
       │         ├─► 6. (đọc) VNPayConfig.vnp_TmnCode, vnp_ReturnUrl
       │         ├─► 7. Build Map vnp_Params, sort key, URL-encode → hashData, query
       │         ├─► 8. VNPayConfig.hmacSHA512(secretKey, hashData)  → vnp_SecureHash
       │         ├─► 9. (đọc) VNPayConfig.vnp_PayUrl
       │         └─► 10. return String (paymentUrl)
       │
       ├─► 11. new VNPayResponse(); response.setRedirectUrl(vnpayUrl)
       └─► 12. return response  (JSON)
```

#### Luồng 2: Nhận kết quả (GET vnpay-payment)

```
1. SecurityConfig  (filter: permitAll cho GET /payments/vnpay-payment)
       │
       ▼
2. PaymentController.handleVnPayReturn(request)
       │
       └─► 3. VNPayService.orderReturn(request)
                 │
                 ├─► 4. request.getParameterNames() / getParameter()  → Map fields
                 ├─► 5. request.getParameter("vnp_SecureHash")
                 ├─► 6. VNPayConfig.hashAllFieldsEncoded(fields)     → signValueEncoded
                 ├─► 7. VNPayConfig.hashAllFields(fields)             → signValueRaw
                 ├─► 8. request.getQueryString()
                 ├─► 9. VNPayConfig.hashQueryString(queryString)       → signValueQuery
                 ├─► 10. So sánh vnp_SecureHash với 3 giá trị → valid
                 ├─► 11. request.getParameter("vnp_TransactionStatus")
                 └─► 12. return int (1 / 0 / -1)
       │
       ├─► 13. request.getParameter("vnp_OrderInfo"), "vnp_PayDate", "vnp_TransactionNo", "vnp_Amount"
       ├─► 14. new VNPayResponse(); setPaymentStatus, setOrderInfo, setPaymentTime, setTransactionId, setTotalPrice, setMessage
       └─► 15. return response  (JSON)
```

### 3.5. Chức năng các function chính

| Class | Function | Chức năng |
|-------|----------|-----------|
| **PaymentController** | `submitOrder(VNPayRequest, HttpServletRequest)` | Entry point tạo đơn: lấy baseUrl, IP, gọi service tạo URL VNPay, trả VNPayResponse chứa `redirectUrl`. |
| **PaymentController** | `handleVnPayReturn(HttpServletRequest)` | Entry point khi VNPay redirect về: gọi service verify và lấy trạng thái, gắn thông tin từ query (orderInfo, transactionId, …) vào VNPayResponse, trả JSON. |
| **VNPayService** | `createOrder(int total, String orderInfor, String urlReturn, String ipAddr)` | Tạo đủ tham số VNPay (version, command, amount×100, returnUrl, TxnRef, CreateDate, ExpireDate, …), sort key, build chuỗi hash (đã URL-encode), gọi Config.hmacSHA512, lắp query + vnp_SecureHash, trả URL thanh toán đầy đủ. |
| **VNPayService** | `orderReturn(HttpServletRequest)` | Thu thập toàn bộ query params, bỏ vnp_SecureHash/vnp_SecureHashType, verify chữ ký bằng 3 cách (encoded, raw, raw query), so sánh với vnp_SecureHash; kiểm tra vnp_TransactionStatus. Trả 1 (thành công), 0 (thất bại), -1 (chữ ký lỗi). |
| **VNPayConfig** | `getIpAddress(HttpServletRequest)` | Lấy IP khách: ưu tiên header X-Forwarded-For, không có thì RemoteAddr. Dùng cho tham số vnp_IpAddr (bắt buộc theo VNPay). |
| **VNPayConfig** | `getRandomNumber(int len)` | Sinh chuỗi số ngẫu nhiên độ dài `len` (dùng làm vnp_TxnRef – mã tham chiếu đơn hàng). |
| **VNPayConfig** | `hmacSHA512(String key, String data)` | Tạo chữ ký HMAC SHA512: key và data UTF-8, trả chuỗi hex. Dùng khi tạo URL thanh toán và khi verify return. |
| **VNPayConfig** | `hashAllFields(Map<String,String> fields)` | Sort key theo alphabet, nối `key=value` (giá trị gốc, không encode), gọi hmacSHA512. Dùng verify return (một trong ba cách). |
| **VNPayConfig** | `hashAllFieldsEncoded(Map<String,String> fields)` | Sort key, nối `key=value` với key/value đã **URL-encode UTF-8**, gọi hmacSHA512. Dùng verify return (chuẩn 2.1.0). |
| **VNPayConfig** | `hashQueryString(String queryString)` | Tách query theo `&`, bỏ cặp `vnp_SecureHash` và `vnp_SecureHashType`, nối lại chuỗi, gọi hmacSHA512. Dùng verify return (trường hợp VNPay ký đúng chuỗi query gửi đi). |

---

## 4. API cụ thể để test bằng Postman

Base URL (project dùng context-path `/api`, port 8080):

- **Base:** `http://localhost:8080/api`

---

### 4.1. Đăng nhập lấy token (bắt buộc nếu submitOrder yêu cầu auth)

| Thuộc tính | Giá trị |
|------------|--------|
| Method | `POST` |
| URL | `http://localhost:8080/api/auth/login` |
| Headers | `Content-Type: application/json` |
| Body (raw, JSON) | `{"username":"admin","password":"1"}` |

**Response mẫu:**  
Lấy `result.token` để dùng cho request submitOrder.

```json
{
  "code": 1000,
  "result": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 4.2. Tạo đơn VNPay (lấy URL redirect)

| Thuộc tính | Giá trị |
|------------|--------|
| Method | `POST` |
| URL | `http://localhost:8080/api/payments/submitOrder` |
| Headers | `Content-Type: application/json`<br>`Authorization: Bearer <token>` |
| Body (raw, JSON) | Xem bên dưới |

**Body mẫu:**

```json
{
  "amount": 100000,
  "orderInfo": "Thanh toan don hang test so 01"
}
```

- **amount**: Số tiền **VND** (ví dụ 100000 = 100k). Backend gửi sang VNPay dạng xu (×100).
- **orderInfo**: Nội dung hiển thị trên VNPay.

**Response mẫu (200):**

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

**Cách test:** Copy `redirectUrl` → dán vào trình duyệt → thực hiện thanh toán bằng thẻ test VNPay Sandbox (ví dụ thẻ `9704198526191432198`, OTP `123456`) → sau đó VNPay sẽ redirect về Return URL và backend trả JSON tại mục 3.3.

---

### 4.3. Return URL (VNPay redirect – không gọi tay trong Postman)

Khi user thanh toán xong trên VNPay, **trình duyệt** sẽ được redirect tới:

- **URL:** `GET http://localhost:8080/api/payments/vnpay-payment?vnp_TransactionStatus=00&vnp_Amount=...&vnp_SecureHash=...` (các tham số do VNPay thêm).

**Có thể mô phỏng trong Postman** (chỉ để kiểm tra format response, chữ ký sẽ sai nếu tự tạo tay):

| Thuộc tính | Giá trị |
|------------|--------|
| Method | `GET` |
| URL | `http://localhost:8080/api/payments/vnpay-payment` |
| Params | Thêm đúng bộ params mà VNPay gửi (sau lần thanh toán thật từ redirectUrl). Không cần Authorization. |

**Response mẫu (khi thanh toán thành công, paymentStatus=1):**

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

- **totalPrice**: Đơn vị **xu** (10000000 = 100.000 VND).
- **paymentStatus**: `1` = thành công, `0` = thất bại/hủy, `-1` = chữ ký không hợp lệ.

---

## 5. Thẻ test VNPay Sandbox

**Chỉ dùng với ngân hàng NCB:** Khi chọn "Thẻ nội địa", hãy chọn **NCB** (Ngân hàng TMCP Quốc Dân). Các ngân hàng khác (Agribank, Sacombank, v.v.) trên sandbox thường báo "Số thẻ chưa hợp lệ".

| Kịch bản | Số thẻ | OTP |
|----------|--------|-----|
| Thanh toán OK | `9704198526191432198` | `123456` |
| Không đủ số dư | `9704195798459170488` | - |
| Thẻ chưa kích hoạt | `9704192181368742` | - |

---

## 6. Checklist test nhanh Postman

1. **POST** `http://localhost:8080/api/auth/login` với body `{"username":"admin","password":"1"}` → copy `result.token`.
2. **POST** `http://localhost:8080/api/payments/submitOrder` với header `Authorization: Bearer <token>` và body `{"amount": 100000, "orderInfo": "Postman test"}` → copy `redirectUrl`.
3. Mở `redirectUrl` trong trình duyệt → chọn phương thức, nhập thẻ test `9704198526191432198`, OTP `123456` → hoàn tất.
4. Trình duyệt redirect về `http://localhost:8080/api/payments/vnpay-payment?...` → trang (hoặc API) trả JSON với `paymentStatus: 1` và `message: "Thanh toán thành công"`.

---

## 7. Lưu ý

- **Return URL** phải trùng cấu hình: `VNPayConfig.vnp_ReturnUrl = "/api/payments/vnpay-payment"` và baseUrl từ controller không chứa path (chỉ scheme + host + port).
- Chữ ký VNPay: dữ liệu đưa vào HMAC SHA512 là chuỗi **không URL-encode**; chỉ có query string gửi đi mới encode.
- Endpoint **GET /payments/vnpay-payment** được cấu hình **permitAll()** trong SecurityConfig để VNPay redirect không bị 401.

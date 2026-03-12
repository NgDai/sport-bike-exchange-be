# Hướng dẫn test VNPay

Project dùng **context-path** `/api`, port mặc định `8080`. Các bước dưới giúp bạn test luồng thanh toán VNPay Sandbox từ đầu đến cuối.

---

## 1. Chạy Backend

```bash
cd sport-bike-exchange-be
# Backend là Java Spring Boot, chạy bằng Maven:
mvn spring-boot:run
```

Hoặc mở project bằng IDE (IntelliJ/Eclipse) và chạy class có `@SpringBootApplication`.

Backend chạy tại: **http://localhost:8080**  
Base URL API: **http://localhost:8080/api**

---

## 2. Tạo phiên thanh toán (lấy URL redirect VNPay)

Gọi API tạo đơn để lấy `redirectUrl` đưa user sang trang VNPay.

**Request**

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/payments/submitOrder`
- **Headers:** `Content-Type: application/json`
- **Body (JSON):**

```json
{
  "amount": 100000,
  "orderInfo": "Thanh toan don hang test so 01"
}
```

- `amount`: số tiền **VND** (ví dụ 100000 = 100k VND).
- `orderInfo`: nội dung/mô tả đơn (VNPay hiển thị cho user).

**Ví dụ bằng cURL:**

```bash
curl -X POST "http://localhost:8080/api/payments/submitOrder" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 100000, \"orderInfo\": \"Test don hang 01\"}"
```

**Ví dụ bằng PowerShell:**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/payments/submitOrder" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"amount": 100000, "orderInfo": "Test don hang 01"}'
```

**Response mẫu:**

```json
{
  "redirectUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&vnp_Command=pay&...",
  "paymentStatus": null,
  "orderInfo": null,
  "paymentTime": null,
  "transactionId": null,
  "totalPrice": null
}
```

Lấy giá trị `redirectUrl` để dùng ở bước 3.

---

## 3. Thanh toán trên VNPay Sandbox

1. Mở **`redirectUrl`** (từ `redirectUrl` trong response) trên trình duyệt.
2. Trên trang VNPay Sandbox, chọn phương thức (ATM, QR, ví…).
3. **Quan trọng – Chọn đúng ngân hàng:** Thẻ test chỉ chạy với **NCB** (Ngân hàng TMCP Quốc Dân). Nếu chọn **Agribank, Sacombank, Vietcombank**, v.v. sandbox có thể báo "Số thẻ chưa hợp lệ" vì môi trường test các ngân hàng khác bị giới hạn. → Hãy chọn **NCB** khi test thẻ nội địa.
4. Nhập thông tin thẻ **test** (chỉ dùng trên sandbox):

### Thẻ test VNPay Sandbox (dùng với NCB)

| Kịch bản       | Số thẻ                 | OTP     |
|----------------|------------------------|--------|
| Thanh toán OK  | `9704198526191432198`  | `123456` |
| Không đủ số dư | `9704195798459170488`  | -      |
| Thẻ chưa kích hoạt | `9704192181368742` | -      |
| Thẻ bị khóa    | `9704193370791314`     | -      |
| Thẻ hết hạn    | `9704194841945513`     | -      |

- Tên chủ thẻ: `NGUYEN VAN A`, Ngày phát hành: `07/15` (hoặc `03/07`, `10/26`).

4. Sau khi xác thực, VNPay sẽ **redirect** trình duyệt về URL return của bạn:  
   `http://localhost:8080/api/payments/vnpay-payment?vnp_TransactionStatus=00&vnp_...`

Backend nhận request GET này và trả JSON kết quả.

---

## 4. Kết quả tại Return URL

Khi VNPay redirect về `/api/payments/vnpay-payment`, backend trả JSON với ý nghĩa `paymentStatus`:

| paymentStatus | Ý nghĩa                          |
|---------------|-----------------------------------|
| `1`           | Thanh toán thành công             |
| `0`           | Giao dịch thất bại / hủy / lỗi   |
| `-1`          | Chữ ký không hợp lệ (bảo mật)    |

Response mẫu (thành công):

```json
{
  "redirectUrl": null,
  "paymentStatus": 1,
  "orderInfo": "Thanh toan don hang test so 01",
  "paymentTime": "20250303120000",
  "transactionId": "14234567",
  "totalPrice": "10000000"
}
```

(Lưu ý: `totalPrice` từ VNPay là **đơn vị xu** – ví dụ 10000000 = 100.000 VND.)

---

## 5. Test nhanh bằng Postman

1. **Tạo request:**  
   - Method: `POST`  
   - URL: `http://localhost:8080/api/payments/submitOrder`  
   - Body → raw → JSON:

   ```json
   {
     "amount": 50000,
     "orderInfo": "Postman test order"
   }
   ```

2. Send → copy `redirectUrl` từ response.
3. Dán `redirectUrl` vào tab mới trên trình duyệt → thực hiện thanh toán bằng thẻ test → kiểm tra redirect về và response JSON tại return URL.

---

## 6. Lưu ý khi test

- **Return URL** đã được cấu hình là `/api/payments/vnpay-payment` (đúng với `context-path=/api`). Nếu đổi port hoặc context-path, cần cập nhật `VNPayConfig.vnp_ReturnUrl` tương ứng (hoặc build full URL từ config).
- Chỉ dùng **sandbox** và **thẻ test** ở trên; không dùng thẻ thật.
- Merchant code và secret trong code là cấu hình sandbox mẫu; production cần dùng tài khoản VNPay thật và bảo mật secret (env/config).

---

## 7. Tài liệu tham khảo VNPay

- Demo/tài liệu Sandbox: https://sandbox.vnpayment.vn/apis/vnpay-demo/
- Tạo đơn thử nghiệm: http://sandbox.vnpayment.vn/tryitnow/Home/CreateOrder

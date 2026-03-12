# Transaction – Luồng và kết nối VNPay

Mô tả ngắn gọn các **luồng** và **cách kết nối VNPay với CRUD Transaction**.

---

## 1. Transaction là gì?

Giao dịch mua bán giữa Buyer và Seller cho một bài đăng (BikeListing), gắn với Event, Reservation, Deposit, số tiền và trạng thái. **Xe đã lên sàn (BikeListing) tức là seller đã đồng ý bán** — không có bước "seller đồng ý/từ chối" riêng. Nghiệp vụ dùng field **status** có sẵn (vd. PENDING, CONFIRMED, COMPLETED). Địa điểm/thời gian có thể gắn qua **Event** (Transaction đã có event_id).

---

## 2. Các luồng chính

### Luồng tạo Transaction (deal mua bán) – theo yêu cầu mới

1. **Seller đăng xe (BikeListing)** → xe lên sàn = **seller đã đồng ý bán** (không cần bước đồng ý/từ chối sau).
2. **Người mua muốn mua:** Gửi yêu cầu **và đồng thời đặt cọc** (Reservation + Deposit + POST /transactions, sau đó thanh toán VNPay).
3. Hệ thống tạo Transaction (status PENDING). Sau khi thanh toán VNPay thành công, backend cập nhật theo nghiệp vụ (Giai đoạn E).
4. **Admin:** Cập nhật giao dịch (event, status, amount...) qua **PUT /transactions/{id}**. Địa điểm/thời gian có thể dùng Event (gán eventId).

```
Seller tạo BikeListing (xe lên sàn = đã đồng ý bán)
  → Buyer gửi yêu cầu + đặt cọc (Reservation + Deposit + POST /transactions, thanh toán VNPay)
  → Admin cập nhật giao dịch / gán event (PUT /transactions/{id})
  → (Tuỳ nghiệp vụ) Cập nhật listing status = completed khi hoàn tất
```

- **POST /transactions** (người mua tạo, đã đăng nhập): tạo Transaction với đủ thông tin listing, buyer, seller, deposit, reservation; status mặc định PENDING.
- **PUT /transactions/{id}** (chỉ ADMIN): cập nhật event, listing, buyer, seller, deposit, reservation, status, amount, actualPrice, fee.

### Luồng User ↔ Hệ thống (ví)

- **Nạp ví:** User thanh toán VNPay → tiền về merchant → backend cộng Wallet user, ghi WalletTransaction (nạp).
- **Thanh toán / Hoàn tiền:** Trừ ví buyer hoặc cộng ví seller / hoàn cọc theo nghiệp vụ, ghi WalletTransaction tương ứng.

### Luồng Hệ thống ↔ Seller

- Khi deal hoàn tất: trừ tiền buyer (đã trừ khi cọc/thanh toán), cộng tiền vào Wallet seller (trừ phí nếu có), ghi WalletTransaction. Transaction và Wallet nối qua User (buyer/seller → Users → Wallet).

---

## 3. Kết nối VNPay với CRUD Transaction

**Ý tưởng:** Mỗi lần gọi VNPay cần lưu “đơn thanh toán” (pending). Khi VNPay redirect về **thành công**, dùng mã đơn để tạo hoặc cập nhật Transaction (và Wallet/WalletTransaction).

### Bước 1 – Khi tạo URL thanh toán (submitOrder)

- Client gọi **POST /payments/submitOrder** (amount, orderInfo, có thể thêm: depositId, listingId, buyerId, sellerId… tùy nghiệp vụ).
- Backend:
  - Tạo bản ghi **đơn thanh toán** (vd: bảng `PaymentOrder` hoặc tương đương): lưu **mã tham chiếu** (dùng `vnp_TxnRef` từ VNPay hoặc mã tự sinh), **loại** (nạp ví / thanh toán cọc / thanh toán deal), **userId**, (nếu có) **depositId**, **reservationId**, **listingId**, **amount**, **status = PENDING**.
  - Gọi `VNPayService.createOrder(...)`, đảm bảo **orderInfo** chứa mã đơn (hoặc vnp_TxnRef) để khi return biết đơn nào.
  - Trả về **redirectUrl** cho client.

### Bước 2 – Khi VNPay redirect về (vnpay-payment)

- VNPay GET về **/api/payments/vnpay-payment** với query params (vnp_TransactionStatus, vnp_TxnRef, vnp_OrderInfo, …).
- Backend:
  - Verify chữ ký (đã có trong `VNPayService.orderReturn`).
  - Nếu **paymentStatus != 1**: cập nhật đơn thanh toán sang FAILED/CANCELLED, trả JSON; không tạo Transaction.
  - Nếu **paymentStatus == 1**:
    - Lấy **vnp_TxnRef** (hoặc decode từ **orderInfo**) → tìm bản ghi đơn thanh toán tương ứng.
    - Theo **loại đơn**:
      - **Nạp ví:** cộng `Wallet.balance` user, tạo WalletTransaction (nạp). Không tạo Transaction.
      - **Thanh toán cọc / deal:** lấy từ đơn đã lưu: listingId, buyerId, sellerId, depositId, reservationId, eventId (nếu có). Gọi **TransactionService.createTransaction(...)** (hoặc update nếu đã tạo trước với status pending) với đầy đủ thông tin → **Transaction** được tạo/cập nhật (CRUD Transaction). Sau đó đồng bộ ví: trừ ví buyer, cộng ví seller, tạo WalletTransaction (PAYMENT / RECEIVE).
  - Cập nhật đơn thanh toán sang COMPLETED, trả JSON cho client.

### Tóm tắt kết nối

| VNPay | CRUD Transaction |
|-------|-------------------|
| submitOrder | Lưu đơn (pending) + mã trong orderInfo/vnp_TxnRef |
| vnpay-payment (success) | Tìm đơn theo mã → gọi createTransaction/updateTransaction với dữ liệu từ đơn → cập nhật Wallet + WalletTransaction |

---

## 4. Sơ đồ luồng (tóm tắt)

```
Client: POST /payments/submitOrder (amount, orderInfo, ...)
    → Backend: lưu PaymentOrder (pending, vnp_TxnRef, type, ids...)
    → VNPayService.createOrder(...), orderInfo chứa mã đơn
    → Response: redirectUrl

User thanh toán trên VNPay → VNPay redirect GET /api/payments/vnpay-payment?...
    → Backend: verify chữ ký
    → paymentStatus == 1: tìm PaymentOrder theo vnp_TxnRef/orderInfo
        → Nạp ví: cộng Wallet, WalletTransaction
        → Thanh toán deal: TransactionService.createTransaction(...) + cập nhật 2 ví + WalletTransaction
    → Response: JSON (paymentStatus, message, ...)
```

---

## 5. File liên quan

- **Transaction:** `entities/Transaction.java`, `TransactionController`, `TransactionService`, DTO/Mapper.
- **VNPay:** `PaymentController`, `VNPayService`, `VNPayConfig`.
- **Wallet:** `Wallet`, `WalletTransaction`, `WalletService`.
- Luồng chi tiết: `BIKELISTING_FLOW.md`, `VNPAY_FLOW_AND_POSTMAN_API.md`.

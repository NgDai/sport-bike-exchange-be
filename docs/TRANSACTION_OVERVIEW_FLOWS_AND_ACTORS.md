# Transaction – Luồng xử lý, thời điểm tạo, và các bên giao dịch

Tài liệu trả lời ba câu: **Transaction xử lý những luồng nào**, **được tạo ra khi nào**, **giao dịch với những ai**.

---

## 1. Transaction xử lý những luồng nào?

Transaction trong project này là **giao dịch mua bán hoàn tất** (deal) cho một bài đăng xe (BikeListing), gắn với đặt cọc (Deposit), đặt chỗ (Reservation), sự kiện (Event), số tiền và trạng thái. Các luồng có liên quan:

### 1.1. Luồng chính – Deal mua bán (BikeListing)

```
Seller tạo BikeListing
    → Buyer đặt chỗ (Reservation)
    → Buyer nộp cọc (Deposit)
    → Tạo Transaction (ghi nhận deal hoàn tất)
    → (Theo nghiệp vụ) Cập nhật listing status = completed
```

- **Transaction** nằm ở bước “ghi nhận deal hoàn tất”: một bản ghi Transaction = một deal đã hoàn tất giữa **một Buyer** và **một Seller** cho **một Listing**, gắn **một Deposit** và **một Reservation** (và có thể **một Event**).

### 1.2. Luồng phụ – Kết nối VNPay (theo thiết kế, chưa implement đầy đủ)

- **Nạp ví:** User thanh toán VNPay → tiền về merchant → cộng Wallet, ghi **WalletTransaction**. **Không** tạo Transaction (Transaction chỉ dùng cho deal mua bán).
- **Thanh toán cọc / thanh toán deal:** User thanh toán qua VNPay cho cọc hoặc deal → khi VNPay redirect về **thành công** (paymentStatus = 1), backend (theo doc) sẽ gọi **TransactionService.createTransaction(...)** với dữ liệu từ đơn thanh toán (PaymentOrder). Hiện tại trong code **chưa có** PaymentOrder và chưa có đoạn gọi `createTransaction` trong PaymentController khi VNPay return.

### 1.3. Luồng Hệ thống ↔ Seller (sau khi có Transaction)

- Khi deal hoàn tất (đã có Transaction): trừ tiền buyer (đã trừ khi cọc/thanh toán), cộng tiền vào Wallet seller (trừ phí nếu có), ghi **WalletTransaction**. Transaction và Wallet nối qua User (buyer/seller → Users → Wallet). Phần này có thể nằm ở WalletService / payment service, không nằm trong TransactionService hiện tại.

**Tóm tắt luồng mà Transaction tham gia:**

| Luồng | Transaction đóng vai trò |
|-------|---------------------------|
| Deal mua bán (listing) | **Luồng chính:** Transaction = bản ghi “deal hoàn tất” (buyer + seller + listing + deposit + reservation + event + amount/status). |
| VNPay thanh toán cọc/deal | **Luồng thiết kế:** Khi VNPay return success, tạo Transaction từ PaymentOrder (chưa implement trong code). |
| Ví (nạp / trừ buyer / cộng seller) | Transaction **không** xử lý trực tiếp; Wallet/WalletTransaction xử lý. Transaction chỉ cung cấp thông tin buyer/seller/amount để service khác trừ/cộng ví. |

---

## 2. Transaction được tạo ra khi nào?

### 2.1. Hiện tại (đã implement)

- **Chỉ** khi client gọi **POST /api/transactions** với body đủ: `depositId`, `reservationId` (bắt buộc), và các id khác (eventId, listingId, buyerId, sellerId) cùng amount, actualPrice, status.
- Tức là: **Transaction được tạo khi có request tạo giao dịch từ phía client** (admin/user đã đăng nhập), sau khi đã có Deposit và Reservation tương ứng.

### 2.2. Theo thiết kế (TRANSACTION.md / TRANSACTION_IMPLEMENTATION_PLAN.md)

- **Khi VNPay redirect về thành công** (GET /api/payments/vnpay-payment, paymentStatus == 1) và đơn thanh toán có loại **THANH_TOAN_COC** hoặc **THANH_TOAN_DEAL**:
  - Backend tìm **PaymentOrder** theo `vnp_TxnRef` (hoặc orderInfo).
  - Lấy từ PaymentOrder: depositId, reservationId, listingId, buyerId, sellerId, eventId, amount.
  - Gọi **TransactionService.createTransaction(...)** với dữ liệu đó → **Transaction được tạo** tại thời điểm “thanh toán VNPay thành công cho cọc/deal”.
- Hiện tại **chưa có** entity PaymentOrder và chưa có đoạn code trong PaymentController gọi `createTransaction` khi VNPay return, nên trong thực tế Transaction **chỉ** được tạo qua POST /api/transactions.

**Tóm tắt:**

| Thời điểm | Cách tạo | Trạng thái |
|-----------|----------|------------|
| Client gọi POST /api/transactions | TransactionService.createTransaction(request) | Đã có trong code |
| VNPay return success (thanh toán cọc/deal) | Tìm PaymentOrder → createTransaction(...) | Chỉ trong tài liệu, chưa implement |

---

## 3. Giao dịch với những ai? (Các bên liên quan)

Transaction là giao dịch **giữa hai bên người dùng** trên nền tảng, và gắn với **các entity nghiệp vụ** của hệ thống:

### 3.1. Hai bên chính (con người / tài khoản)

| Bên | Entity | Ý nghĩa |
|-----|--------|--------|
| **Người mua (Buyer)** | **Users** (buyer_id) | User đặt chỗ, nộp cọc và hoàn tất thanh toán cho listing. |
| **Người bán (Seller)** | **Users** (seller_id) | User sở hữu bài đăng (BikeListing), nhận tiền khi deal hoàn tất. |

Transaction luôn gắn **một buyer** và **một seller**; mỗi bản ghi Transaction = một deal giữa hai bên đó.

### 3.2. Các đối tượng nghiệp vụ gắn với Transaction (bảng/entity)

| Đối tượng | Entity | Quan hệ | Ý nghĩa |
|-----------|--------|--------|--------|
| **Bài đăng xe** | BikeListing | ManyToOne (listing_id) | Giao dịch cho listing nào. |
| **Đặt cọc** | Deposit | OneToOne (deposit_id) | Cọc của deal này (một deposit chỉ gắn một transaction). |
| **Đặt chỗ** | Reservation | OneToOne (reservation_id) | Đặt chỗ tương ứng. |
| **Sự kiện** | Events | ManyToOne (event_id) | Giao dịch diễn ra trong event nào (nếu có). |

### 3.3. Hệ thống (backend / nền tảng)

- **Hệ thống** không phải “bên thứ ba giao dịch” mà là **bên ghi nhận và lưu trữ** Transaction, quản lý Deposit, Reservation, Wallet, v.v. Sau khi có Transaction, hệ thống (qua WalletService / payment service) có thể **trừ ví buyer**, **cộng ví seller**, ghi WalletTransaction – Transaction cung cấp buyerId, sellerId, amount để thực hiện các bước đó.

**Sơ đồ các bên:**

```
                    [HỆ THỐNG / Nền tảng]
                    - Lưu Transaction, Deposit, Reservation, Listing
                    - (Sau này) Trừ/cộng Wallet, ghi WalletTransaction
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
   [BUYER]                     [Transaction]                 [SELLER]
   Users (buyer_id)             - amount                      Users (seller_id)
   - Đặt chỗ (Reservation)     - actualPrice                 - Chủ listing
   - Nộp cọc (Deposit)          - status                     - Nhận tiền
                                - listing (BikeListing)
                                - deposit (Deposit)
                                - reservation (Reservation)
                                - event (Events, optional)
```

---

## 4. Tóm tắt một dòng

- **Luồng:** Transaction phục vụ **luồng deal mua bán** (listing → reservation → deposit → transaction); theo doc còn tham gia luồng **VNPay thanh toán cọc/deal** (tạo Transaction khi return success – chưa implement).
- **Tạo khi nào:** Hiện tại **chỉ** khi gọi **POST /api/transactions**; theo thiết kế thêm: khi **VNPay return thành công** cho thanh toán cọc/deal (cần PaymentOrder + đoạn gọi createTransaction trong return handler).
- **Giao dịch với ai:** Giao dịch **giữa Buyer (Users)** và **Seller (Users)** cho một **BikeListing**, gắn **Deposit**, **Reservation**, và có thể **Events**; hệ thống là bên ghi nhận và (sau này) điều phối ví.

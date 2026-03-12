# Kế hoạch thực thi: VNPay + Transaction + Wallet

Phân tích **cách thực thi code** theo đúng luồng trong `TRANSACTION.md`. Chỉ mô tả bước làm, không viết code.

---

## 0. Luồng Transaction (đã triển khai – không thêm/sửa entity)

- **Xe đã lên sàn (BikeListing) = seller đã đồng ý bán** — không có bước seller đồng ý/từ chối.
- **Người mua:** Gửi yêu cầu + đặt cọc → tạo Transaction (POST /transactions), status mặc định PENDING.
- **Admin:** Cập nhật thông tin giao dịch qua **PUT /transactions/{id}** (event, listing, buyer, seller, deposit, reservation, status, amount, actualPrice, fee).
- **Lưu ý:** **Giữ nguyên tất cả entities** (không chỉnh sửa Transaction, không thêm entity mới). Các nghiệp vụ dùng chỉ các field và entity hiện có.

---

## 1. Tổng quan thứ tự thực thi

| Giai đoạn | Nội dung |
|-----------|----------|
| **A** | Tạo đơn thanh toán (PaymentOrder) + lấy/trả vnp_TxnRef |
| **B** | Mở rộng DTO/Request và CRUD Transaction (đủ FK) |
| **C** | WalletTransaction: repository + service ghi lịch sử |
| **D** | Nối submitOrder → lưu PaymentOrder, truyền txnRef vào VNPay |
| **E** | Nối vnpay-payment return → tìm đơn, nạp ví hoặc tạo Transaction + ví |

---

## 2. Giai đoạn A – Đơn thanh toán (PaymentOrder)

**Mục đích:** Mỗi lần gọi VNPay phải lưu một bản ghi “đơn thanh toán” với mã tham chiếu. Khi VNPay redirect về, dùng mã đó để biết đơn nào và xử lý (nạp ví hay tạo Transaction).

**Việc cần làm:**

1. **Entity PaymentOrder** (hoặc tên tương đương)
   - Lưu: mã tham chiếu (vd `vnpTxnRef` – string), loại đơn (enum: NAP_VI, THANH_TOAN_COC, THANH_TOAN_DEAL), userId, amount, status (PENDING, COMPLETED, FAILED, CANCELLED).
   - Nếu hỗ trợ thanh toán deal/cọc: thêm depositId, reservationId, listingId, buyerId, sellerId, eventId (nullable).
   - Thời gian: createdAt, (có thể) updatedAt.

2. **Repository**  
   - JpaRepository. Thêm method `findByVnpTxnRef(String vnpTxnRef)` (Optional) để khi return tìm đơn theo `vnp_TxnRef` VNPay gửi về.

3. **vnp_TxnRef phải khớp giữa “lưu đơn” và “gửi VNPay”**
   - Hiện tại `vnp_TxnRef` được sinh **bên trong** `VNPayService.buildPaymentParams` (gọi `VNPayConfig.getRandomNumber(8)`), nên backend không biết giá trị để lưu vào PaymentOrder.
   - **Cách 1:** Sinh `vnp_TxnRef` ở tầng gọi (PaymentController hoặc service mới): trước khi gọi VNPay, gọi `VNPayConfig.getRandomNumber(8)` (hoặc tương đương), lưu PaymentOrder với mã này, rồi truyền mã đó vào VNPayService. Khi đó **VNPayService.createOrder** cần nhận thêm tham số `String vnpTxnRef` (optional): nếu có thì dùng, không có thì sinh trong method như hiện tại (giữ tương thích cũ).
   - **Cách 2:** Đổi `createOrder` trả về một object (vd `CreateOrderResult`) chứa cả `redirectUrl` và `vnpTxnRef`; sau khi gọi xong, controller/service lưu PaymentOrder với `vnpTxnRef` vừa lấy từ kết quả. Lưu ý: VNPayService hiện chỉ return String (URL), nên phải sửa signature và chỗ build params để lấy được txnRef ra ngoài.

Chọn một trong hai cách và thống nhất: **khi submitOrder xong phải có bản ghi PaymentOrder với đúng vnp_TxnRef đã gửi lên VNPay.**

---

## 3. Giai đoạn B – Mở rộng DTO và CRUD Transaction

**Mục đích:** Tạo/cập nhật Transaction từ dữ liệu “đơn thanh toán” (sau khi VNPay return success), nên request phải chứa đủ FK mà entity Transaction cần.

**Việc cần làm:**

1. **TransactionCreationRequest**
   - Hiện chỉ có: status, amount, actualPrice, createdAt, updateAt. Thiếu: eventId, listingId, buyerId, sellerId, depositId, reservationId.
   - Thêm các field id trên (Integer nullable cho eventId nếu không bắt buộc). Khi tạo từ PaymentOrder (thanh toán deal), backend sẽ set từ PaymentOrder vào request rồi gọi TransactionService.createTransaction.

2. **TransactionMapper**
   - Map từ TransactionCreationRequest sang Entity: cần set event, listing, buyer, seller, deposit, reservation qua repository (findById) từ các id trong request. Hiện mapper chỉ map các field đơn giản, không set quan hệ → cần bổ sung (custom method hoặc @AfterMapping) lấy entity từ id rồi set vào Transaction.

3. **TransactionService.createTransaction**
   - Giữ nguyên signature nhận TransactionCreationRequest. Bên trong, mapper (đã mở rộng) sẽ tạo entity đầy đủ từ request (gồm cả quan hệ). Nếu nghiệp vụ “tạo Transaction từ payment” cần logic khác (vd chỉ cho phép status nhất định), có thể tách method riêng kiểu `createTransactionFromPayment(PaymentOrder order)` gọi lại createTransaction với request build từ order.

---

## 4. Giai đoạn C – WalletTransaction (ghi lịch sử ví)

**Mục đích:** Mỗi lần cộng/trừ Wallet cần ghi WalletTransaction để audit và hiển thị lịch sử.

**Việc cần làm:**

1. **Repository**
   - Tạo `IWalletTransactionRepository` extends JpaRepository<WalletTransaction, Integer>. Có thể thêm `List<WalletTransaction> findByWalletOrderByCreateAtDesc(Wallet w)` nếu cần API lịch sử theo ví.

2. **WalletService (hoặc service chung cho payment)**
   - Khi cộng tiền ví (nạp ví): ngoài `wallet.setBalance(...)` và save Wallet, tạo bản ghi WalletTransaction: wallet, amount (dương), type (vd "NAP_VI"), description (có thể chứa vnp_TxnRef hoặc orderId).
   - Khi trừ ví buyer / cộng ví seller (sau khi tạo Transaction): tạo hai WalletTransaction (type PAYMENT cho buyer, RECEIVE cho seller), mô tả có thể gắn transactionId. Tùy nghiệp vụ: có thể tách thành method `recordPaymentFromBuyer(...)` và `recordReceiveForSeller(...)` hoặc một method `settleTransaction(Transaction t)` gọi cả hai.

3. **Entity WalletTransaction**
   - Đã có: wallet, amount, type, description, createAt. Có thể thêm field optional `transactionId` (Integer, FK tới Transaction) để tra cứu “giao dịch ví này thuộc deal nào”.

---

## 5. Giai đoạn D – submitOrder: lưu PaymentOrder + dùng chung vnp_TxnRef

**Mục đích:** Khi client gọi POST /payments/submitOrder, backend vừa tạo đơn thanh toán vừa tạo URL VNPay với cùng mã tham chiếu.

**Việc cần làm:**

1. **VNPayRequest mở rộng**
   - Thêm field (optional): paymentType (NAP_VI / THANH_TOAN_COC / THANH_TOAN_DEAL), depositId, reservationId, listingId, (buyerId/sellerId/eventId nếu không lấy từ token hoặc listing). Client gửi theo nghiệp vụ (nạp ví chỉ cần amount + orderInfo; thanh toán deal cần thêm ids).

2. **Lấy userId**
   - Từ SecurityContextHolder (user đang đăng nhập) để lưu vào PaymentOrder và dùng cho nạp ví / buyer.

3. **Luồng trong PaymentController (hoặc PaymentService mới)**
   - Nhận request, lấy userId.
   - Sinh `vnpTxnRef` (VNPayConfig.getRandomNumber(8) hoặc format dài hơn nếu cần).
   - Tạo bản ghi PaymentOrder: vnpTxnRef, paymentType, userId, amount, depositId/reservationId/listingId/buyerId/sellerId/eventId (theo request), status = PENDING. Save.
   - Gọi VNPayService.createOrder(amount, orderInfo, baseUrl, clientIp, vnpTxnRef). Trong đó orderInfo có thể = "Ma don: " + vnpTxnRef (hoặc mã đơn nội bộ) để khi return vẫn đọc được. VNPayService phải dùng đúng vnpTxnRef này khi build params (xem Giai đoạn A).
   - Trả response với redirectUrl (và có thể trả thêm orderId/vnpTxnRef cho client lưu).

---

## 6. Giai đoạn E – vnpay-payment return: tìm đơn → nạp ví hoặc tạo Transaction + ví

**Mục đích:** Khi VNPay GET /api/payments/vnpay-payment, verify xong thì theo kết quả cập nhật đơn và thực hiện nghiệp vụ (nạp ví hoặc tạo Transaction + trừ/cộng ví).

**Việc cần làm:**

1. **Lấy tham số từ request**
   - vnp_TransactionStatus, vnp_TxnRef (bắt buộc để tìm đơn), vnp_OrderInfo, vnp_Amount, … như hiện tại.

2. **Verify chữ ký**
   - Giữ nguyên VNPayService.orderReturn(request). Nếu trả -1 → trả JSON lỗi, không cập nhật đơn. Nếu 0 (thất bại/hủy) → tìm đơn theo vnp_TxnRef, cập nhật status = FAILED hoặc CANCELLED, trả JSON.

3. **Khi paymentStatus == 1**
   - Tìm PaymentOrder theo vnp_TxnRef (repository.findByVnpTxnRef). Nếu không tìm thấy → log, trả JSON lỗi (đơn không tồn tại).
   - Nếu tìm thấy và đã COMPLETED → có thể coi là idempotent: trả JSON thành công, không xử lý lại (tránh cộng ví hai lần).
   - Nếu status = PENDING:
     - Cập nhật PaymentOrder status = COMPLETED (và có thể lưu thêm vnp_TransactionNo, vnp_PayDate từ request).
     - Theo **paymentType**:
       - **NAP_VI:** Lấy userId từ PaymentOrder → Wallet user (hoặc tạo ví nếu chưa có như WalletService hiện tại) → cộng balance đúng amount (lưu ý vnp_Amount từ VNPay là x100, cần chia 100). Tạo WalletTransaction (type NAP_VI, amount, description). Không gọi TransactionService.
       - **THANH_TOAN_COC / THANH_TOAN_DEAL:** Từ PaymentOrder lấy depositId, reservationId, listingId, buyerId, sellerId, eventId. Build TransactionCreationRequest với **buyerDepositStatus=PAID**, sellerAcceptStatus=PENDING, status PENDING (hoặc theo nghiệp vụ). Gọi TransactionService.createTransaction(request). Sau đó: trừ Wallet buyer (và ghi WalletTransaction PAYMENT), cộng Wallet seller (và ghi WalletTransaction RECEIVE). Số tiền và ví lấy từ User (buyer/seller) của Transaction vừa tạo.
   - Trả VNPayResponse JSON như hiện tại (paymentStatus, message, orderInfo, …).

4. **Giao dịch DB**
   - Toàn bộ bước “cập nhật PaymentOrder + nạp ví hoặc tạo Transaction + Wallet + WalletTransaction” nên nằm trong **@Transactional** để hỏng thì rollback (tránh đơn đã COMPLETED nhưng ví chưa cộng hoặc ngược lại).

---

## 7. Thứ tự triển khai gợi ý

1. **A** – PaymentOrder entity + repository + (sửa VNPayService để nhận/trả vnp_TxnRef).
2. **B** – TransactionCreationRequest + TransactionMapper + TransactionService (đủ FK).
3. **C** – IWalletTransactionRepository + WalletService ghi WalletTransaction (nạp ví, thanh toán/recieve).
4. **D** – Mở rộng VNPayRequest, PaymentController/PaymentService: submitOrder tạo PaymentOrder và gọi VNPay với cùng txnRef.
5. **E** – handleVnPayReturn: tìm PaymentOrder, cập nhật status, nhánh nạp ví vs tạo Transaction + ví, trong @Transactional.

---

## 8. Điểm cần lưu ý khi code

- **vnp_Amount:** VNPay gửi amount × 100 (đơn vị xu). Khi cộng Wallet phải chia 100 hoặc lưu đơn vị thống nhất (VND).
- **Idempotent return URL:** VNPay có thể gọi return nhiều lần; nên kiểm tra PaymentOrder đã COMPLETED thì không xử lý lại.
- **Quyền:** submitOrder nên yêu cầu user đăng nhập (buyer = current user khi thanh toán deal). handleVnPayReturn vẫn permitAll (VNPay không gửi JWT).
- **Lỗi:** Thiếu deposit/listing/reservation khi thanh toán deal → báo lỗi rõ, không tạo Transaction thiếu dữ liệu.

Khi triển khai từng giai đoạn, tham chiếu lại `TRANSACTION.md` để đảm bảo luồng và kết nối VNPay ↔ CRUD Transaction đúng như mô tả.

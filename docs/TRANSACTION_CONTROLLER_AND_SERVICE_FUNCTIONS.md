# Giải thích từng function trong TransactionController và TransactionService

Tài liệu mô tả **chức năng**, **tham số**, **luồng xử lý** và **giá trị trả về** của từng method chính.

---

## 1. TransactionController

Controller nhận HTTP request, gọi Service tương ứng, bọc kết quả vào **ApiResponse** và trả JSON. Base path: **`/api/transactions`**.

---

### 1.1. `createTransaction(TransactionCreationRequest request)`

- **Endpoint:** `POST /api/transactions`
- **Quyền:** `@PreAuthorize("isAuthenticated()")` – chỉ cần đăng nhập.
- **Tham số:** `request` – body JSON map vào **TransactionCreationRequest** (eventId, listingId, buyerId, sellerId, **depositId**, **reservationId**, status, amount, actualPrice). `@Valid` kiểm tra validation (depositId, reservationId bắt buộc).
- **Chức năng:** Tạo một giao dịch (deal) mới.
- **Luồng:**
  1. Tạo `ApiResponse<TransactionResponse>`.
  2. Gọi `transactionService.createTransaction(request)` → nhận `TransactionResponse`.
  3. Set `result` và `message = "Transaction created successfully"`.
  4. Return apiResponse (Spring serialize thành JSON).
- **Trả về:** `ApiResponse<TransactionResponse>` (code 1000, result chứa transaction vừa tạo).
- **Lỗi có thể:** 400 (validation thiếu depositId/reservationId), 4xx (EVENT_NOT_FOUND, USER_NOT_FOUND, DEPOSIT_NOT_FOUND, RESERVATION_NOT_FOUND, DEPOSIT_ALREADY_HAS_TRANSACTION, …) do Service ném `AppException`.

---

### 1.2. `updateTransaction(int transactionId, TransactionUpdateRequest request)`

- **Endpoint:** `PUT /api/transactions/{transactionId}`
- **Quyền:** `@PreAuthorize("hasRole('ADMIN')")` – chỉ ADMIN.
- **Tham số:**
  - `transactionId` – ID giao dịch cần sửa (từ path).
  - `request` – body JSON map vào **TransactionUpdateRequest** (các field nullable: eventId, listingId, buyerId, sellerId, depositId, reservationId, status, amount, actualPrice).
- **Chức năng:** Cập nhật một giao dịch đã tồn tại; chỉ cập nhật những trường được gửi trong body.
- **Luồng:**
  1. Tạo ApiResponse.
  2. Gọi `transactionService.updateTransaction(transactionId, request)`.
  3. Set result và message `"Transaction updated successfully"`.
  4. Return apiResponse.
- **Trả về:** `ApiResponse<TransactionResponse>` (transaction sau khi cập nhật).
- **Lỗi có thể:** 404/4xx nếu không tìm thấy transaction hoặc id liên quan (event, user, deposit, …) không tồn tại.

---

### 1.3. `getAllTransactions()`

- **Endpoint:** `GET /api/transactions`
- **Quyền:** `hasRole('ADMIN')`.
- **Tham số:** Không.
- **Chức năng:** Lấy danh sách tất cả giao dịch.
- **Luồng:**
  1. Gọi `transactionService.findAllTransactionResponses()` → `List<TransactionResponse>`.
  2. Set result và message `"Transactions fetched successfully"`.
  3. Return apiResponse.
- **Trả về:** `ApiResponse<List<TransactionResponse>>`.

---

### 1.4. `getTransactionById(int transactionId)`

- **Endpoint:** `GET /api/transactions/{transactionId}`
- **Quyền:** `hasRole('ADMIN')`.
- **Tham số:** `transactionId` – ID giao dịch (từ path).
- **Chức năng:** Lấy chi tiết một giao dịch theo ID.
- **Luồng:**
  1. Gọi `transactionService.findTransactionById(transactionId)`.
  2. Set result và message `"Transaction fetched successfully"`.
  3. Return apiResponse.
- **Trả về:** `ApiResponse<TransactionResponse>`.
- **Lỗi có thể:** 4xx nếu không tìm thấy (TRANSACTION_NOT_FOUND).

---

### 1.5. `deleteTransaction(int transactionId)`

- **Endpoint:** `DELETE /api/transactions/{transactionId}`
- **Quyền:** `hasRole('ADMIN')`.
- **Tham số:** `transactionId` – ID giao dịch cần xóa (từ path).
- **Chức năng:** Xóa một giao dịch khỏi DB.
- **Luồng:**
  1. Gọi `transactionService.deleteTransaction(transactionId)` → trả String thông báo.
  2. Set result = chuỗi đó (message có thể null).
  3. Return apiResponse.
- **Trả về:** `ApiResponse<String>` (result = `"Transaction deleted successfully"`).
- **Lỗi có thể:** 4xx nếu không tìm thấy transaction.

---

### 1.6. `getTransactionsByStatus(String status)`

- **Endpoint:** `GET /api/transactions/status/{status}`
- **Quyền:** `hasRole('ADMIN')`.
- **Tham số:** `status` – trạng thái cần lọc (vd: PENDING, COMPLETED) từ path.
- **Chức năng:** Lấy danh sách giao dịch có trạng thái bằng `status`.
- **Luồng:**
  1. Gọi `transactionService.findTransactionResponsesByStatus(status)`.
  2. Set result và message `"Transactions fetched successfully"`.
  3. Return apiResponse.
- **Trả về:** `ApiResponse<List<TransactionResponse>>`.

---

## 2. TransactionService

Service chứa toàn bộ nghiệp vụ: validate ID, load entity liên quan, tạo/sửa/xóa Transaction, map sang DTO. Dùng **ITransactionRepository**, **TransactionMapper** và các repository khác (Event, BikeListing, User, Deposit, Reservation).

---

### 2.1. `createTransaction(TransactionCreationRequest request)`

- **Annotation:** `@Transactional` – toàn bộ thao tác trong một transaction DB (lỗi thì rollback).
- **Tham số:** `request` – DTO tạo giao dịch (eventId, listingId, buyerId, sellerId, depositId, reservationId, status, amount, actualPrice).
- **Chức năng:** Tạo một bản ghi Transaction mới sau khi kiểm tra deposit chưa có transaction và load đủ entity liên quan.

**Luồng chi tiết:**

1. **Load Event (nếu có):** Nếu `request.getEventId() != null` → `eventRepository.findById(eventId)`. Không có → throw `AppException(ErrorCode.EVENT_NOT_FOUND)`.
2. **Load BikeListing (nếu có):** Nếu `request.getListingId() != null` → `bikeListingRepository.findById(listingId)`. Không có → BIKE_LISTING_NOT_FOUND.
3. **Load Buyer:** Nếu `request.getBuyerId() != null` → `userRepository.findById(buyerId)`. Không có → USER_NOT_FOUND.
4. **Load Seller:** Nếu `request.getSellerId() != null` → `userRepository.findById(sellerId)`. Không có → USER_NOT_FOUND.
5. **Load Deposit và kiểm tra trùng:** Nếu `request.getDepositId() != null`:
   - `depositRepository.findById(depositId)` → không có thì DEPOSIT_NOT_FOUND.
   - `transactionRepository.findByDeposit_DepositId(depositId)` → nếu đã có transaction cho deposit này → throw **DEPOSIT_ALREADY_HAS_TRANSACTION** (một deposit chỉ gắn một transaction).
6. **Load Reservation (nếu có):** Nếu `request.getReservationId() != null` → `reservationRepository.findById(reservationId)`. Không có → RESERVATION_NOT_FOUND.
7. **Tạo entity Transaction:** `Transaction.builder()` với event, listing, buyer, seller, deposit, reservation, amount, actualPrice, status (nếu null thì mặc định `"PENDING"`).
8. **Lưu DB:** `transactionRepository.save(transaction)`.
9. **Map sang DTO:** `transactionMapper.toTransactionResponse(transaction)` → **TransactionResponse**.
10. **Return:** TransactionResponse.

- **Trả về:** `TransactionResponse` (chứa transactionId, buyerId, sellerId, eventId, listingId, depositId, reservationId, amount, actualPrice, createdAt, updatedAt, status).

---

### 2.2. `updateTransaction(int transactionId, TransactionUpdateRequest request)`

- **Annotation:** `@Transactional`.
- **Tham số:**
  - `transactionId` – ID giao dịch cần sửa.
  - `request` – DTO cập nhật (các field nullable; chỉ field nào được gửi mới được cập nhật).
- **Chức năng:** Cập nhật một transaction đã tồn tại; có thể đổi event, listing, buyer, seller, deposit, reservation, status, amount, actualPrice.

**Luồng chi tiết:**

1. **Lấy transaction hiện tại:** `transactionRepository.findById(transactionId)`. Không có → throw **TRANSACTION_NOT_FOUND**.
2. **Cập nhật từng nhóm (chỉ khi request có gửi):**
   - `eventId` → load Events, `transaction.setEvent(event)`.
   - `listingId` → load BikeListing, `transaction.setListing(listing)`.
   - `buyerId` → load Users, `transaction.setBuyer(buyer)`.
   - `sellerId` → load Users, `transaction.setSeller(seller)`.
   - `depositId` → load Deposit, `transaction.setDeposit(deposit)`.
   - `reservationId` → load Reservation, `transaction.setReservation(reservation)`.
   - `status` → `transaction.setStatus(request.getStatus())`.
   - `amount` → `transaction.setAmount(request.getAmount())`.
   - `actualPrice` → `transaction.setActualPrice(request.getActualPrice())`.
3. **Lưu:** `transactionRepository.save(transaction)`.
4. **Map và return:** `transactionMapper.toTransactionResponse(transaction)`.

- **Trả về:** `TransactionResponse` (transaction sau khi cập nhật).

**Lưu ý:** Update **không** kiểm tra deposit đã gắn transaction khác; nếu nghiệp vụ cần ràng buộc tương tự khi update depositId thì phải bổ sung logic (vd kiểm tra `findByDeposit_DepositId` trừ transaction hiện tại).

---

### 2.3. `findAllTransactions()`

- **Tham số:** Không.
- **Chức năng:** Lấy tất cả bản ghi Transaction dạng entity (không map sang DTO).
- **Luồng:** `transactionRepository.findAll()` → `List<Transaction>`.
- **Trả về:** `List<Transaction>`.  
- **Ghi chú:** Controller không gọi trực tiếp; dùng khi cần entity trong code (vd service khác). API trả list cho client dùng `findAllTransactionResponses()`.

---

### 2.4. `findAllTransactionResponses()`

- **Tham số:** Không.
- **Chức năng:** Lấy tất cả giao dịch và chuyển sang DTO để trả API.
- **Luồng:**
  1. `transactionRepository.findAll()` → `List<Transaction>`.
  2. Stream: `map(transactionMapper::toTransactionResponse)` → `List<TransactionResponse>`.
- **Trả về:** `List<TransactionResponse>`.

---

### 2.5. `findTransactionById(int transactionId)`

- **Tham số:** `transactionId` – ID giao dịch.
- **Chức năng:** Lấy một giao dịch theo ID, trả dạng DTO.
- **Luồng:**
  1. `transactionRepository.findById(transactionId).orElseThrow(...)` → Transaction hoặc **TRANSACTION_NOT_FOUND**.
  2. `transactionMapper.toTransactionResponse(transaction)`.
- **Trả về:** `TransactionResponse`.

---

### 2.6. `deleteTransaction(int transactionId)`

- **Tham số:** `transactionId` – ID giao dịch cần xóa.
- **Chức năng:** Xóa bản ghi Transaction khỏi DB.
- **Luồng:**
  1. `transactionRepository.findById(transactionId).orElseThrow(...)` → Transaction hoặc TRANSACTION_NOT_FOUND.
  2. `transactionRepository.delete(transaction)`.
  3. Return chuỗi `"Transaction deleted successfully"`.
- **Trả về:** `String`.

---

### 2.7. `findTransactionsByStatus(String status)`

- **Tham số:** `status` – trạng thái (vd PENDING, COMPLETED).
- **Chức năng:** Lấy danh sách Transaction có đúng status, dạng entity.
- **Luồng:** `transactionRepository.findAllByStatus(status)` → `List<Transaction>`.
- **Trả về:** `List<Transaction>`.  
- **Ghi chú:** Controller dùng phiên bản DTO là `findTransactionResponsesByStatus`.

---

### 2.8. `findTransactionResponsesByStatus(String status)`

- **Tham số:** `status` – trạng thái cần lọc.
- **Chức năng:** Lấy danh sách giao dịch theo status và trả DTO cho API.
- **Luồng:**
  1. `transactionRepository.findAllByStatus(status)` → `List<Transaction>`.
  2. Stream: `map(transactionMapper::toTransactionResponse)` → `List<TransactionResponse>`.
- **Trả về:** `List<TransactionResponse>`.

---

## 3. Bảng tương ứng Controller ↔ Service

| Controller method | Service method | Mô tả ngắn |
|-------------------|----------------|------------|
| createTransaction(request) | createTransaction(request) | Tạo giao dịch mới |
| updateTransaction(id, request) | updateTransaction(id, request) | Cập nhật giao dịch |
| getAllTransactions() | findAllTransactionResponses() | Lấy tất cả (DTO) |
| getTransactionById(id) | findTransactionById(id) | Lấy một theo ID (DTO) |
| deleteTransaction(id) | deleteTransaction(id) | Xóa giao dịch |
| getTransactionsByStatus(status) | findTransactionResponsesByStatus(status) | Lấy danh sách theo status (DTO) |

Service còn có **findAllTransactions()** và **findTransactionsByStatus()** trả entity – dùng nội bộ hoặc cho service khác, không được gọi trực tiếp từ Controller hiện tại.

# Luồng đi chi tiết của Transaction (class → class)

Tài liệu mô tả **luồng đi cụ thể** từ HTTP request đến response, **từ class nào gọi sang class nào**, và **main flow** của tính năng Transaction.

---

## 1. Tổng quan Main Flow

```
HTTP Request (POST/GET/PUT/DELETE /api/transactions...)
    │
    ▼
[Spring] DispatcherServlet
    │
    ▼
[Spring Security] JWT Filter → kiểm tra token, quyền (isAuthenticated / ADMIN)
    │
    ▼
TransactionController (method tương ứng: createTransaction / updateTransaction / ...)
    │
    ├── Nhận: TransactionCreationRequest hoặc TransactionUpdateRequest (từ Body)
    ├── Gọi: TransactionService.createTransaction(request) hoặc updateTransaction(...) hoặc find...
    │
    ▼
TransactionService
    │
    ├── Gọi: IEventRepository, IBikeListingRepository, IUserRepository, IDepositRepository, IReservationRepository (load entity theo ID)
    ├── Gọi: ITransactionRepository (findByDeposit_DepositId / findById / findAll / save / delete)
    ├── Tạo/sửa: Entity Transaction (entities/Transaction.java)
    ├── Gọi: TransactionMapper.toTransactionResponse(transaction)
    │
    ▼
TransactionMapper → tạo TransactionResponse từ Transaction
    │
    ▼
TransactionService return TransactionResponse (hoặc List<TransactionResponse> / String)
    │
    ▼
TransactionController bọc vào ApiResponse<T> → return JSON
```

---

## 2. Luồng theo từng API (class → class cụ thể)

### 2.1. POST /api/transactions – Tạo giao dịch (Create)

| Bước | Class | Method / hành động | Gọi tới class nào |
|------|--------|---------------------|-------------------|
| 1 | **DispatcherServlet** | Nhận POST `/api/transactions`, map body JSON | → |
| 2 | **Security Filter Chain** | Kiểm tra JWT, `@PreAuthorize("isAuthenticated()")` | (cho qua nếu đã login) |
| 3 | **TransactionController** | `createTransaction(@RequestBody @Valid TransactionCreationRequest request)` | Bind body → **TransactionCreationRequest** (DTO) |
| 4 | **TransactionController** | `transactionService.createTransaction(request)` | → **TransactionService** |
| 5 | **TransactionService** | `createTransaction(TransactionCreationRequest request)` | |
| 5a | TransactionService | `request.getEventId()` != null → `eventRepository.findById(...)` | → **IEventRepository** → trả **Events** (entity) |
| 5b | TransactionService | `request.getListingId()` != null → `bikeListingRepository.findById(...)` | → **IBikeListingRepository** → **BikeListing** |
| 5c | TransactionService | `request.getBuyerId()` != null → `userRepository.findById(...)` | → **IUserRepository** → **Users** |
| 5d | TransactionService | `request.getSellerId()` != null → `userRepository.findById(...)` | → **IUserRepository** → **Users** |
| 5e | TransactionService | `request.getDepositId()` != null → `depositRepository.findById(...)` | → **IDepositRepository** → **Deposit** |
| 5f | TransactionService | `transactionRepository.findByDeposit_DepositId(request.getDepositId())` | → **ITransactionRepository** (kiểm tra deposit đã có transaction chưa) |
| 5g | TransactionService | Nếu có transaction trùng → `throw new AppException(ErrorCode.DEPOSIT_ALREADY_HAS_TRANSACTION)` | → **AppException**, **ErrorCode** |
| 5h | TransactionService | `request.getReservationId()` != null → `reservationRepository.findById(...)` | → **IReservationRepository** → **Reservation** |
| 6 | TransactionService | `Transaction.builder()...build()` | Tạo object **Transaction** (entity) |
| 7 | TransactionService | `transactionRepository.save(transaction)` | → **ITransactionRepository** (JPA → DB) |
| 8 | TransactionService | `transactionMapper.toTransactionResponse(transaction)` | → **TransactionMapper** |
| 9 | **TransactionMapper** | `toTransactionResponse(Transaction t)` | Tạo **TransactionResponse**, set từng field từ entity (kể cả id từ buyer, seller, event, listing, deposit, reservation) |
| 10 | TransactionService | `return transactionMapper.toTransactionResponse(transaction)` | Trả **TransactionResponse** |
| 11 | **TransactionController** | `apiResponse.setResult(...); return apiResponse` | Bọc vào **ApiResponse&lt;TransactionResponse&gt;** → JSON response |

**Tóm tắt luồng Create:**  
`TransactionController` → `TransactionService` → (nhiều Repository + Entity) → `ITransactionRepository.save()` → `TransactionMapper.toTransactionResponse()` → `TransactionResponse` → `ApiResponse` → Client.

---

### 2.2. PUT /api/transactions/{transactionId} – Cập nhật (Update)

| Bước | Class | Method / hành động | Gọi tới |
|------|--------|---------------------|---------|
| 1 | **TransactionController** | `updateTransaction(@PathVariable int transactionId, @RequestBody TransactionUpdateRequest request)` | |
| 2 | TransactionController | `transactionService.updateTransaction(transactionId, request)` | → **TransactionService** |
| 3 | **TransactionService** | `updateTransaction(int transactionId, TransactionUpdateRequest request)` | |
| 4 | TransactionService | `transactionRepository.findById(transactionId)` | → **ITransactionRepository** → **Transaction** (entity) hoặc throw **AppException(TRANSACTION_NOT_FOUND)** |
| 5 | TransactionService | Nếu request có eventId → `eventRepository.findById(...)`; set `transaction.setEvent(event)` | → **IEventRepository**, **Events** |
| 6 | TransactionService | Tương tự cho listingId, buyerId, sellerId, depositId, reservationId | → **IBikeListingRepository**, **IUserRepository**, **IDepositRepository**, **IReservationRepository** |
| 7 | TransactionService | set status, amount, actualPrice từ request | Trên entity **Transaction** |
| 8 | TransactionService | `transactionRepository.save(transaction)` | → **ITransactionRepository** |
| 9 | TransactionService | `transactionMapper.toTransactionResponse(transaction)` | → **TransactionMapper** → **TransactionResponse** |
| 10 | TransactionController | `apiResponse.setResult(...); return apiResponse` | **ApiResponse&lt;TransactionResponse&gt;** |

**Luồng Update:** Controller → TransactionService → ITransactionRepository.findById → (các repo khác nếu cập nhật quan hệ) → set fields → save → TransactionMapper → Response → ApiResponse.

---

### 2.3. GET /api/transactions – Lấy tất cả (Get All)

| Bước | Class | Method | Gọi tới |
|------|--------|--------|---------|
| 1 | **TransactionController** | `getAllTransactions()` | |
| 2 | TransactionController | `transactionService.findAllTransactionResponses()` | → **TransactionService** |
| 3 | **TransactionService** | `transactionRepository.findAll()` | → **ITransactionRepository** (JpaRepository) → `List<Transaction>` |
| 4 | TransactionService | `.stream().map(transactionMapper::toTransactionResponse).toList()` | → **TransactionMapper.toTransactionResponse()** từng phần tử → `List<TransactionResponse>` |
| 5 | TransactionController | `apiResponse.setResult(list); return apiResponse` | **ApiResponse&lt;List&lt;TransactionResponse&gt;&gt;** |

**Luồng Get All:** Controller → TransactionService → ITransactionRepository.findAll() → TransactionMapper (từng item) → List<TransactionResponse> → ApiResponse.

---

### 2.4. GET /api/transactions/{transactionId} – Lấy theo ID

| Bước | Class | Method | Gọi tới |
|------|--------|--------|---------|
| 1 | **TransactionController** | `getTransactionById(@PathVariable int transactionId)` | |
| 2 | TransactionController | `transactionService.findTransactionById(transactionId)` | → **TransactionService** |
| 3 | **TransactionService** | `transactionRepository.findById(transactionId).orElseThrow(...)` | → **ITransactionRepository** → **Transaction** hoặc **AppException(TRANSACTION_NOT_FOUND)** |
| 4 | TransactionService | `transactionMapper.toTransactionResponse(transaction)` | → **TransactionMapper** → **TransactionResponse** |
| 5 | TransactionController | `apiResponse.setResult(...); return apiResponse` | **ApiResponse&lt;TransactionResponse&gt;** |

---

### 2.5. GET /api/transactions/status/{status} – Lấy theo trạng thái

| Bước | Class | Method | Gọi tới |
|------|--------|--------|---------|
| 1 | **TransactionController** | `getTransactionsByStatus(@PathVariable String status)` | |
| 2 | TransactionController | `transactionService.findTransactionResponsesByStatus(status)` | → **TransactionService** |
| 3 | **TransactionService** | `transactionRepository.findAllByStatus(status)` | → **ITransactionRepository** (custom method) → `List<Transaction>` |
| 4 | TransactionService | `.stream().map(transactionMapper::toTransactionResponse).toList()` | → **TransactionMapper** → `List<TransactionResponse>` |
| 5 | TransactionController | `apiResponse.setResult(list); return apiResponse` | **ApiResponse&lt;List&lt;TransactionResponse&gt;&gt;** |

---

### 2.6. DELETE /api/transactions/{transactionId} – Xóa

| Bước | Class | Method | Gọi tới |
|------|--------|--------|---------|
| 1 | **TransactionController** | `deleteTransaction(@PathVariable int transactionId)` | |
| 2 | TransactionController | `transactionService.deleteTransaction(transactionId)` | → **TransactionService** |
| 3 | **TransactionService** | `transactionRepository.findById(transactionId).orElseThrow(...)` | → **ITransactionRepository** → **Transaction** hoặc **AppException** |
| 4 | TransactionService | `transactionRepository.delete(transaction)` | → **ITransactionRepository** (JPA delete) |
| 5 | TransactionService | `return "Transaction deleted successfully"` | String |
| 6 | TransactionController | `apiResponse.setResult(string); return apiResponse` | **ApiResponse&lt;String&gt;** |

---

## 3. Sơ đồ phụ thuộc class (Transaction)

```
                    HTTP Request
                         │
                         ▼
              ┌──────────────────────┐
              │ TransactionController │
              │  (controller package) │
              └───────────┬──────────┘
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
  TransactionCreationRequest   TransactionUpdateRequest   ApiResponse
  TransactionResponse (dto)
         │                │                │
         └────────────────┼────────────────┘
                          ▼
              ┌──────────────────────┐
              │  TransactionService   │
              │  (services package)  │
              └───────────┬──────────┘
                          │
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
ITransactionRepository   TransactionMapper   Các repository khác:
    │                     │                  IEventRepository
    │                     │                  IBikeListingRepository
    ▼                     │                  IUserRepository
Transaction (entity) ◄────┘                  IDepositRepository
    │                                        IReservationRepository
    │                     Entities: Events, BikeListing, Users,
    │                                Deposit, Reservation
    ▼
AppException / ErrorCode (khi not found hoặc DEPOSIT_ALREADY_HAS_TRANSACTION)
```

---

## 4. Main Flow tóm tắt (1 dòng cho từng API)

| API | Main flow (class theo thứ tự) |
|-----|------------------------------|
| **POST Create** | Controller → **TransactionCreationRequest** → **TransactionService** → (IEventRepository, IBikeListingRepository, IUserRepository, IDepositRepository, **ITransactionRepository.findByDeposit_DepositId**, IReservationRepository) → build **Transaction** → **ITransactionRepository.save** → **TransactionMapper.toTransactionResponse** → **TransactionResponse** → **ApiResponse** |
| **PUT Update** | Controller → **TransactionUpdateRequest** → **TransactionService** → **ITransactionRepository.findById** → (các repo khác nếu cập nhật FK) → set fields → **ITransactionRepository.save** → **TransactionMapper** → **TransactionResponse** → **ApiResponse** |
| **GET All** | Controller → **TransactionService** → **ITransactionRepository.findAll** → **TransactionMapper** (từng item) → **List&lt;TransactionResponse&gt;** → **ApiResponse** |
| **GET By Id** | Controller → **TransactionService** → **ITransactionRepository.findById** → **TransactionMapper** → **TransactionResponse** → **ApiResponse** |
| **GET By Status** | Controller → **TransactionService** → **ITransactionRepository.findAllByStatus** → **TransactionMapper** → **List&lt;TransactionResponse&gt;** → **ApiResponse** |
| **DELETE** | Controller → **TransactionService** → **ITransactionRepository.findById** → **ITransactionRepository.delete** → String → **ApiResponse** |

---

## 5. Danh sách class tham gia luồng Transaction

| Package | Class | Vai trò trong luồng |
|---------|--------|----------------------|
| controller | **TransactionController** | Entry point: nhận HTTP, gọi Service, bọc ApiResponse |
| services | **TransactionService** | Nghiệp vụ: validate, load entity, build Transaction, gọi Repository + Mapper |
| repository | **ITransactionRepository** | JPA: findById, findAll, findAllByStatus, findByDeposit_DepositId, save, delete |
| entities | **Transaction** | Entity JPA (transaction_id, event, listing, buyer, seller, deposit, reservation, amount, actualPrice, status, createAt, updateAt) |
| dto.request | **TransactionCreationRequest** | Body POST: eventId, listingId, buyerId, sellerId, depositId, reservationId, status, amount, actualPrice |
| dto.request | **TransactionUpdateRequest** | Body PUT: cùng các trường (nullable) |
| dto.response | **TransactionResponse** | Response: transactionId, buyerId, sellerId, eventId, listingId, depositId, reservationId, amount, actualPrice, createdAt, updatedAt, status |
| dto.response | **ApiResponse** | Bọc mọi response: code, message, result |
| mapper | **TransactionMapper** | Entity → TransactionResponse (toTransactionResponse) |
| exception | **AppException**, **ErrorCode** | Khi not found hoặc DEPOSIT_ALREADY_HAS_TRANSACTION |
| repository | IEventRepository, IBikeListingRepository, IUserRepository, IDepositRepository, IReservationRepository | TransactionService dùng để load Events, BikeListing, Users, Deposit, Reservation |
| entities | Events, BikeListing, Users, Deposit, Reservation | Entity được gắn vào Transaction (quan hệ ManyToOne / OneToOne) |

Luồng đi luôn bắt đầu từ **TransactionController**, qua **TransactionService**, rồi tới **ITransactionRepository** và **TransactionMapper**; các repository/entity còn lại phục vụ việc load dữ liệu quan hệ và validate trước khi tạo/cập nhật **Transaction**.

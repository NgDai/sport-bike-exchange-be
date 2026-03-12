# Danh sách file/class liên quan Transaction – push GitHub tránh conflict

Các file và **đoạn code** dưới đây chỉ phục vụ **Transaction** (giao dịch mua bán – entity `Transaction`, API `/api/transactions`).  
Dùng để chọn đúng file/đoạn cần push, tránh conflict với người sửa module khác.

---

## 1. File CHỈ thuộc Transaction (push thoải mái, ít conflict)

| # | Đường dẫn file (từ root `sport-bike-exchange-be`) |
|---|----------------------------------------------------|
| 1 | `src/main/java/com/bicycle/marketplace/entities/Transaction.java` |
| 2 | `src/main/java/com/bicycle/marketplace/controller/TransactionController.java` |
| 3 | `src/main/java/com/bicycle/marketplace/services/TransactionService.java` |
| 4 | `src/main/java/com/bicycle/marketplace/services/TransactionAutoFillService.java` |
| 5 | `src/main/java/com/bicycle/marketplace/repository/ITransactionRepository.java` |
| 6 | `src/main/java/com/bicycle/marketplace/dto/request/TransactionCreationRequest.java` |
| 7 | `src/main/java/com/bicycle/marketplace/dto/request/TransactionUpdateRequest.java` |
| 8 | `src/main/java/com/bicycle/marketplace/dto/response/TransactionResponse.java` |
| 9 | `src/main/java/com/bicycle/marketplace/mapper/TransactionMapper.java` |
| 10 | `src/main/java/com/bicycle/marketplace/config/TransactionTableMigrationRunner.java` |

---

## 2. File DÙNG CHUNG – chỉ MỘT PHẦN liên quan Transaction (dễ conflict)

Khi push, nếu người khác cũng sửa file này thì dễ conflict. Chỉ cần đảm bảo **các đoạn dưới đây** có trong repo.

### 2.1. `exception/ErrorCode.java`

**Đoạn code liên quan Transaction (các enum):**

- `TRANSACTION_NOT_FOUND(1016, "Không tìm thấy giao dịch")`
- `DEPOSIT_ALREADY_HAS_TRANSACTION(1028, ...)`
- `USER_NOT_AUTHORIZED(1029, ...)` *(nếu dùng cho transaction)*
- `INVALID_REQUEST(1030, ...)` *(handler dùng chung)*
- `TRANSACTION_SAVE_FAILED(1031, ...)`
- `RESERVATION_ALREADY_HAS_TRANSACTION(1032, ...)`

→ Nếu conflict: giữ lại đủ 6 enum trên, merge thêm enum của người khác.

---

### 2.2. `exception/GlobalExceptionHandle.java`

**Đoạn code liên quan Transaction:**

- **Import:** `org.springframework.transaction.UnexpectedRollbackException`
- **Method:** `isTransactionSaveError(Throwable t)` (khoảng dòng 34–40)
- **Handler:** `handleDataIntegrityViolation` – đoạn `if (isTransactionSaveError(...))` trả `TRANSACTION_SAVE_FAILED` (khoảng 46–49)
- **Handler:** `handlePersistence` – đoạn `if (isTransactionSaveError(...))` trả `TRANSACTION_SAVE_FAILED` (khoảng 62–64)
- **Handler:** `handleUnexpectedRollback` – toàn bộ method (khoảng 71–84) dùng `TRANSACTION_SAVE_FAILED`

→ Nếu conflict: giữ nguyên 1 method + 3 handler trên, merge phần handler khác (vd. AppException, AccessDenied, MethodArgumentNotValid).

---

## 3. Docs / SQL (nếu muốn push kèm)

| File |
|------|
| `docs/TRANSACTION_TABLE_NULLABLE_FK.sql` |
| `docs/TRANSACTION_ADD_DESCRIPTION_TYPE.sql` |
| `docs/TRANSACTION_FEE_MIGRATION.sql` |
| `docs/TRANSACTION_SAVE_API_TEST.md` |
| `docs/TRANSACTION_FLOW_API_TEST.md` |
| `docs/TRANSACTION_API_CLASSES_AND_TEST.md` |
| `docs/TRANSACTION_API_TEST_GUIDE.md` |
| `docs/TRANSACTION_IMPLEMENTATION_PLAN.md` |
| `docs/TRANSACTION.md` |
| `docs/TRANSACTION_CONTROLLER_AND_SERVICE_FUNCTIONS.md` |
| `docs/TRANSACTION_OVERVIEW_FLOWS_AND_ACTORS.md` |
| `docs/TRANSACTION_FLOW_DETAIL.md` |

---

## 4. Lệnh git – chỉ add file Transaction

```bash
# Chỉ Transaction (10 file riêng + 2 file dùng chung)
git add src/main/java/com/bicycle/marketplace/entities/Transaction.java
git add src/main/java/com/bicycle/marketplace/controller/TransactionController.java
git add src/main/java/com/bicycle/marketplace/services/TransactionService.java
git add src/main/java/com/bicycle/marketplace/services/TransactionAutoFillService.java
git add src/main/java/com/bicycle/marketplace/repository/ITransactionRepository.java
git add src/main/java/com/bicycle/marketplace/dto/request/TransactionCreationRequest.java
git add src/main/java/com/bicycle/marketplace/dto/request/TransactionUpdateRequest.java
git add src/main/java/com/bicycle/marketplace/dto/response/TransactionResponse.java
git add src/main/java/com/bicycle/marketplace/mapper/TransactionMapper.java
git add src/main/java/com/bicycle/marketplace/config/TransactionTableMigrationRunner.java
git add src/main/java/com/bicycle/marketplace/exception/ErrorCode.java
git add src/main/java/com/bicycle/marketplace/exception/GlobalExceptionHandle.java

git status
git commit -m "feat: Transaction API, auto-fill, migration, exception handling"
git push origin <tên-nhánh>
```

Thêm docs (tùy chọn):

```bash
git add docs/TRANSACTION_TABLE_NULLABLE_FK.sql
git add docs/TRANSACTION_SAVE_API_TEST.md
git add docs/TRANSACTION_FLOW_API_TEST.md
# ... các file docs/TRANSACTION_*.md, *.sql khác nếu cần
```

---

## 5. File KHÔNG thuộc module Transaction (đừng nhầm)

- **WalletTransaction*, IWalletTransactionRepository, WalletTransactionService, WalletTransactionMapper, WalletTransaction.java, WalletTransactionResponse** → module **Ví (Wallet)**, không phải giao dịch mua bán.
- **Event, BikeListing, Users, Deposit, Reservation** → module chung, Transaction chỉ phụ thuộc; không cần list riêng khi push “chỉ Transaction”.
- **ApiResponse, AppException** → dùng chung toàn project.

---

**Tóm tắt:** Push **10 file chỉ Transaction** + **2 file exception** (ErrorCode, GlobalExceptionHandle). Nếu conflict ở 2 file exception, chỉ cần giữ đúng các **class/đoạn code** liệt kê ở mục 2.

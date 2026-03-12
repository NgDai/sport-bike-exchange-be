# Kế hoạch commit – Sửa validation (Username + Yêu cầu giao dịch)

Tài liệu này **chỉ phân tích và chia nhỏ commit** để tăng số lượng commit trên GitHub. **Chưa thực hiện sửa code.**

---

## 1. Hiện trạng

### 1.1 Username (đăng ký / đăng nhập)

| Vị trí | Hiện tại | Yêu cầu mới (từ ảnh) |
|--------|----------|----------------------|
| **UserCreationRequest.java** | `@Size(min=3, max=50)`, `@Pattern` bắt buộc **cả chữ và số** | **[6–15 chữ cái alphabet]** (bắt buộc) **+ số optional phía sau** (nếu có). Không trùng. |
| **ErrorCode.java** | `USERNAME_INVALID_LENGTH`: "3 đến 50", `USERNAME_INVALID_FORMAT`: "phải có cả chữ và số..." | Message: 6–15 chữ cái, có thể thêm số ở cuối. |
| **UserService** | Đã có `existsByUsername()` → báo trùng | Giữ nguyên, có thể bổ sung doc/test |

### 1.2 Yêu cầu giao dịch (hồ sơ cá nhân)

| Vị trí | Hiện tại | Yêu cầu mới |
|--------|----------|-------------|
| **TransactionCreationRequest** | Không có validation | Thiếu 1 trường → báo lỗi khi nhấn "Gửi yêu cầu giao dịch" |
| **TransactionUpdateRequest** | Không có validation | Có thể bổ sung validation tương tự (tùy product) |
| **ErrorCode** | Chỉ có `TRANSACTION_NOT_FOUND` | Cần thêm mã lỗi cho từng trường thiếu/sai |

---

## 2. Chia nhỏ commit (để tăng số commit trên GitHub)

### Nhóm A – Validation username

| # | Commit (gợi ý message) | File cần sửa | Nội dung thay đổi (khi làm) |
|---|-------------------------|--------------|-----------------------------|
| **A1** | `fix(validation): update ErrorCode messages for username 6-15 letters + optional digits` | `ErrorCode.java` | Đổi message `USERNAME_INVALID_LENGTH` thành "Tên đăng nhập phải có từ 6 đến 15 chữ cái, có thể thêm số ở cuối"; đổi `USERNAME_INVALID_FORMAT` thành "Tên đăng nhập: 6-15 chữ cái (a-z, A-Z), số optional phía sau". |
| **A2** | `fix(validation): username length 6-15 letters in UserCreationRequest` | `UserCreationRequest.java` | Đổi `@Size(min = 3, max = 50)` thành `@Size(min = 6, max = 50)` (tối thiểu 6; max 50 để chừa phần số phía sau). |
| **A3** | `fix(validation): username pattern 6-15 alphabet letters + optional digits at end` | `UserCreationRequest.java` | Đổi `@Pattern` regex thành: **`^[a-zA-Z]{6,15}[0-9]*$`** — 6–15 chữ cái (bắt buộc), sau đó có thể có số ở cuối (optional). |
| **A4** | `docs: update register API doc for username 6-15 letters + optional digits` | `docs/POSTMAN_REGISTER_API.md` (và file docs khác có nhắc username) | Cập nhật rule: username = 6–15 chữ cái alphabet + số optional phía sau; không trùng. |
| **A5** | `docs: clarify duplicate username check in UserService` | `UserService.java` hoặc `docs/` | Thêm comment hoặc đoạn doc ngắn nêu rõ kiểm tra trùng username qua `existsByUsername()` (không đổi logic). |

### Nhóm B – Validation yêu cầu giao dịch (gửi yêu cầu giao dịch – thiếu trường thì báo)

| # | Commit (gợi ý message) | File cần sửa | Nội dung thay đổi (khi làm) |
|---|-------------------------|--------------|-----------------------------|
| **B1** | `feat(validation): add ErrorCode for transaction request validation` | `ErrorCode.java` | Thêm enum cho từng trường: ví dụ `TRANSACTION_STATUS_REQUIRED`, `TRANSACTION_AMOUNT_REQUIRED`, `TRANSACTION_ACTUAL_PRICE_REQUIRED` (và message tiếng Việt tương ứng). |
| **B2** | `feat(validation): add validation to TransactionCreationRequest for required fields` | `TransactionCreationRequest.java` | Thêm `@NotBlank`/`@NotNull`, `@Min`/`@DecimalMin` cho `status`, `amount`, `actualPrice` (và trường bắt buộc khác theo entity/API). Khi thiếu hoặc sai → Spring trả 400 và message từ ErrorCode/validation. |
| **B3** | `feat(validation): add validation to TransactionUpdateRequest for required fields` | `TransactionUpdateRequest.java` | Áp dụng validation tương tự (bắt buộc/format) cho update, đảm bảo "thiếu 1 trường thì báo" khi gửi yêu cầu cập nhật. |
| **B4** | `docs: document transaction request validation and error codes` | `docs/` (file mới hoặc có sẵn) | Ghi rõ: khi gửi yêu cầu tạo/cập nhật giao dịch, thiếu trường nào sẽ trả mã lỗi và message gì (phục vụ frontend/hồ sơ cá nhân). |

---

## 3. Thứ tự commit gợi ý

- **Username:** A1 → A2 → A3 → A4 → A5 (có thể gộp A2+A3 vào 1 commit nếu muốn ít commit hơn).
- **Transaction:** B1 → B2 → B3 → B4.

Tổng tối đa **9 commit** (5 username + 4 transaction). Có thể giảm còn 6–7 bằng cách gộp (ví dụ A2+A3, B2+B3).

---

## 4. Spec username (đã xác nhận)

- **Format:** **[6–15 chữ cái alphabet] + [số optional phía sau nếu có]**
- **Regex:** `^[a-zA-Z]{6,15}[0-9]*$`
- **Ví dụ hợp lệ:** `abcdef`, `username`, `abcdefghijklmno`, `username12`, `abcd1234`
- **Ví dụ không hợp lệ:** `abc` (ít hơn 6 chữ), `user` (ít hơn 6 chữ), `123abc` (số đứng trước), `user_name` (có gạch dưới)

## 5. Lưu ý khi implement

- **Transaction:** Entity `Transaction` có thêm `event`, `listing`, `buyer`, `seller`, `deposit`, `reservation`. Nếu "yêu cầu giao dịch" từ hồ sơ cá nhân gửi thêm các trường này, cần thêm validation (và ErrorCode) cho từng trường bắt buộc tương ứng.
- **Supabase:** Validation username ở backend chỉ cần đúng format trên và kiểm tra không trùng.

---

## 6. File tham chiếu nhanh

- Username: `UserCreationRequest.java`, `ErrorCode.java`, `UserService.java`, `docs/POSTMAN_REGISTER_API.md`
- Transaction: `TransactionCreationRequest.java`, `TransactionUpdateRequest.java`, `ErrorCode.java`, `TransactionController.java`, `TransactionService.java`

Sau khi đồng ý kế hoạch, có thể bắt đầu implement theo từng commit như trên.

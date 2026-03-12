# Flow khi đã hoàn thành BikeListing

Tài liệu mô tả luồng từ lúc tạo bài đăng (BikeListing) đến khi bài đăng **hoàn thành** (completed), dựa trên các entity và API hiện có.

---

## 1. Tổng quan luồng (Flow tổng)

```
[Seller tạo bài đăng] → pending
       ↓
[Nhập thông tin xe (tùy chọn)] → PUT /bike-listings/{id}/bicycle
       ↓
[Đăng ký sự kiện (EventBicycle)] → bài đăng tham gia event
       ↓
[Buyer đặt chỗ] → Reservation (listing_id)
       ↓
[Buyer nộp cọc] → Deposit (listing_id)
       ↓
[Giao dịch hoàn tất] → Transaction (listing, buyer, seller, deposit, reservation)
       ↓
[Cập nhật status bài đăng] → "completed"
```

---

## 2. Chi tiết từng bước

### Bước 1: Tạo bài đăng (BikeListing) – status thường là `pending`

| API | Mô tả |
|-----|--------|
| `POST /bike-listings` | Tạo bài đăng. Body: `PostingCreationRequest` (sellerId, eventId, title, price, status, **bicycle** optional). |

- **Status** khi tạo: thường gửi `"pending"`.
- Có thể nhập luôn thông tin xe trong body (field `bicycle` với `brandId`, `categoryId` từ GET /brands, GET /categories).

### Bước 2: Nhập thông tin xe đạp (chỉ khi status = `pending`)

| API | Mô tả |
|-----|--------|
| `PUT /bike-listings/{listingId}/bicycle` | Thêm/cập nhật thông tin xe (Bicycle) cho bài đăng. Chỉ cho phép khi listing.status = `"pending"`. |

- Dữ liệu xe lưu ở bảng **Bicycle**, liên kết **Brand**, **Category**.
- BikeListing lưu `bicycle_id` (OneToOne với Bicycle).

### Bước 3: Bài đăng tham gia sự kiện (EventBicycle)

| API | Mô tả |
|-----|--------|
| `POST /event-bicycles` | Đăng ký bài đăng vào event. Body: eventId, listingId, seller info. |

- Entity **EventBicycle** nối **Events** + **BikeListing** + **Users** (seller).

### Bước 4: Buyer đặt chỗ (Reservation)

| API | Mô tả |
|-----|--------|
| `POST /reservations` | Tạo đặt chỗ. Body: **ReservationCreationRequest** (status, reservedAt). |

- Entity **Reservation**: `listing_id` → BikeListing, `buyer_id` → Users.
- *Lưu ý:* Request hiện tại chưa có `listingId`, `buyerId`; cần truyền qua body hoặc bổ sung DTO nếu backend map từ request.

### Bước 5: Buyer nộp cọc (Deposit)

| API | Mô tả |
|-----|--------|
| `POST /deposits` | Tạo phiếu cọc. Body: **DepositCreationRequest** (type, amount, status, createAt). |

- Entity **Deposit**: `listing_id` → BikeListing, `user_id` → Users (buyer).
- *Lưu ý:* Request hiện tại chưa có `listingId`, `userId`; cần bổ sung nếu muốn gắn đúng listing.

### Bước 6: Giao dịch hoàn tất (Transaction)

| API | Mô tả |
|-----|--------|
| `POST /transactions` | Tạo giao dịch. Body: **TransactionCreationRequest** (status, amount, actualPrice, …). |
| `PUT /transactions/{transactionId}` | Cập nhật transaction (ví dụ status = completed). |

- Entity **Transaction**: `listing_id`, `buyer_id`, `seller_id`, `event_id`, `deposit_id`, `reservation_id`.
- Khi giao dịch hoàn tất (status completed), coi như deal đã xong.

### Bước 7: Đánh dấu bài đăng hoàn thành (BikeListing status = completed)

| API | Mô tả |
|-----|--------|
| `PUT /bike-listings/{listingId}` | Cập nhật bài đăng. Body: **PostingUpdateRequest** với `"status": "completed"`. |

- Backend hiện **không tự** đổi status BikeListing khi Transaction completed; cần gọi API cập nhật listing sau khi giao dịch hoàn tất (hoặc bổ sung logic trong TransactionService nếu muốn tự động).

---

## 3. Quan hệ bảng (tóm tắt)

| Bảng | Liên quan với BikeListing |
|------|----------------------------|
| **BikeListing** | Bài đăng; có `bicycle_id` (optional), `seller_id`, `event_id`. |
| **Bicycle** | Thông tin xe; có `brand_id`, `category_id`. |
| **Brand** | Thương hiệu xe. |
| **Category** | Danh mục xe. |
| **Reservation** | `listing_id` → BikeListing, `buyer_id` → Users. |
| **Deposit** | `listing_id` → BikeListing, `user_id` → Users. |
| **Transaction** | `listing_id` → BikeListing, + buyer, seller, event, deposit, reservation. |
| **EventBicycle** | `listing_id` → BikeListing, `event_id`, seller. |
| **Wishlist** | `listing_id` → BikeListing (user yêu thích). |

---

## 4. Trạng thái (status) gợi ý cho BikeListing

| Status | Ý nghĩa |
|--------|--------|
| `pending` | Mới tạo / chờ duyệt / chưa đủ thông tin (có thể nhập xe qua PUT .../bicycle). |
| `approved` / `active` | Đã duyệt, đang mở bán (tùy nghiệp vụ). |
| `reserved` | Đã có người đặt chỗ (Reservation). |
| `completed` | Đã bán xong (Transaction hoàn tất). |
| `cancelled` | Hủy. |

*(Tên status có thể thay theo quy ước dự án.)*

---

## 5. Flow tóm tắt khi “đã hoàn thành”

1. **Seller**: Tạo BikeListing (pending) → (tùy chọn) nhập xe → đăng ký event.
2. **Buyer**: Đặt chỗ (Reservation) → nộp cọc (Deposit).
3. **Hệ thống / Seller**: Tạo Transaction (gắn listing, buyer, seller, deposit, reservation).
4. **Khi giao dịch xong**: Gọi `PUT /bike-listings/{listingId}` với `status: "completed"` để đánh dấu bài đăng đã hoàn thành.

Sau khi status = `"completed"`, bài đăng không nên cho đặt chỗ/nộp cọc nữa; có thể thêm validation ở Reservation/Deposit/Transaction (chỉ chấp nhận listing chưa completed).

# Phân tích luồng: Đăng bài trước (BikeListing) → Sau đó mới đăng thông tin xe đạp (Bicycle)

Tài liệu phân tích lại theo đúng nghiệp vụ: **đăng bài = tạo BikeListing trước**, BikeListing chỉ chứa đúng những gì có trong bảng Database; **thông tin xe đạp được đăng/ghi sau**, tức là Bicycle được tạo và gắn vào listing ở bước tiếp theo.

---

## 1. Luồng nghiệp vụ (business flow)

### Bước 1 – Đăng bài (post listing)

- User **đăng bài** → hệ thống tạo một bản ghi trong bảng **`bike_listing`**.
- Ở bước này **chỉ** tồn tại dữ liệu thuộc bảng `bike_listing` trong Database:
  - Các cột mô tả bài đăng: title, brand, model, category, frame_size, wheel_size, manufacture_year, brake_type, transmission, weight, image_url, description, price, status, created_at.
  - Cột **seller_id** (bắt buộc): ai đăng.
  - Cột **event_id** (tùy chọn): bài đăng thuộc event nào.
  - Cột **bicycle_id** (tùy chọn): ở bước đăng bài **chưa có xe đạp chi tiết** → trong DB cột này là **NULL**.
- Kết luận: **BikeListing phải có đầu tiên** và **chỉ bao gồm các cột có trong bảng bike_listing**; tại thời điểm này `bicycle_id` = NULL.

### Bước 2 – Đăng thông tin xe đạp (sau)

- User (hoặc luồng khác) **nhập/đăng thông tin xe đạp** cho bài đăng vừa tạo.
- Hệ thống:
  - Tạo (hoặc cập nhật) bản ghi trong bảng **`bicycle`**.
  - Cập nhật bản ghi **`bike_listing`** đã có: gán **`bicycle_id`** = id của Bicycle vừa tạo.
- Như vậy: **Bicycle và việc gắn vào listing xuất hiện sau**, không bắt buộc ngay khi đăng bài.

---

## 2. Entity BikeListing – “chỉ có những gì trong Database”

Bảng **`bike_listing`** trong Database có đúng **19 cột** (đã liệt kê trong BIKELISTING_DATABASE_ANALYSIS.md / BIKELISTING_SUPABASE_ENTITY_ANALYSIS.md). Entity **BikeListing** nên chỉ phản ánh đúng 19 cột đó:

- **16 cột scalar:** listing_id, brake_type, brand, category, created_at, description, frame_size, image_url, manufacture_year, model, price, status, title, transmission, weight, wheel_size.
- **3 cột FK:** seller_id, event_id, bicycle_id.

Trong JPA, 3 cột FK có thể map bằng:
- **Quan hệ:** `Users seller` (seller_id), `Events event` (event_id), `Bicycle bicycle` (bicycle_id).
- **Hoặc** chỉ kiểu **Integer:** sellerId, eventId, bicycleId.

Cả hai cách đều “chỉ có dữ liệu như Database”; khác nhau ở cách biểu diễn (object vs ID). Điều quan trọng theo luồng là:

- Ở **bước đăng bài**: BikeListing được tạo với đủ 19 cột, trong đó **bicycle_id = null** (chưa có Bicycle).
- Entity **không** nên có field nào không tồn tại trong bảng `bike_listing` (ví dụ không thêm sellerName, eventName trong entity; nếu cần thì đưa vào DTO).

**Kết luận entity:**  
BikeListing chỉ nên có các field tương ứng 19 cột của bảng `bike_listing`. Quan hệ `Bicycle bicycle` (optional, nullable) là cách map cột `bicycle_id`; đúng với luồng “đăng bài trước, xe đạp sau” vì lúc tạo listing có thể để bicycle = null.

---

## 3. So sánh với code hiện tại

### 3.1 Entity BikeListing

- Đã có đủ các field tương ứng các cột bảng (listingId, seller, event, title, brand, …, bicycle).
- **bicycle** là optional (`nullable = true`, `optional = true`) → phù hợp luồng “chưa có xe đạp khi đăng bài”.
- Thiếu: `@Table(name = "bike_listing")` và `@Column(name = "...")` cho các cột snake_case để map tường minh với DB (đã nêu trong BIKELISTING_SUPABASE_ENTITY_ANALYSIS.md).

→ Entity về mặt luồng là đúng (BikeListing trước, Bicycle sau, bicycle nullable); chỉ cần chuẩn hóa @Table/@Column cho đúng Database.

### 3.2 Service – createBikeListing (đăng bài)

- Hiện tại:
  - Tạo BikeListing, set seller, created_at, title, brand, model, category, frame_size, wheel_size, manufacture_year, brake_type, transmission, weight, image_url, description, price, status.
  - **Không** set event (dù request có eventId) → cần bổ sung.
  - **Nếu** `request.getBicycle() != null` thì tạo Bicycle và set vào listing ngay trong cùng request.

- Theo luồng “đăng bài trước, thông tin xe đạp sau”:
  - Ở bước **chỉ đăng bài**, BikeListing nên được tạo **chỉ với dữ liệu thuộc bảng bike_listing**; **bicycle_id** nên để **null** (không tạo Bicycle trong bước này).
  - Có hai cách thiết kế API:
    - **Cách A (luồng tách rõ):** Ở API tạo bài đăng **không** nhận/nhập thông tin xe đạp; luôn tạo BikeListing với bicycle_id = null. Thông tin xe đạp chỉ được gửi ở bước sau (ví dụ PUT `/bike-listings/{id}/bicycle`). Khi đó trong `createBikeListing` **bỏ** hoặc **không xử lý** `request.getBicycle()`.
    - **Cách B (linh hoạt):** Vẫn cho phép gửi kèm thông tin xe đạp khi đăng bài (như hiện tại). Nếu có thì tạo Bicycle và set bicycle_id; nếu không có thì bicycle_id = null. Luồng “đăng bài trước, xe đạp sau” là trường hợp không gửi bicycle.

- Dù chọn A hay B, **BikeListing vẫn phải có trước** (được save vào DB với 19 cột, bicycle_id null hoặc có nếu đã gửi). Điều cần sửa thêm: set **event** từ `request.getEventId()` khi có.

### 3.3 Service – addBicycleToListing (đăng thông tin xe đạp sau)

- Đây đúng là bước **sau** khi đã có BikeListing: tạo Bicycle, gán vào listing (set bicycle_id).
- Code hiện tại: chỉ cho phép khi status = "pending", tạo Bicycle từ BicycleInfoRequest, set listing.setBicycle(bicycle), save listing. Phù hợp luồng “đăng thông tin xe đạp sau”.

---

## 4. Tóm tắt xử lý theo luồng

| Giai đoạn | Nội dung | Database | Entity BikeListing | Ghi chú |
|-----------|----------|----------|---------------------|--------|
| **1. Đăng bài** | Tạo bài đăng | Chỉ ghi vào bảng `bike_listing` (19 cột). seller_id có giá trị, event_id có thể có, **bicycle_id = NULL**. | BikeListing được tạo với đủ field tương ứng 19 cột; bicycle = null. | BikeListing “chỉ có những gì trong Database” (bảng bike_listing). |
| **2. Đăng thông tin xe đạp** | Nhập/xác nhận thông tin xe đạp | Tạo/cập nhật bảng `bicycle`; cập nhật `bike_listing.bicycle_id`. | Gọi addBicycleToListing: tạo Bicycle, set listing.setBicycle(bicycle), save. | Bicycle xuất hiện sau, gắn vào listing đã có. |

**Entity:**  
BikeListing chỉ nên map đúng 19 cột của `bike_listing`; không thêm field không có trong bảng. Quan hệ optional với Bicycle (bicycle_id nullable) phản ánh đúng DB và đúng luồng “có listing trước, có bicycle sau”.

**API/Service:**  
- Bước tạo bài đăng: chỉ cần dữ liệu thuộc bike_listing (seller_id, event_id nếu có, các trường mô tả bài đăng). Có thể không nhận bicycle trong request (luồng tách rõ) hoặc nhận tùy chọn (luồng linh hoạt).  
- Bước sau: API riêng (ví dụ PUT `/bike-listings/{id}/bicycle`) để “đăng thông tin xe đạp” và set bicycle_id.

**Cần sửa thêm (đã nêu ở tài liệu khác):**  
- Entity: `@Table(name = "bike_listing")`, `@Column(name = "...")` cho các cột snake_case.  
- Service create/update: set **event** từ `request.getEventId()` khi có.

Như vậy luồng “đăng bài trước (BikeListing chỉ có dữ liệu trong Database), rồi mới đăng thông tin xe đạp (Bicycle sau)” được đảm bảo cả về schema lẫn nghiệp vụ.

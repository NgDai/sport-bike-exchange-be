# Phân tích: BikeListing chỉ có các field giống như Database

Tài liệu phân tích **cách xử lý** nếu muốn class `BikeListing` chỉ phản ánh đúng cấu trúc bảng `bike_listing` trong Database (không thừa, không thiếu). **Chưa sửa code.**

---

## 1. Bảng `bike_listing` trong Database có gì?

Trong DB, bảng `bike_listing` có **đúng 19 cột**:

| # | Cột DB       | Kiểu DB   | Ghi chú      |
|---|--------------|-----------|--------------|
| 1 | listing_id   | integer   | PK           |
| 2 | brake_type  | varchar   |              |
| 3 | brand       | varchar   |              |
| 4 | category    | varchar   |              |
| 5 | created_at  | timestamp |              |
| 6 | description | varchar   |              |
| 7 | frame_size  | varchar   |              |
| 8 | image_url   | varchar   |              |
| 9 | manufacture_year | integer |        |
|10 | model       | varchar   |              |
|11 | price       | double    |              |
|12 | status      | varchar   |              |
|13 | title       | varchar   |              |
|14 | transmission| varchar   |              |
|15 | weight      | double    |              |
|16 | wheel_size  | varchar   |              |
|17 | event_id    | integer   | FK → events  |
|18 | seller_id   | integer   | FK → users   |
|19 | bicycle_id  | integer   | FK → bicycle (UNIQUE) |

Trong Database **không có** kiểu “entity” hay “object”: chỉ có kiểu scalar (integer, varchar, double, timestamp) và các cột FK (integer).  
Vì vậy “BikeListing chỉ có các entities giống như phần Database” có thể hiểu theo **hai hướng** dưới đây.

---

## 2. Hai hướng xử lý

### Cách hiểu 1: “Giống Database” = **đúng cột, đúng kiểu** (vẫn dùng JPA relationship cho FK)

- **Ý nghĩa:** Class `BikeListing` chỉ có các field tương ứng **đúng 19 cột** trong DB; không thêm field nào không tồn tại trong bảng.
- **FK trong DB** (event_id, seller_id, bicycle_id) vẫn map sang Java bằng **quan hệ JPA** (`Users seller`, `Events event`, `Bicycle bicycle`) vì đây là cách chuẩn của JPA: một cột FK = một association (ManyToOne/OneToOne).
- **Kết quả:** Entity vẫn có 3 “entity” (seller, event, bicycle), nhưng về **cấu trúc bảng** thì vẫn 1–1 với DB: 19 cột ↔ 19 phần dữ liệu (16 scalar + 3 FK qua relationship).

**Cần xử lý:**

- Khai báo tường minh tên bảng và tên cột để **không phụ thuộc** naming strategy:
  - `@Table(name = "bike_listing")`
  - Với mỗi field có tên Java khác tên cột DB → `@Column(name = "tên_cột_db")`:
    - listingId → `listing_id`
    - frameSize → `frame_size`
    - wheelSize → `wheel_size`
    - manufactureYear → `manufacture_year`
    - brakeType → `brake_type`
    - imageUrl → `image_url`
    - createdAt → `created_at`
  - Các field trùng tên (title, brand, model, category, description, price, status, transmission, weight) có thể không cần `@Column` hoặc vẫn thêm cho đồng bộ với DB.
- **Không** thêm field nào không có trong bảng (ví dụ không thêm `sellerName`, `eventName` trong entity; nếu cần thì đưa vào DTO/response).

→ **Kết luận Cách 1:** Giữ nguyên `Users seller`, `Events event`, `Bicycle bicycle`; chỉ chuẩn hóa `@Table` + `@Column` để class “chỉ có các phần dữ liệu giống DB” và map đúng từng cột.

---

### Cách hiểu 2: “Giống Database” = **trong Java cũng chỉ có ID, không có object** (không dùng JPA relationship)

- **Ý nghĩa:** Trong DB chỉ có `event_id`, `seller_id`, `bicycle_id` (integer); vì vậy trong entity cũng **chỉ** lưu `Integer eventId`, `Integer sellerId`, `Integer bicycleId`, **không** có field kiểu `Users`, `Events`, `Bicycle`.
- **Kết quả:** Class `BikeListing` giống DB về **kiểu dữ liệu**: toàn bộ là scalar + 3 Integer FK, không có reference tới entity khác.

**Cần xử lý:**

1. **Entity BikeListing**
   - Xóa:
     - `private Users seller;` và `@ManyToOne` / `@JoinColumn(name = "seller_id")`
     - `private Events event;` và `@ManyToOne` / `@JoinColumn(name = "event_id")`
     - `private Bicycle bicycle;` và `@OneToOne` / `@JoinColumn(name = "bicycle_id")`
   - Thêm (hoặc thay bằng):
     - `private Integer sellerId;` với `@Column(name = "seller_id")`
     - `private Integer eventId;` với `@Column(name = "event_id")`
     - `private Integer bicycleId;` với `@Column(name = "bicycle_id")`
   - Giữ đúng 16 field còn lại (listingId, title, brand, model, …) và thêm `@Table(name = "bike_listing")` + `@Column(name = "...")` cho các cột snake_case như Cách 1.

2. **Service (BikeListingService)**
   - **createBikeListing:** Thay vì `listing.setSeller(seller)` → `listing.setSellerId(request.getSellerId())`. Tương tự `setEventId(request.getEventId())`. Nếu tạo Bicycle thì vẫn tạo và lưu, sau đó `listing.setBicycleId(bicycle.getBikeId())`.
   - **updateBikeListing:** Set `listing.setSellerId(...)`, `listing.setEventId(...)` thay vì set entity. Không set `listing.setBicycle(...)`; nếu có cập nhật bicycle_id thì set `listing.setBicycleId(...)`.
   - **getBikeListingById / getAllBikeListings:** Trả về `BikeListing` chỉ có ID. Nếu API cần trả về tên seller, tên event, thông tin bicycle thì **không** lấy từ `listing.getSeller()`/`getEvent()`/`getBicycle()` (vì không còn) mà phải:
     - Hoặc query riêng: `userRepository.findById(listing.getSellerId())`, `eventRepository.findById(listing.getEventId())`, `bicycleRepository.findById(listing.getBicycleId())` rồi lắp vào DTO/response.
     - Hoặc dùng DTO response (ví dụ `BikeListingResponse`) chứa sellerId, eventId, bicycleId và các field thông tin khác; khi cần “expand” thì service tự load User/Events/Bicycle và set vào DTO.

3. **Controller**
   - Có thể vẫn trả về entity `BikeListing` (khi đó response chỉ có ID cho seller/event/bicycle). Hoặc chuyển hẳn sang trả về DTO (ví dụ `BikeListingResponse`) để chủ động đưa thông tin seller/event/bicycle khi cần.

4. **Chỗ khác đang dùng `BikeListing`**
   - Các entity khác (Deposit, Reservation, Transaction, EventBicycle, Wishlist) có FK tới `bike_listing`; chúng thường dùng `BikeListing listing` (reference) hoặc `listing_id`. Nếu BikeListing không còn `seller`/`event`/`bicycle` thì những chỗ code nào đang gọi `listing.getSeller()`, `listing.getEvent()`, `listing.getBicycle()` sẽ **lỗi biên dịch** → phải đổi sang dùng `listing.getSellerId()`, `listing.getEventId()`, `listing.getBicycleId()` và nếu cần thông tin chi tiết thì load từ repository tương ứng.

5. **Validation**
   - Khi set `sellerId`/`eventId`/`bicycleId`, nên kiểm tra tồn tại (userRepository.existsById(sellerId), …) nếu muốn đảm bảo FK hợp lệ; DB sẽ check FK nhưng việc check trước giúp báo lỗi rõ ràng hơn.

→ **Kết luận Cách 2:** Entity chỉ còn scalar + 3 Integer FK; Service và các chỗ dùng BikeListing phải làm việc qua ID và tự load User/Events/Bicycle khi cần.

---

## 3. So sánh nhanh

| Tiêu chí              | Cách 1 (giữ relationship)     | Cách 2 (chỉ ID)              |
|-----------------------|-------------------------------|------------------------------|
| Entity có Users/Events/Bicycle? | Có (map 3 cột FK)        | Không, chỉ Integer sellerId/eventId/bicycleId |
| Khớp “chỉ như DB”     | Có (19 cột, map rõ @Table/@Column) | Có (19 cột, đúng kiểu DB)   |
| Service tạo/cập nhật  | setSeller(seller), setEvent(event), setBicycle(bicycle) | setSellerId(id), setEventId(id), setBicycleId(id) |
| Lấy thông tin seller/event/bicycle | listing.getSeller()…   | Phải query User/Events/Bicycle theo ID |
| Chỗ khác dùng BikeListing | Ít đổi (vẫn getSeller()…)      | Phải đổi sang getSellerId()… và load khi cần |
| JPA join / cascade    | Dùng được                     | Không có relationship nên không dùng trực tiếp |

---

## 4. Đề xuất

- **Nếu mục tiêu là “BikeListing chỉ có các field giống Database” theo nghĩa *cấu trúc bảng* (đúng 19 cột, đúng kiểu, không thừa):**  
  → Dùng **Cách 1**: giữ relationship, thêm `@Table(name = "bike_listing")` và `@Column(name = "...")` cho từng cột. Ít thay đổi logic, vẫn dùng được JPA và ít rủi ro với code hiện tại.

- **Nếu mục tiêu là “trong Java cũng không có entity con, chỉ ID như trong DB”:**  
  → Dùng **Cách 2**: đổi 3 FK sang `Integer sellerId`, `Integer eventId`, `Integer bicycleId` và cập nhật toàn bộ Service + mọi chỗ dùng `BikeListing` (getSeller/getEvent/getBicycle → getSellerId/getEventId/getBicycleId + load khi cần).

Sau khi chọn một trong hai cách, có thể áp dụng từng bước trong mục 2 tương ứng để sửa code.

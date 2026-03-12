# Phân tích BikeListing & entities so với Database mới

Tài liệu **chỉ phân tích** để sửa lại phần BikeListing cho đúng với schema trong `src/main/Database`. **Chưa sửa code.**

---

## 0. Tại sao nhìn vào Entity và Database trông “khác nhau”?

Khi so sánh trực tiếp class `BikeListing.java` với bảng `bike_listing` trong DB, có vài điểm dễ gây hiểu nhầm. Phần dưới giải thích từng loại “khác nhau” và kết luận có thực sự lệch schema hay không.

### 0.1 Tên: camelCase (Java) vs snake_case (DB)

| Trong Entity (Java) | Trong DB (bike_listing) |
|--------------------|-------------------------|
| listingId          | listing_id              |
| createdAt          | created_at              |
| frameSize          | frame_size              |
| wheelSize          | wheel_size              |
| manufactureYear   | manufacture_year        |
| brakeType          | brake_type              |
| imageUrl           | image_url               |

- **Giải thích:** JPA/Hibernate (với naming strategy mặc định của Spring Boot) tự map camelCase → snake_case. Không cần khai báo từng cột; entity vẫn map đúng bảng `bike_listing`.
- **Kết luận:** Đây là cách đặt tên chuẩn Java vs SQL, **không phải** lệch schema.

### 0.2 Cột `bicycle_id` trong DB – có tương ứng trong Entity không?

- **Trong DB:** Bảng `bike_listing` có cột **`bicycle_id`** (integer, FK tới `bicycle.bike_id`).
- **Trong Entity:** Không có field kiểu `private Integer bicycleId`, nhưng có đoạn:

```java
@OneToOne(fetch = FetchType.LAZY, optional = true)
@JoinColumn(name = "bicycle_id", nullable = true)
private Bicycle bicycle;
```

- **Giải thích:** Trong JPA, quan hệ `@OneToOne` + `@JoinColumn(name = "bicycle_id")` chính là cách map cột **`bicycle_id`** trong DB. Cột đó lưu khóa ngoại; trong Java ta dùng object `bicycle` (Bicycle) thay vì một field `bicycleId`. Khi persist, Hibernate ghi/đọc đúng cột `bicycle_id`.
- **Kết luận:** **Đã có mapping**, không thiếu. “Khác nhau” chỉ là cách biểu diễn: DB là cột FK, Entity là quan hệ object.

### 0.3 `seller_id` / `event_id` – tương tự

- **DB:** Cột `seller_id`, `event_id` (integer FK).
- **Entity:** `private Users seller` với `@JoinColumn(name = "seller_id")`, `private Events event` với `@JoinColumn(name = "event_id")`.
- **Kết luận:** Đúng chuẩn JPA: cột FK trong DB = quan hệ ManyToOne trong entity. **Không lệch.**

### 0.4 Tên bảng: class `BikeListing` vs bảng `bike_listing`

- Entity **không** khai báo `@Table(name = "bike_listing")`. Tên bảng thực tế phụ thuộc naming strategy (thường class `BikeListing` → bảng `bike_listing`).
- Nếu muốn **rõ ràng** và không phụ thuộc cấu hình, có thể thêm `@Table(name = "bike_listing")` (đề xuất trong mục 5).

---

## 1. Bảng `bike_listing` (Database) vs entity `BikeListing`

### 1.1 Cột trong DB

| Cột DB            | Kiểu DB        | Entity BikeListing      | Ghi chú |
|-------------------|----------------|--------------------------|---------|
| listing_id        | integer PK     | listingId (int)         | ✓ |
| seller_id          | integer FK     | seller (Users)          | ✓ |
| event_id           | integer FK     | event (Events)          | ✓ |
| bicycle_id         | integer FK UNIQUE | bicycle (Bicycle)     | ✓ |
| title              | varchar        | title                   | ✓ |
| brand              | varchar        | brand                   | ✓ |
| model              | varchar        | model                   | ✓ |
| category           | varchar        | category                | ✓ |
| frame_size         | varchar        | frameSize               | ✓ (naming strategy) |
| wheel_size         | varchar        | wheelSize               | ✓ |
| manufacture_year   | integer        | manufactureYear         | ✓ |
| brake_type         | varchar        | brakeType               | ✓ |
| transmission       | varchar        | transmission            | ✓ |
| weight             | double         | weight                  | ✓ |
| image_url          | varchar        | imageUrl                | ✓ |
| description        | varchar        | description             | ✓ |
| price              | double         | price                   | ✓ |
| status             | varchar        | status                  | ✓ |
| created_at         | timestamp      | createdAt (LocalDateTime) | ✓ |

**Kết luận cấu trúc:** Entity `BikeListing` khớp cột với bảng `bike_listing`. Nếu không khai báo `@Table`, tên bảng mặc định có thể là `bike_listing` (tùy naming strategy).

### 1.2 Logic nghiệp vụ cần chỉnh

- **event_id:** Bảng có `event_id`, entity có `event`, nhưng trong `BikeListingService`:
  - `createBikeListing(PostingCreationRequest)` **không** gán `listing.setEvent(...)` dù request có `eventId`.
  - `updateBikeListing(..., PostingUpdateRequest)` **không** gán `listing.setEvent(...)` dù request có `eventId`.
- **Hành động đề xuất:** Nếu API/DB mong đợi gán listing vào event khi tạo/cập nhật thì cần set `event` từ `request.getEventId()` (tìm `Events` theo id rồi `listing.setEvent(event)`).

---

## 2. Bảng `bicycle` (Database) vs entity `Bicycle`

### 2.1 Cột trong DB

- `bike_id`, `bike_type`, `brake_type`, `color`, `drivetrain`, `fork_type`, `frame_material`, `frame_size`, `number_of_gears`, `wheel_size`, `year_manufacture`
- `brand_id`, `category_id` (FK)
- Cột trùng/legacy: `drive_train`, `number_of_gear`, `year_manufactured`, `brand_id_brand_id`, `cate_id_cate_id`
- `condition` (integer NOT NULL)

### 2.2 Entity `Bicycle` – lỗi cần sửa

| Vấn đề | Hiện tại trong code | Trong DB | Cần sửa |
|--------|----------------------|----------|---------|
| FK brand  | `@JoinColumn(name = "brandId")` | Cột thực tế là `brand_id` | Đổi thành `name = "brand_id"` |
| FK category | `@JoinColumn(name = "categoryId")` | Cột thực tế là `category_id` | Đổi thành `name = "category_id"` |

Hibernate sẽ tạo/tìm cột đúng tên trong DB. Đặt `brandId`/`categoryId` sẽ không khớp schema hiện tại (snake_case `brand_id`, `category_id`).

### 2.3 Các trường khác

- Các field còn lại (bikeType, wheelSize, numberOfGears, brakeType, yearManufacture, frameSize, drivetrain, forkType, color, frameMaterial, condition) đều có cột tương ứng trong DB; naming strategy map camelCase → snake_case (ví dụ `numberOfGears` → `number_of_gears`).
- **condition:** Entity có `int condition`, DB có `condition integer NOT NULL`. Trong `BikeListingService.buildBicycleFromRequest(BicycleInfoRequest)` **không** set `condition`; BicycleInfoRequest cũng không có field này. Cần quyết định: mặc định (ví dụ 0) hay thêm field vào request và set khi tạo Bicycle.

---

## 3. Bảng `brand` / `category` vs entity Brand / Category

### 3.1 Brand

- DB: `brand_id`, `name`.
- Entity: `brandId`, `name`.
- **Kết luận:** Khớp, không cần đổi cho BikeListing.

### 3.2 Category

- DB: `category_id`, `bicycle_type`, `name`, **`cate_id integer NOT NULL`**.
- Entity: `categoryId`, `name`, `bicycleType`; **không có `cateId`**.
- **Vấn đề:** Cột `cate_id` là NOT NULL trong DB. Nếu entity không map thì khi insert/update Category có thể lỗi hoặc giá trị mặc định (tùy DB).
- **Đề xuất:** Thêm vào entity `Category` field tương ứng (ví dụ `cateId`) và map đúng cột `cate_id`, hoặc xác nhận với DB/admin xem `cate_id` có bắt buộc và ý nghĩa (trùng với category_id hay không) rồi mới bỏ qua.

---

## 4. Entity liên quan khác (Deposit, Reservation, Transaction, EventBicycle, Wishlist)

Các bảng này trong DB đều có FK tới `bike_listing(listing_id)`. Các entity tương ứng đã dùng quan hệ tới `BikeListing`/listing_id thì giữ nguyên; chỉ cần đảm bảo tên bảng và tên cột (qua naming hoặc @Table/@Column) khớp với file Database. Không phát sinh thay đổi đặc thù cho “phần BikeListing” ngoài những mục trên.

---

## 5. Tóm tắt việc cần làm (khi sửa code)

1. **Bicycle.java**
   - Đổi `@JoinColumn(name = "brandId")` → `name = "brand_id"`.
   - Đổi `@JoinColumn(name = "categoryId")` → `name = "category_id"`.
   - Xử lý `condition`: hoặc set mặc định khi build từ `BicycleInfoRequest`, hoặc thêm field vào `BicycleInfoRequest` và set trong `buildBicycleFromRequest`.

2. **BikeListingService**
   - Trong `createBikeListing`: nếu có `request.getEventId()` thì lấy `Events` theo id và gán `listing.setEvent(event)`.
   - Trong `updateBikeListing`: nếu có `request.getEventId()` thì cập nhật `listing.setEvent(event)` tương tự (và xử lý trường hợp clear event nếu API cho phép).

3. **Category.java** (nếu giữ đúng DB)
   - Thêm field map tới cột `cate_id` (ví dụ `private Integer cateId;` với `@Column(name = "cate_id")`) và đảm bảo có giá trị khi tạo/sửa Category (hoặc xác nhận với DB rằng cột này có default/không bắt buộc).

4. **Tùy chọn**
   - Thêm `@Table(name = "bike_listing")` cho entity `BikeListing` để không phụ thuộc naming strategy.
   - Thêm `@Table(name = "bicycle")` cho entity `Bicycle` nếu muốn rõ ràng.

Sau khi chỉnh xong, chạy lại ứng dụng (ddl-auto=update hoặc so sánh với DB thật) để đảm bảo không có thay đổi schema ngoài ý muốn và các API BikeListing hoạt động đúng với DB mới.

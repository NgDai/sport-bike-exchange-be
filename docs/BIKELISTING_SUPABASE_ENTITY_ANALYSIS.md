# Phân tích Entity BikeListing so với schema Supabase (bike_listing)

Tài liệu **chỉ phân tích** trước khi sửa code. So sánh từng cột trong schema Supabase (hình bạn gửi) với entity `BikeListing.java` hiện tại.

---

## 1. Schema Supabase – bảng `bike_listing`

| # | Cột DB       | Kiểu DB   | Ràng buộc   |
|---|--------------|-----------|-------------|
| 1 | listing_id   | int4      | Primary Key |
| 2 | brake_type   | varchar   | —           |
| 3 | brand        | varchar   | —           |
| 4 | category     | varchar   | —           |
| 5 | created_at   | timestamp | —           |
| 6 | description  | varchar   | —           |
| 7 | frame_size   | varchar   | —           |
| 8 | image_url    | varchar   | —           |
| 9 | manufacture_year | int4  | —           |
|10 | model        | varchar   | —           |
|11 | price        | float8    | —           |
|12 | status       | varchar   | —           |
|13 | title        | varchar   | —           |
|14 | transmission | varchar   | —           |
|15 | weight       | float8    | —           |
|16 | wheel_size   | varchar   | —           |
|17 | event_id     | int4      | FK          |
|18 | seller_id    | int4      | FK          |
|19 | bicycle_id   | int4      | FK          |

---

## 2. So sánh từng cột với Entity hiện tại

| Cột Supabase   | Kiểu DB   | Entity hiện tại                    | Kiểu Java     | Ghi chú |
|----------------|-----------|------------------------------------|---------------|---------|
| listing_id     | int4      | listingId                          | int           | ✓ PK, nhưng **thiếu @Table** nên tên bảng có thể không khớp |
| brake_type     | varchar   | brakeType                          | String        | ✓ Map qua naming (camelCase → snake_case) |
| brand          | varchar   | brand                              | String        | ✓ Trùng tên |
| category       | varchar   | category                           | String        | ✓ |
| created_at     | timestamp | createdAt                          | LocalDateTime | ✓ Kiểu phù hợp timestamp |
| description    | varchar   | description                        | String        | ✓ |
| frame_size     | varchar   | frameSize                          | String        | ✓ Map qua naming |
| image_url      | varchar   | imageUrl                           | String        | ✓ Map qua naming |
| manufacture_year | int4    | manufactureYear                    | Integer       | ✓ |
| model          | varchar   | model                              | String        | ✓ |
| price          | float8    | price                              | Double        | ✓ |
| status         | varchar   | status                             | String        | ✓ |
| title          | varchar   | title                              | String        | ✓ |
| transmission   | varchar   | transmission                       | String        | ✓ |
| weight         | float8    | weight                             | Double        | ✓ |
| wheel_size     | varchar   | wheel_size                         | String        | ✓ Map qua naming (wheelSize → wheel_size) |
| event_id       | int4      | event (Events) @JoinColumn("event_id") | ManyToOne  | ✓ Đúng FK |
| seller_id      | int4      | seller (Users) @JoinColumn("seller_id") | ManyToOne | ✓ Đúng FK |
| bicycle_id     | int4      | bicycle (Bicycle) @JoinColumn("bicycle_id") | OneToOne | ✓ Đúng FK |

---

## 3. Các điểm chưa đúng / rủi ro với schema Supabase

### 3.1 Tên bảng không khai báo tường minh

- **Hiện tại:** Entity chỉ có `@Entity`, không có `@Table(name = "bike_listing")`.
- **Rủi ro:** Hibernate dùng tên bảng mặc định (phụ thuộc naming strategy). Với Spring Boot mặc định thường ra `bike_listing`, nhưng không đảm bảo với mọi cấu hình hoặc phiên bản.
- **Cần sửa:** Thêm `@Table(name = "bike_listing")` để entity luôn map đúng bảng Supabase.

### 3.2 Tên cột phụ thuộc naming strategy

- **Hiện tại:** Các field dùng camelCase (frameSize, wheelSize, brakeType, imageUrl, manufactureYear, createdAt). Cột Supabase là snake_case (frame_size, wheel_size, brake_type, image_url, manufacture_year, created_at). Mapping dựa vào naming strategy mặc định (SpringPhysicalNamingStrategy).
- **Rủi ro:** Nếu sau này tắt hoặc đổi naming strategy, mapping có thể sai (entity vẫn dùng camelCase nhưng cột Supabase vẫn là snake_case).
- **Cần sửa (nên làm):** Thêm `@Column(name = "tên_cột_supabase")` cho từng field có tên khác với cột DB, để entity **tường minh** khớp schema Supabase, không phụ thuộc strategy:

| Field entity   | Cột Supabase   | Annotation đề xuất            |
|----------------|----------------|--------------------------------|
| listingId      | listing_id     | @Column(name = "listing_id") (kèm @Id) |
| frameSize      | frame_size     | @Column(name = "frame_size")   |
| wheelSize      | wheel_size     | @Column(name = "wheel_size")    |
| manufactureYear| manufacture_year | @Column(name = "manufacture_year") |
| brakeType      | brake_type     | @Column(name = "brake_type")   |
| imageUrl       | image_url      | @Column(name = "image_url")    |
| createdAt      | created_at     | @Column(name = "created_at")  |

Các field trùng tên (title, brand, model, category, description, price, status, transmission, weight) có thể không cần @Column hoặc vẫn thêm cho đồng bộ: `@Column(name = "title")`, v.v.

### 3.3 Khóa chính (listing_id)

- **Hiện tại:** `@Id @GeneratedValue(strategy = GenerationType.AUTO)` trên `listingId`. Kiểu `int` tương đương int4.
- **Supabase:** Thường dùng SERIAL/IDENTITY cho PK integer. GenerationType.AUTO với PostgreSQL thường chọn IDENTITY → phù hợp.
- **Kết luận:** Có thể giữ nguyên; nếu Supabase dùng sequence cụ thể thì sau này có thể đổi sang `GenerationType.IDENTITY` hoặc cấu hình sequence.

### 3.4 Kiểu dữ liệu

- int4 → `int` / `Integer`: đúng.
- float8 → `Double`: đúng.
- varchar → `String`: đúng.
- timestamp → `LocalDateTime`: phù hợp với `timestamp` (và `timestamp without time zone`) trong PostgreSQL/Supabase.

---

## 4. Tóm tắt hạng mục cần sửa (khi vào code)

| Hạng mục | Mức độ   | Nội dung |
|----------|----------|----------|
| 1        | Bắt buộc | Thêm `@Table(name = "bike_listing")` để map đúng bảng Supabase. |
| 2        | Nên làm  | Thêm `@Column(name = "snake_case")` cho các field có tên camelCase tương ứng cột Supabase: listingId, frameSize, wheelSize, manufactureYear, brakeType, imageUrl, createdAt. |
| 3        | Tùy chọn | Thêm `@Column(name = "...")` cho các field còn lại (title, brand, model, ...) để entity tự mô tả đúng 100% schema Supabase. |

Quan hệ FK (event_id, seller_id, bicycle_id) đã đúng qua `@JoinColumn(name = "event_id")`, `seller_id`, `bicycle_id`; không cần đổi.

---

## 5. Thứ tự thực hiện khi sửa code

1. Thêm `@Table(name = "bike_listing")` trên class `BikeListing`.
2. Thêm `@Column(name = "listing_id")` cho `listingId` (vẫn giữ `@Id`).
3. Thêm lần lượt `@Column(name = "frame_size")`, `"wheel_size"`, `"manufacture_year"`, `"brake_type"`, `"image_url"`, `"created_at"` cho các field tương ứng.
4. (Tùy chọn) Thêm `@Column` cho title, brand, model, category, description, price, status, transmission, weight.

Sau khi sửa, entity sẽ **tường minh** khớp schema Supabase và không phụ thuộc cấu hình naming strategy.

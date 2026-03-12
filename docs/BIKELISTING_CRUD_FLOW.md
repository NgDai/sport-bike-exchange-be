# Flow code CRUD BikeListing (ngắn gọn)

---

## Flow đơn giản nhất của BikeListing

1. **Seller đăng BikeListing**
2. **BikeListing ở trạng thái chờ cập nhật xe** (pending)
3. **Seller bấm cập nhật xe** (nhập thông tin xe đạp)

---

## Giải thích đơn giản: Cách đăng bài

**Đăng bài** = tạo một bài đăng bán xe (BikeListing).

1. **Người dùng gửi** thông tin bài đăng: ai bán (sellerId), sự kiện nào (eventId), tiêu đề, giá, mô tả, v.v.  
   Có thể gửi thêm **thông tin xe** (brandId, categoryId, loại xe, kích thước, …).

2. **Controller** nhận request → gọi **Service** `createBikeListing`.

3. **Service** làm lần lượt:
   - Kiểm tra sellerId, eventId có đúng không (tìm User và Event trong DB).
   - Tạo **BikeListing** và điền đủ thông tin từ request.
   - **Nếu có gửi thông tin xe:**  
     Lấy Brand và Category theo brandId, categoryId → tạo **Bicycle** → lưu Bicycle → gắn Bicycle vào bài đăng.
   - Lưu **BikeListing** vào DB.

4. Trả về bài đăng vừa tạo (đã lưu).

**Tóm lại:** Đăng bài = nhận dữ liệu → kiểm tra seller/event → tạo bài đăng (và nếu có thì tạo luôn xe, gắn vào bài) → lưu DB → trả kết quả.

---

## Create

1. **BikeListingController** `createBikeListing(PostingCreationRequest)`  
   → gọi **BikeListingService** `createBikeListing(request)`.

2. **BikeListingService** `createBikeListing`:
   - Validate: sellerId, eventId bắt buộc; nếu thiếu → `AppException`.
   - **IUserRepository**.findById(sellerId) → Users (hoặc USER_NOT_FOUND).
   - **IEventRepository**.findById(eventId) → Events (hoặc EVENT_NOT_FOUND).
   - Tạo entity **BikeListing**, set các field từ request (title, brand, model, category, price, status, …).
   - Nếu `request.getBicycle() != null`:
     - Gọi **buildBicycleFromRequest**(request.getBicycle()):
       - **IBrandRepository**.findById(brandId) → Brand (hoặc BRAND_NOT_FOUND).
       - **ICategoryRepository**.findById(categoryId) → Category (hoặc CATEGORY_NOT_FOUND).
       - Tạo entity **Bicycle** (builder) gắn brand, category + các field từ BicycleInfoRequest.
     - **IBicycleRepository**.save(bicycle).
     - listing.setBicycle(bicycle).
   - **IBikeListingRepository**.save(listing) → trả về BikeListing.

3. Controller bọc kết quả vào **ApiResponse** và trả về.

---

## Read (danh sách)

1. **BikeListingController** `getAllBikeListings()`  
   → **BikeListingService** `getAllBikeListings()`  
   → **IBikeListingRepository**.findAll()  
   → trả về List&lt;BikeListing&gt; (Controller bọc ApiResponse).

---

## Read (chi tiết)

1. **BikeListingController** `getBikeListingById(listingId)`  
   → **BikeListingService** `getBikeListingById(listingId)`  
   → **IBikeListingRepository**.findById(listingId)  
   → có thì trả BikeListing (có thể kèm bicycle do lazy load khi dùng), không thì LISTING_NOT_FOUND.

---

## Update (sửa bài đăng)

1. **BikeListingController** `updateBikeListing(listingId, PostingUpdateRequest)`  
   → **BikeListingService** `updateBikeListing(listingId, request)`.

2. **BikeListingService** `updateBikeListing`:
   - Gọi **getBikeListingById**(listingId) → lấy BikeListing (hoặc LISTING_NOT_FOUND).
   - Nếu request có sellerId/eventId → load Users/Events từ **IUserRepository** / **IEventRepository** và set lại.
   - Set từng field listing theo request (chỉ set nếu request field != null).
   - **IBikeListingRepository**.save(listing) → trả về BikeListing.

---

## Update (nhập thông tin xe – chỉ khi pending)

1. **BikeListingController** `addBicycleToListing(listingId, BicycleInfoRequest)`  
   → **BikeListingService** `addBicycleToListing(listingId, request)`.

2. **BikeListingService** `addBicycleToListing`:
   - **getBikeListingById**(listingId) → BikeListing.
   - Nếu listing.getStatus() != "pending" → **AppException**(LISTING_NOT_PENDING).
   - **buildBicycleFromRequest**(request) → Bicycle (dùng **IBrandRepository**, **ICategoryRepository** như Create).
   - **IBicycleRepository**.save(bicycle).
   - listing.setBicycle(bicycle).
   - **IBikeListingRepository**.save(listing) → trả về BikeListing.

---

## Delete

1. **BikeListingController** `deleteBikeListing(listingId)`  
   → **BikeListingService** `deleteBikeListing(listingId)`.

2. **BikeListingService** `deleteBikeListing`:
   - **getBikeListingById**(listingId) → BikeListing (hoặc LISTING_NOT_FOUND).
   - **IBikeListingRepository**.delete(listing).

3. Controller trả ApiResponse với message "Bike listing deleted successfully".

---

## Tóm tắt lớp tham gia

| Lớp | Vai trò |
|-----|--------|
| **BikeListingController** | Nhận request, gọi service, trả ApiResponse. |
| **BikeListingService** | Xử lý nghiệp vụ: validate, build Bicycle (buildBicycleFromRequest), gọi repository. |
| **IBikeListingRepository** | Lưu/đọc/xóa BikeListing. |
| **IBicycleRepository** | Lưu Bicycle (khi tạo bài đăng có xe hoặc thêm xe cho pending). |
| **IBrandRepository** / **ICategoryRepository** | Lấy Brand, Category trong buildBicycleFromRequest. |
| **IUserRepository** / **IEventRepository** | Lấy seller, event khi tạo/cập nhật listing. |

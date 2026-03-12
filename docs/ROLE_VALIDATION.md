# Kiểm tra validation theo Role

## Các role trong hệ thống

| Role      | Mô tả ngắn |
|-----------|------------|
| **ADMIN** | Quản trị: quản lý user, category, brand, toàn quyền |
| **INSPECTOR** | Nhân viên kiểm định: tạo/cập nhật/xóa báo cáo kiểm định |
| **USER**  | Người dùng thường: đăng bài, đặt chỗ, tranh chấp, v.v. |

---

## SecurityConfig (HTTP)

- **permitAll (không cần đăng nhập):**
  - `POST /auth/login`
  - `POST /auth/introspect`
  - `POST /users` — đăng ký tài khoản USER (public)
- **Còn lại:** `anyRequest().authenticated()` — cần JWT hợp lệ.

**Lưu ý:** `POST /users/inspector` **không** nằm trong permitAll → cần đăng nhập; và ở tầng service bắt buộc **ADMIN** (xem UserService).

---

## Validation theo từng module (Method Security – @PreAuthorize / @PostAuthorize)

### 1. UserService

| Method            | Role / Điều kiện | Ghi chú |
|-------------------|------------------|--------|
| createUser        | Không (public)   | Đăng ký USER qua POST /users |
| **createInspector** | **ADMIN**      | Chỉ ADMIN được tạo tài khoản INSPECTOR |
| getAllUser        | ADMIN            | Danh sách user |
| getUserById       | Self hoặc ADMIN  | @PostAuthorize |
| updateUser        | Self hoặc ADMIN  | @PostAuthorize |
| deleteUser        | ADMIN            | Xóa user |
| deActiveUser      | ADMIN            | Kích hoạt / vô hiệu hóa user |
| getMyInfo         | Self hoặc ADMIN  | @PostAuthorize |

### 2. InspectionReportService

| Method                    | Role              | Ghi chú |
|---------------------------|-------------------|--------|
| createInspectionReport    | INSPECTOR hoặc ADMIN | Tạo báo cáo kiểm định |
| updateInspectionReport    | INSPECTOR hoặc ADMIN | Cập nhật báo cáo |
| deleteInspectionReport    | INSPECTOR hoặc ADMIN | Xóa báo cáo |
| findAllInspectionReports  | Không (chỉ cần authenticated) | Danh sách báo cáo |
| findInspectionReportById  | Không (chỉ cần authenticated) | Chi tiết báo cáo |

### 3. CategoryService

| Method          | Role  | Ghi chú |
|-----------------|-------|--------|
| createCategory  | ADMIN | Tạo danh mục |
| updateCategory  | ADMIN | Sửa danh mục |
| deleteCategory  | ADMIN | Xóa danh mục |
| getAllCategories | Không | GET — mọi user đã đăng nhập |
| getCategoryById | Không | GET — mọi user đã đăng nhập |

### 4. BrandService

| Method        | Role  | Ghi chú |
|---------------|-------|--------|
| createBrand   | ADMIN | Tạo thương hiệu |
| updateBrand   | ADMIN | Sửa thương hiệu |
| deleteBrand   | ADMIN | Xóa thương hiệu |
| getAllBrands  | Không | GET — mọi user đã đăng nhập |
| getBrandById  | Không | GET — mọi user đã đăng nhập |

---

## Các module chưa áp dụng validation theo role (cần xem xét thêm)

Các controller/service sau **chưa** dùng `@PreAuthorize` / `@PostAuthorize` theo role. Mọi request chỉ cần **authenticated**.

| Module              | Gợi ý validation (tùy nghiệp vụ) |
|---------------------|------------------------------------|
| **BikeListingService** | Create: USER/INSPECTOR/ADMIN; Update/Delete: chỉ **chủ bài đăng (seller)** hoặc ADMIN (cần so sánh `listing.getSeller()` với user hiện tại). |
| **DisputeService**  | Create: USER (bên liên quan); Update/Delete hoặc xử lý: ADMIN hoặc quy tắc tranh chấp. |
| **ReservationService** | Create/Update/Delete: chủ đặt chỗ hoặc ADMIN. |
| **EventController/Service** | CRUD event: thường ADMIN. |
| **TransactionController/Service** | Tùy luồng thanh toán (USER/ADMIN). |
| **DepositController** | Tùy luồng đặt cọc. |
| **WishlistController** | Thường chỉ chủ wishlist (self). |
| **FileUploadController** | Có thể giới hạn theo role hoặc theo resource (ví dụ chỉ owner/ADMIN). |
| **CheckInController** | Tùy quy trình check-in (USER/INSPECTOR/ADMIN). |

---

## Tóm tắt đã sửa (so với trước khi kiểm tra)

1. **UserService.createInspector**  
   Trước: bất kỳ user đã đăng nhập đều gọi được.  
   Sau: thêm `@PreAuthorize("hasRole('ADMIN')")` → chỉ ADMIN được tạo inspector.

2. **InspectionReportService**  
   Thêm `@PreAuthorize("hasAnyRole('INSPECTOR', 'ADMIN')")` cho:  
   `createInspectionReport`, `updateInspectionReport`, `deleteInspectionReport`.

3. **CategoryService**  
   Thêm `@PreAuthorize("hasRole('ADMIN')")` cho:  
   `createCategory`, `updateCategory`, `deleteCategory`.

4. **BrandService**  
   Thêm `@PreAuthorize("hasRole('ADMIN')")` cho:  
   `createBrand`, `updateBrand`, `deleteBrand`.

---

## Cách kiểm tra nhanh

- **ADMIN:** Gọi POST /users/inspector, CRUD category/brand, CRUD inspection report → phải thành công (với JWT ADMIN).
- **INSPECTOR:** Gọi CRUD inspection report → thành công; POST /users/inspector hoặc CRUD category/brand → 403 Forbidden.
- **USER:** Chỉ được đăng ký (POST /users), get/update bản thân; không được createInspector, CRUD category/brand, hoặc (tùy cấu hình) CRUD inspection report → 403 khi gọi các API không cho phép.

Cấu hình JWT đang dùng prefix `ROLE_` (trong `JwtAuthenticationConverter`), nên trong token scope/authorities phải có dạng `ROLE_ADMIN`, `ROLE_INSPECTOR`, `ROLE_USER`.

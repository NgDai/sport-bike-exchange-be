# Chạy server để test Postman

## Bước 1: Mở terminal tại thư mục project
```powershell
cd "D:\FPTU powerpoint\Spring2026\SWP391\Back-end ver 3\sport-bike-exchange-be"
```

## Bước 2: Chạy ứng dụng (khởi động server)
```powershell
mvn spring-boot:run
```

## Bước 3: Đợi server sẵn sàng
- **Không tắt** cửa sổ terminal.
- Đợi đến khi thấy dòng chữ màu xanh trong log:
  ```
  Started SportBikeExchangeBeApplication in ... seconds
  ```
- Khi thấy dòng đó = server đang chạy trên http://localhost:8080.

## Bước 4: Gửi request trong Postman
- Method: **POST**
- URL: `http://localhost:8080/api/auth/login`
- Body → **raw** → **JSON**:
  ```json
  {
    "username": "admin",
    "password": "1"
  }
  ```
- Bấm **Send**.

---

**Lưu ý:** Nếu bạn chỉ chạy `mvn compile` hoặc `mvn package` thì chỉ build (BUILD SUCCESS), **không** khởi động server. Phải chạy `mvn spring-boot:run` và giữ terminal mở thì Postman mới kết nối được.

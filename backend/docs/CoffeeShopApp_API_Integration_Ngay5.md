# COFFEESHOPAPP — TÀI LIỆU TÍCH HỢP API (NGÀY 5)

**Dành cho:** Client Android, Admin Android và Backend  
**Stack:** Android Java/XML + Retrofit/Gson; Spring Boot 4.0.0 + Spring Security/JWT + MySQL  
**Mục đích:** Thống nhất cách gọi API, dữ liệu JSON, quyền truy cập và xử lý lỗi khi tích hợp lần 1.

## 1. Địa chỉ kết nối

| Nơi gọi API | Base URL | Lưu ý |
|---|---|---|
| Postman / trình duyệt trên máy chạy Spring Boot | `http://localhost:8080/` | Dùng cho thử nghiệm local |
| Android Emulator chuẩn (kết nối host) | `http://10.0.2.2:8080/` | `10.0.2.2` trỏ về máy host của Android Emulator |
| Android Emulator đã thiết lập ADB reverse | `http://127.0.0.1:8080/` | Chỉ hoạt động khi lệnh `adb reverse tcp:8080 tcp:8080` đang áp dụng cho đúng emulator |
| Điện thoại Android thật | `http://<IP-LAN-CUA-MAY-BACKEND>:8080/` | Cùng mạng LAN; cho phép kết nối qua Windows Firewall; không dùng `localhost` |

> Khi triển khai thật, chuyển sang HTTPS và URL máy chủ chính thức. `android:usesCleartextTraffic="true"` chỉ nên dùng cho môi trường phát triển nội bộ.

## 2. Quy ước chung

- Tiền tố endpoint: `/api`.
- Request JSON: Header `Content-Type: application/json`.
- API công khai: Không cần Bearer Token.
- API quản trị: Header `Authorization: Bearer <JWT_ADMIN>`.
- JWT được lấy từ endpoint login; ứng dụng Android nên lưu và xóa session/token theo trạng thái đăng nhập.
- Phần `category` trong Product hiện lưu **mã danh mục dạng chuỗi** (ví dụ `COFFEE`), chưa phải quan hệ khóa ngoại JPA.
- Field `imageUrl` là **đường dẫn HTTP/HTTPS**, không phải file ảnh được upload trực tiếp.

## 3. Danh sách 12 endpoint chính

| # | Method | Endpoint | Quyền | Phản hồi thành công đã dùng trong dự án |
|---|---|---|---|---|
| 1 | POST | `/api/auth/register` | Public | Phản hồi theo `AuthController` hiện tại; kiểm tra trong Postman |
| 2 | POST | `/api/auth/login` | Public | `200 OK` + JSON có `token`, `role` |
| 3 | GET | `/api/categories` | Public | `200 OK` + mảng Category |
| 4 | GET | `/api/categories/{id}` | Public | `200 OK` + Category |
| 5 | POST | `/api/categories` | ADMIN | `201 Created` + Category |
| 6 | PUT | `/api/categories/{id}` | ADMIN | `200 OK` + Category |
| 7 | DELETE | `/api/categories/{id}` | ADMIN | `204 No Content` |
| 8 | GET | `/api/products` | Public | `200 OK` + mảng Product |
| 9 | GET | `/api/products/{id}` | Public | `200 OK` + Product |
| 10 | POST | `/api/products` | ADMIN | `200 OK` + Product (Controller hiện tại) |
| 11 | PUT | `/api/products/{id}` | ADMIN | `200 OK` + Product |
| 12 | DELETE | `/api/products/{id}` | ADMIN | `200 OK` + thông báo xóa |

> Mã HTTP phản ánh Controller đang triển khai ở Ngày 5. Nếu thay đổi Controller, cần cập nhật tài liệu này và Postman Collection.

## 4. Mẫu JSON

### 4.1. Đăng ký

`POST /api/auth/register`

```json
{
  "name": "Khach Hang Test",
  "email": "customer.test@example.com",
  "password": "<MAT_KHAU_TU_CHON>"
}
```

### 4.2. Đăng nhập

`POST /api/auth/login`

```json
{
  "email": "<EMAIL_DA_DANG_KY>",
  "password": "<MAT_KHAU_DANG_NHAP>"
}
```

Ví dụ phần dữ liệu phản hồi quan trọng (rút gọn, không phải token thật):

```json
{
  "token": "<JWT_DUOC_SERVER_CAP>",
  "role": "ADMIN"
}
```

Client và Admin dùng trường `token` để gửi các request cần xác thực; vai trò `ADMIN` quyết định quyền quản trị ở Backend. **Không hardcode JWT hoặc mật khẩu trong mã nguồn.**

### 4.3. Tạo Category — chỉ ADMIN

`POST /api/categories`

```json
{
  "code": "TEA",
  "name": "Trà",
  "description": "Các loại trà trái cây và trà pha chế",
  "available": true
}
```

Category response có thể gồm `id`, `code`, `name`, `description`, `available`, `createdAt`.

### 4.4. Tạo Product — chỉ ADMIN

`POST /api/products`

```json
{
  "name": "Trà đào cam sả",
  "description": "Trà đào kết hợp cam tươi và sả",
  "price": 39000,
  "imageUrl": "https://images.unsplash.com/photo-1544787219-7f47ccb76574",
  "category": "TEA",
  "available": true
}
```

Product response có thể gồm `id`, `name`, `description`, `price`, `imageUrl`, `category`, `available`, `createdAt`. Trường `price` là số; Android nên dùng kiểu dữ liệu phù hợp với giá tiền, không coi là chuỗi.

## 5. Quy tắc xác thực và phân quyền

- `GET /api/categories/**` và `GET /api/products/**`: không yêu cầu đăng nhập.
- `POST`, `PUT`, `DELETE` trên Category và Product: chỉ ADMIN.
- Request từ tài khoản USER gọi API ADMIN đã được kiểm thử và bị từ chối với `403 Forbidden`.
- Request không có JWT gọi API ADMIN đã được kiểm thử và bị từ chối với `403 Forbidden` ở cấu hình hiện tại.
- Token cũ không còn hợp lệ sau khi Backend thay `JWT_SECRET`: đăng nhập lại.
- Không chia sẻ JWT thật trong ảnh chụp, repository hoặc Postman Collection xuất cho nhóm.

## 6. Quy ước phản hồi lỗi

Ví dụ lỗi nghiệp vụ sau khi áp dụng `GlobalExceptionHandler`:

```json
{
  "timestamp": "<THOI_DIEM>",
  "status": 400,
  "error": "Bad Request",
  "message": "Gia san pham phai lon hon 0",
  "path": "/api/products"
}
```

- `400 Bad Request`: dữ liệu không hợp lệ (giá <= 0, tên rỗng, URL sai, danh mục chưa tồn tại).
- `404 Not Found`: không tìm thấy Category/Product theo ID.
- `409 Conflict`: mã Category trùng.
- `401/403`: lỗi xác thực/ủy quyền; Spring Security có thể trả response khác cấu trúc `GlobalExceptionHandler`, Android cần dựa cả vào HTTP status.
- Không trả stack trace Java cho Android.

## 7. Cấu hình Retrofit gợi ý

```java
new Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8080/")
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

Khi dùng ADB reverse cho đúng máy ảo, thay base URL bằng `http://127.0.0.1:8080/`.

Ví dụ API cần header:

```java
@POST("api/products")
Call<JsonObject> createProduct(
    @Header("Authorization") String authorization,
    @Body JsonObject product
);
```

Giá trị `authorization`: `"Bearer " + jwtToken` (nếu interface không tự thêm tiền tố).

**AndroidManifest (phát triển local):** `INTERNET` permission; HTTP cleartext chỉ trong môi trường thử nghiệm.

## 8. Dữ liệu mẫu / seed

Dữ liệu mẫu dự kiến:

| Mã danh mục | Tên sản phẩm | Giá |
|---|---|---:|
| `COFFEE` | Cà phê sữa đá | 35.000 |
| `TEA` | Trà đào cam sả | 39.000 |
| `JUICE` | Nước ép cam tươi | 35.000 |

File seed dự kiến trong repository: `backend/database/CoffeeShopApp_seed_ngay5.sql`.

**Lưu ý:** Dữ liệu MySQL local không tự đồng bộ qua Git. Mỗi thành viên cần cấu hình MySQL của mình và nạp seed sau khi Backend đã tạo bảng, hoặc kết nối tới một Backend/database dùng chung có kiểm soát.

## 9. Checklist kiểm thử/bàn giao

- [x] Spring Boot khởi động và kết nối MySQL trên port 8080.
- [x] Android Emulator truy cập `/api/categories` bằng ADB reverse.
- [x] Admin Android dùng Retrofit lấy danh sách Category từ API.
- [x] Emulator truy cập `/api/products` và nhận JSON.
- [x] Postman login trả JWT và role.
- [x] ADMIN tạo Product thành công bằng Bearer JWT.
- [x] USER và request không có JWT bị từ chối khi POST Product.
- [x] Postman Collection 12 endpoint đã được import.
- [ ] Client Android tích hợp Login/Menu bằng Retrofit và dữ liệu MySQL thật.
- [ ] Admin Android thay phần đăng nhập và CRUD mock/local bằng API thật.
- [ ] Kiểm thử end-to-end Client/Admin trên máy của từng thành viên.
- [ ] Review/merge PR Ngày 5 lên `develop`.

## 10. Lưu ý cho quy trình Git

- Backend làm việc trên `feature/backend-integration-1` → Pull Request vào `develop`.
- Các thay đổi Android Admin (`admin/`) không nên commit chung vào feature Backend; thành viên Admin cần nhận/bàn giao các thay đổi trên nhánh riêng của họ.
- Trước khi commit, luôn chạy `git status`, `git diff --cached --check`, kiểm tra không chứa JWT, mật khẩu MySQL hoặc file cấu hình bí mật.

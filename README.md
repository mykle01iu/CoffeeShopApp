# CoffeeShopApp
Dự án lập trình ứng dụng thiết bị di động của Nhóm 15349899

# CoffeeShopApp

## 1. Giới thiệu dự án

CoffeeShopApp là dự án lập trình ứng dụng thiết bị di động dành cho quán cà phê.

Hệ thống được chia thành 3 phần chính:

- Client Android: ứng dụng dành cho khách hàng.
- Admin Android: ứng dụng dành cho quản trị viên.
- Backend: REST API sử dụng Spring Boot kết nối MySQL.

Mục tiêu của dự án là xây dựng một hệ thống có thể quản lý sản phẩm, tài khoản người dùng, đặt hàng và các chức năng liên quan đến hoạt động của quán cà phê.

---

## 2. Công nghệ sử dụng

### Android

- Java
- XML
- RecyclerView
- Retrofit
- Gson
- Glide
- SharedPreferences
- Android Studio

### Backend

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT Authentication
- MySQL
- Maven
- REST API

### Công cụ hỗ trợ

- Android Studio
- Visual Studio Code
- MySQL Workbench
- Postman
- Git
- GitHub

---

## 3. Cấu trúc dự án

Dự án được tổ chức theo hướng tách riêng Backend, Client Android và Admin Android.

```text
CoffeeShopApp/
│
├── backend/
│   └── Spring Boot REST API
│
├── client-app/
│   └── Ứng dụng Android dành cho khách hàng
│
├── admin-app/
│   └── Ứng dụng Android dành cho quản trị viên
│
├── .gitignore
└── README.md
```

Tên thư mục thực tế có thể thay đổi trong quá trình phát triển nhưng các thành viên cần đảm bảo không đặt code Android vào thư mục Backend và ngược lại.

---

## 4. Quy tắc sử dụng Git của nhóm

Dự án sử dụng Git và GitHub để quản lý source code.

Các thành viên cần tuân thủ đúng quy trình branch để tránh ghi đè code hoặc merge nhầm.

### Branch chính

```text
main
```

Là phiên bản ổn định của dự án.

Không code trực tiếp trên branch `main`.

Không push trực tiếp code đang phát triển lên `main`.

---

```text
develop
```

Là branch tích hợp code của cả nhóm.

Tất cả các chức năng hoàn thành sẽ được merge vào `develop` trước.

Sau khi toàn bộ chức năng trên `develop` ổn định và đã kiểm thử thì mới merge:

```text
develop → main
```

Không nên code trực tiếp trên `develop`.

---

### Feature branch

Mỗi thành viên khi làm một chức năng mới phải tạo một branch riêng.

Ví dụ:

```text
feature/backend-products
feature/admin-init
feature/client-login
feature/client-products
feature/admin-products
feature/orders
```

Quy trình chuẩn:

```text
develop
   ↓
feature/...
   ↓
Pull Request
   ↓
develop
   ↓
kiểm thử
   ↓
main
```

---

## 5. Quy trình trước khi bắt đầu làm một task mới

Trước khi code task mới, luôn cập nhật branch `develop`.

```bash
git checkout develop
```

Sau đó:

```bash
git pull origin develop
```

Sau khi đã lấy code mới nhất từ `develop`, tạo branch mới.

Ví dụ:

```bash
git checkout -b feature/client-products
```

Sau đó mới bắt đầu code.

Không nên tiếp tục sử dụng branch feature cũ đã merge để làm chức năng mới.

---

## 6. Quy trình sau khi hoàn thành chức năng

Kiểm tra file đã thay đổi:

```bash
git status
```

Thêm các file cần commit:

```bash
git add .
```

Commit:

```bash
git commit -m "feat: mô tả chức năng"
```

Ví dụ:

```bash
git commit -m "feat: add product management"
```

Push branch:

```bash
git push -u origin feature/ten-branch
```

Sau đó lên GitHub tạo Pull Request.

Phải chọn:

```text
base: develop
compare: feature/ten-branch
```

Ví dụ:

```text
base: develop
compare: feature/admin-products
```

KHÔNG tạo Pull Request:

```text
feature/... → main
```

Quy trình đúng:

```text
feature/... → develop
```

Sau khi Pull Request được merge thành công vào `develop`, branch feature có thể được xóa để repository gọn hơn.

---

## 7. Lưu ý về Push, Pull và Merge

`git push` chỉ có tác dụng đưa code của branch hiện tại lên GitHub.

Ví dụ:

```bash
git push origin feature/admin-init
```

không có nghĩa là code đã xuất hiện trong `develop`.

Muốn code xuất hiện trong `develop` phải:

```text
Push branch
→ Tạo Pull Request
→ Chọn base develop
→ Merge Pull Request
```

---

`git pull` dùng để lấy code mới nhất trên GitHub về máy.

Ví dụ:

```bash
git pull origin develop
```

Không sử dụng `git pull` để merge feature branch vào `develop` trên GitHub.

---

## 8. Chạy Backend

Backend nằm trong thư mục:

```text
backend
```

Có thể mở bằng Visual Studio Code.

Di chuyển vào thư mục backend:

```bash
cd backend
```

Backend hiện sử dụng MySQL làm database.

Thông tin cấu hình nằm trong:

```text
src/main/resources/application.properties
```

Backend hiện chạy mặc định tại:

```text
http://localhost:8080
```

---

## 9. Biến môi trường Backend

Backend không lưu trực tiếp mật khẩu MySQL và JWT Secret trong GitHub.

Các biến cần thiết:

```text
DB_PASSWORD
JWT_SECRET
```

Trong Git Bash có thể kiểm tra:

```bash
if [ -n "$DB_PASSWORD" ]; then echo "DB_PASSWORD OK"; else echo "DB_PASSWORD MISSING"; fi
```

Kiểm tra JWT:

```bash
if [ -n "$JWT_SECRET" ]; then echo "JWT_SECRET OK"; else echo "JWT_SECRET MISSING"; fi
```

Nếu chưa có DB_PASSWORD:

```bash
export DB_PASSWORD='YOUR_MYSQL_PASSWORD'
```

Không gửi mật khẩu MySQL thật cho các thành viên khác qua GitHub.

Nếu chưa có JWT Secret:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
```

Lưu ý:

Biến môi trường trong Git Bash có thể mất khi đóng terminal.

Nếu mở terminal mới mà Backend lỗi kết nối database thì kiểm tra lại `DB_PASSWORD`.

---

## 10. Chạy Backend

Kiểm tra project trước:

```bash
./mvnw clean test
```

Nếu xuất hiện:

```text
BUILD SUCCESS
```

thì có thể chạy Backend:

```bash
./mvnw spring-boot:run
```

Khi thành công sẽ xuất hiện thông báo tương tự:

```text
Tomcat started on port 8080
Started BackendApplication
```

Backend:

```text
http://localhost:8080
```

---

## 11. Database

Database sử dụng MySQL.

Tên database:

```text
coffee_shop
```

Lệnh tạo database:

```sql
CREATE DATABASE coffee_shop
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Chọn database:

```sql
USE coffee_shop;
```

Hibernate đang được cấu hình:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Do đó Spring Boot có thể tự động tạo hoặc cập nhật một số bảng dựa trên Entity.

---

## 12. Không commit thông tin nhạy cảm

Không được commit các thông tin sau lên GitHub:

```text
MySQL password
JWT Secret
API Key
Token đăng nhập
Tài khoản thật
Thông tin cá nhân
File chứa mật khẩu
```

Các thông tin nhạy cảm phải dùng biến môi trường.

---

## 13. Authentication

Backend sử dụng JWT Authentication.

Hiện tại có 2 Role chính:

```text
USER
ADMIN
```

---

## 14. API đăng ký tài khoản

Endpoint:

```text
POST /api/auth/register
```

URL local:

```text
http://localhost:8080/api/auth/register
```

Ví dụ body:

```json
{
  "name": "User Test",
  "email": "user@test.com",
  "password": "123456"
}
```

Tài khoản đăng ký mặc định có quyền:

```text
USER
```

---

## 15. API đăng nhập

Endpoint:

```text
POST /api/auth/login
```

URL:

```text
http://localhost:8080/api/auth/login
```

Ví dụ:

```json
{
  "email": "user@test.com",
  "password": "123456"
}
```

Nếu đăng nhập thành công, Backend trả về JWT Token.

Ví dụ:

```json
{
  "token": "eyJ...",
  "name": "User Test",
  "email": "user@test.com",
  "role": "USER"
}
```

---

## 16. Sử dụng JWT Token

Những API yêu cầu đăng nhập phải gửi Header:

```text
Authorization: Bearer JWT_TOKEN
```

Trong Postman:

```text
Authorization
→ Bearer Token
→ Dán Token
```

Không gửi JWT Token lên GitHub.

---

## 17. Phân quyền

### USER

USER được sử dụng các chức năng dành cho khách hàng.

Ví dụ:

```text
Xem sản phẩm
Đăng nhập
Đăng ký
Đặt hàng
Xem lịch sử đơn hàng
```

Các chức năng sẽ được bổ sung dần trong quá trình phát triển.

---

### ADMIN

ADMIN có quyền quản trị hệ thống.

Ví dụ:

```text
Thêm sản phẩm
Sửa sản phẩm
Xóa sản phẩm
Quản lý đơn hàng
Quản lý dữ liệu
```

---

## 18. Product API

Product API hiện đã hỗ trợ CRUD cơ bản.

### Lấy danh sách sản phẩm

```text
GET /api/products
```

Quyền:

```text
PUBLIC
```

URL:

```text
http://localhost:8080/api/products
```

---

### Lấy sản phẩm theo ID

```text
GET /api/products/{id}
```

Ví dụ:

```text
GET /api/products/1
```

Quyền:

```text
PUBLIC
```

---

### Thêm sản phẩm

```text
POST /api/products
```

Quyền:

```text
ADMIN
```

Ví dụ body:

```json
{
  "name": "Cà phê sữa",
  "description": "Cà phê pha phin với sữa đặc",
  "price": 30000,
  "imageUrl": "https://example.com/caphe-sua.jpg",
  "category": "COFFEE",
  "available": true
}
```

---

### Sửa sản phẩm

```text
PUT /api/products/{id}
```

Ví dụ:

```text
PUT /api/products/1
```

Quyền:

```text
ADMIN
```

Ví dụ:

```json
{
  "name": "Cà phê sữa đá",
  "description": "Cà phê pha phin với sữa đặc và đá",
  "price": 35000,
  "imageUrl": "https://example.com/caphe-sua-da.jpg",
  "category": "COFFEE",
  "available": true
}
```

---

### Xóa sản phẩm

```text
DELETE /api/products/{id}
```

Ví dụ:

```text
DELETE /api/products/1
```

Quyền:

```text
ADMIN
```

---

## 19. Tổng hợp Product API

| Method | Endpoint | Quyền | Chức năng |
|---|---|---|---|
| GET | `/api/products` | Public | Lấy danh sách sản phẩm |
| GET | `/api/products/{id}` | Public | Xem chi tiết sản phẩm |
| POST | `/api/products` | ADMIN | Thêm sản phẩm |
| PUT | `/api/products/{id}` | ADMIN | Sửa sản phẩm |
| DELETE | `/api/products/{id}` | ADMIN | Xóa sản phẩm |

---

## 20. Android kết nối Backend

Đây là phần rất quan trọng.

Nếu chạy Android Emulator thì KHÔNG dùng:

```text
http://localhost:8080
```

Vì `localhost` trong Android Emulator là chính máy Android Emulator.

Để gọi Backend đang chạy trên máy tính, sử dụng:

```text
http://10.0.2.2:8080
```

Ví dụ Retrofit:

```java
private static final String BASE_URL =
        "http://10.0.2.2:8080/";
```

---

## 21. Chạy Android bằng điện thoại thật

Nếu chạy bằng điện thoại thật thì điện thoại và máy tính cần kết nối cùng mạng.

Không dùng:

```text
localhost
```

Không dùng:

```text
10.0.2.2
```

Phải dùng IP LAN của máy tính.

Ví dụ:

```text
http://192.168.1.10:8080
```

IP thực tế tùy máy.

Có thể kiểm tra IP Windows bằng:

```bash
ipconfig
```

Tìm:

```text
IPv4 Address
```

---

## 22. Android Client

Client Android dành cho người dùng.

Dự kiến gồm các chức năng:

```text
Đăng ký
Đăng nhập
Xem danh sách sản phẩm
Xem chi tiết sản phẩm
Giỏ hàng
Đặt hàng
Xem đơn hàng
Thông tin tài khoản
```

Client sử dụng Retrofit để gọi Backend API.

---

## 23. Android Admin

Admin Android dành cho quản trị viên.

Dự kiến gồm:

```text
Đăng nhập ADMIN
Dashboard
Quản lý sản phẩm
Thêm sản phẩm
Sửa sản phẩm
Xóa sản phẩm
Quản lý đơn hàng
```

Các API thay đổi dữ liệu yêu cầu JWT có quyền ADMIN.

---

## 24. Retrofit

Android Client và Admin sẽ sử dụng Retrofit để gọi API.

Ví dụ Base URL khi dùng Emulator:

```text
http://10.0.2.2:8080/
```

Không quên dấu `/` cuối URL khi cấu hình Retrofit.

---

## 25. SharedPreferences

SharedPreferences có thể được sử dụng để lưu:

```text
JWT Token
Tên người dùng
Email
Role
Trạng thái đăng nhập
```

Không nên lưu mật khẩu người dùng dạng plain text trong SharedPreferences.

---

## 26. Glide

Glide được sử dụng để tải ảnh sản phẩm từ URL.

Ví dụ trường Backend:

```json
"imageUrl": "https://example.com/product.jpg"
```

Android có thể sử dụng Glide để hiển thị ảnh trong RecyclerView.

---

## 27. Quy trình tích hợp Android với Backend

Trước khi thành viên Android gọi API, cần kiểm tra API bằng Postman trước.

Quy trình:

```text
Backend hoàn thành API
↓
Test API bằng Postman
↓
API hoạt động
↓
Android tạo Retrofit interface
↓
Android gọi API
↓
Hiển thị dữ liệu
```

Nếu Postman chưa gọi được thì chưa nên sửa Android.

---

## 28. Kiểm tra Backend Health

Backend có API kiểm tra hoạt động:

```text
GET /api/health
```

URL:

```text
http://localhost:8080/api/health
```

Nếu Backend chạy đúng sẽ trả về:

```text
Backend OK
```

---

## 29. Một số lỗi thường gặp

### Lỗi Backend không kết nối MySQL

Nếu xuất hiện:

```text
Unable to determine Dialect without JDBC metadata
```

hãy kiểm tra:

```text
DB_PASSWORD
MySQL Server
Database coffee_shop
application.properties
```

Kiểm tra biến:

```bash
if [ -n "$DB_PASSWORD" ]; then echo "DB_PASSWORD OK"; else echo "DB_PASSWORD MISSING"; fi
```

---

### Android không gọi được localhost

Sai:

```text
http://localhost:8080
```

Đúng khi chạy Emulator:

```text
http://10.0.2.2:8080
```

---

### API trả 403

Có thể do:

```text
Không có JWT
JWT sai
Không đủ Role
ADMIN API nhưng đang login USER
```

---

### API PUT trả 405

Kiểm tra URL.

Sai:

```text
PUT /api/products
```

Đúng:

```text
PUT /api/products/1
```

Vì API update cần ID.

---

### Git branch đã push nhưng develop không có code

Push branch không có nghĩa code đã vào develop.

Phải:

```text
feature branch
→ Pull Request
→ develop
→ Merge
```

---

## 30. Phân công nhóm

### Thành viên 1

Phụ trách:

```text
Client Android
Giao diện khách hàng
Retrofit Client
Hiển thị dữ liệu
Đăng nhập / đăng ký phía Client
```

---

### Thành viên 2

Phụ trách:

```text
Admin Android
Giao diện quản trị
Dashboard
Quản lý sản phẩm
Tích hợp API Admin
```

---

### Thành viên 3

Phụ trách:

```text
Backend Spring Boot
MySQL Database
REST API
JWT Authentication
Spring Security
Phân quyền
Tích hợp Backend với Android
Quản lý Git / Integration
```

---

## 31. Quy ước Commit

Nên viết commit dễ hiểu.

Ví dụ:

```text
feat: add product CRUD API
feat: add admin login screen
feat: integrate product API
fix: fix login request
fix: fix product update
refactor: clean product service
docs: update README
```

Không nên commit kiểu:

```text
update
code
fix
abc
final
final2
test123
```

---

## 32. Quy tắc Pull Request

Mỗi Pull Request nên ghi rõ:

```text
Chức năng đã làm
File đã thay đổi
API đã thêm
Đã test hay chưa
Có lỗi gì còn tồn tại không
```

Ví dụ:

```text
Add Product entity
Add ProductRepository
Add ProductService
Add ProductController
Add Product CRUD APIs
Add ADMIN authorization
Test GET / POST / PUT / DELETE successfully
```

---

## 33. Không sửa code của thành viên khác tùy tiện

Khi cần sửa phần của thành viên khác:

1. Thông báo cho thành viên đó.
2. Kiểm tra branch mới nhất.
3. Pull `develop`.
4. Không ghi đè code đang làm.
5. Nếu có conflict phải xử lý cẩn thận.

---

## 34. Trước khi Push

Luôn kiểm tra:

```bash
git status
```

Nếu backend có thay đổi thì nên chạy:

```bash
./mvnw clean test
```

Nếu:

```text
BUILD SUCCESS
```

thì mới nên commit.

---

## 35. Sau khi Merge Pull Request

Sau khi PR được merge vào `develop`:

Chuyển về develop:

```bash
git checkout develop
```

Lấy code mới:

```bash
git pull origin develop
```

Có thể xóa feature branch local:

```bash
git branch -d feature/ten-branch
```

Branch remote có thể xóa trên GitHub sau khi merge.

---

## 36. Quy trình tổng quát của nhóm

```text
1. Checkout develop
2. Pull develop mới nhất
3. Tạo feature branch
4. Code chức năng
5. Test
6. Git add
7. Git commit
8. Git push
9. Tạo Pull Request vào develop
10. Review
11. Merge
12. Thành viên khác pull develop mới
```

---

## 37. Lưu ý quan trọng

Không code trực tiếp trên:

```text
main
develop
```

Không PR:

```text
feature → main
```

Phải PR:

```text
feature → develop
```

Không commit:

```text
Password
JWT Secret
Token
API Key
```

Android Emulator gọi Backend bằng:

```text
http://10.0.2.2:8080
```

Backend local chạy tại:

```text
http://localhost:8080
```

Luôn test Backend API bằng Postman trước khi tích hợp vào Android.

Luôn pull `develop` mới nhất trước khi bắt đầu chức năng mới.

---

## 38. Trạng thái hiện tại

Backend hiện đã hoàn thành các phần cơ bản:

```text
MySQL Connection
Health API
Register API
Login API
JWT Authentication
Spring Security
Role USER / ADMIN
Product Entity
Product Repository
Product Service
Product Controller
Product CRUD
Product Authorization
```

Product API đã kiểm thử:

```text
GET
POST
PUT
DELETE
```

Các chức năng tiếp theo sẽ tiếp tục được phát triển và cập nhật vào README.

---

## 39. Mục tiêu tiếp theo

Các phần dự kiến phát triển:

```text
Android Client login/register
Android Client product list
Android Admin login
Android Admin product management
Cart
Orders
Order Detail
User profile
Admin order management
Integration testing
```

---

## 40. Nguyên tắc chung

Mỗi thành viên cần:

```text
Đọc README trước khi code
Không tự ý thay đổi cấu trúc chung
Không push trực tiếp main
Không merge nhầm branch
Không chia sẻ secret
Test chức năng trước khi PR
Thông báo cho nhóm khi thay đổi API
```

Khi Backend thay đổi API, thành viên Backend cần cập nhật README hoặc thông báo cho thành viên Android để tránh sử dụng endpoint hoặc JSON cũ.

---

## CoffeeShopApp Team

Dự án được xây dựng phục vụ môn học lập trình ứng dụng thiết bị di động.

Mục tiêu của nhóm là đảm bảo source code dễ quản lý, dễ tích hợp và các thành viên có thể làm việc độc lập trên từng branch mà không ảnh hưởng đến code của nhau.

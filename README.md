# BDS Backend – Quản lý trung tâm môi giới bất động sản

Backend (Spring Boot) của hệ thống Quản lý trung tâm môi giới BĐS (SRS v4.0). Frontend (ReactJS) nằm ở repo riêng.

## Công nghệ

| Thành phần | Phiên bản |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.2 (Spring Data JPA, Spring Security, Validation, Mail) |
| Maven | dùng Maven Wrapper (`mvnw`), không cần cài Maven riêng |
| Cơ sở dữ liệu | MySQL 8.0 chạy bằng Docker |
| Xác thực | JWT (jjwt) |
| Tài liệu API | springdoc-openapi (Swagger UI) |
| Lưu ảnh | Cloudinary |

## Bắt đầu nhanh

Cần cài sẵn: JDK 17, Docker Desktop, Git.

```bash
git clone https://github.com/CodeGym-mockup-gr5/bds-backend-springboot.git
cd bds-backend-springboot
git checkout develop

cd database
docker compose up -d
cd ..

./mvnw spring-boot:run
```

Trên Windows PowerShell dùng `.\mvnw` thay cho `./mvnw`.

Sau khi thấy dòng `Started BdsBackendApplication`, mở Swagger tại http://localhost:8080/swagger-ui.html.

## Cơ sở dữ liệu

- Thư mục `database/` có `docker-compose.yml` và hai script chạy tự động khi tạo container lần đầu:
  - `init/01_V1__init_schema.sql`: cấu trúc bảng, **nguồn sự thật duy nhất của schema**.
  - `init/02_V2__sample_data.sql`: dữ liệu mẫu cho môi trường phát triển.
- Kết nối mặc định: `localhost:3307`, database `bds_broker`, user `root`, mật khẩu `root`.
- Hibernate chạy `ddl-auto=validate`: entity phải khớp V1. Cấm dùng `update` hoặc `create`.
- Khi V1 hoặc V2 thay đổi (hoặc có người sửa), mọi người tạo lại database:

```bash
cd database
docker compose down -v
docker compose up -d
```

Lệnh `down -v` xóa toàn bộ dữ liệu cũ trong container.

- Không tự ý sửa `V1__init_schema.sql`. Muốn đổi schema thì thảo luận với cả nhóm, vì ai cũng phải tạo lại database.

## Cấu hình

Cấu hình chung nằm trong `src/main/resources/application.yml`. Giá trị nhạy cảm đọc từ biến môi trường, **không commit** mật khẩu, khóa hay token.

Cấu hình riêng từng máy đặt trong file `src/main/resources/application-local.yml`. File này đã nằm trong `.gitignore`. Xem `application-example.yml` làm mẫu. Ví dụ:

```yaml
app:
  dev:
    default-password: Dev@12345
```

Khóa `app.dev.default-password` đặt mật khẩu mặc định cho các tài khoản mẫu và Admin chưa có mật khẩu, để đăng nhập thử khi chưa cấu hình email. Chỉ dùng cho máy phát triển.

| Biến môi trường | Ý nghĩa | Mặc định |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Kết nối MySQL | `localhost`, `3307`, `bds_broker` |
| `DB_USERNAME`, `DB_PASSWORD` | Tài khoản MySQL | `root`, `root` |
| `JWT_SECRET` | Khóa ký JWT, từ 32 ký tự. **Bắt buộc đặt ở môi trường thật** | giá trị dùng riêng cho máy dev |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP gửi email | Gmail, cổng 587, để trống |
| `MAIL_FROM` | Địa chỉ gửi email | `no-reply@realia.local` |
| `FRONTEND_URL` | Địa chỉ frontend dùng trong link email | `http://localhost:5173` |
| `ADMIN_EMAIL` | Email của Admin tạo ở lần chạy đầu | `admin@realia.local` |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Lưu ảnh | để trống |

## Tài khoản để thử

- **Admin**: lần chạy đầu, nếu chưa có Admin thì app tự tạo tài khoản `admin` (nhân viên, `la_admin = true`) và gửi lời mời đặt mật khẩu tới `ADMIN_EMAIL`.
- **Tài khoản mẫu** (từ V2): nhân viên `nv101` đến `nv106`, khách hàng `kh201` đến `kh205`. Các tài khoản này chưa có mật khẩu, nên đặt `app.dev.default-password` trong `application-local.yml` rồi chạy lại app để có mật khẩu.
- Cách dùng Swagger: gọi `POST /api/v1/auth/login`, copy chuỗi `token` (**không** copy dấu nháy kép), bấm **Authorize** và dán token vào.

## Cấu trúc mã nguồn

Chia theo module nghiệp vụ (package-by-feature) dưới `com.codegym.bds_backend`. Mỗi module tự chứa Controller, Service, Repository, DTO và Entity của mình.

```
src/main/java/com/codegym/bds_backend/
├── auth/          đăng nhập, đăng ký, JWT, tài khoản, email xác thực
├── nhanvien/      nhân viên
├── khachhang/     khách hàng
├── batdongsan/    bất động sản, hình ảnh
├── nhucau/        nhu cầu mua
├── giaodich/      giao dịch
├── thongbao/      thông báo do Admin soạn
├── bieumau/       biểu mẫu
├── common/        dùng chung: response, exception, enums, util
└── config/        cấu hình: bảo mật, OpenAPI, Cloudinary, thuộc tính
```

Entity, enum và repository của cả 10 bảng đã có sẵn trong các module tương ứng. Khi làm module của mình, thêm Controller, Service và DTO vào đúng package.

## Quy tắc khi viết code

- Đặt tên kỹ thuật bằng tiếng Anh, thuật ngữ nghiệp vụ theo tên bảng tiếng Việt không dấu. Tên hàm: động từ tiếng Anh + danh từ nghiệp vụ (`deleteKhachHang`, `findByHoTen`). Log và thông báo cho người dùng viết tiếng Việt có dấu.
- Lùi đầu dòng 4 khoảng trắng, tối đa 120 ký tự mỗi dòng. Không để comment, `System.out.println` hay code chết trong code đã commit. Dùng `@Slf4j` để ghi log.
- Controller chỉ nhận request, gọi Service và trả DTO. Không chứa nghiệp vụ, không gọi Repository trực tiếp. Service gắn `@Transactional` (hàm chỉ đọc dùng `readOnly = true`).
- Tiêm phụ thuộc bằng constructor (`@RequiredArgsConstructor`), cấm `@Autowired` trên field. Không dùng `@Data` trên Entity.
- Không trả Entity ra API, luôn dùng DTO. Mọi API trả cùng khuôn:

```json
{ "success": true, "data": {}, "message": "...", "errors": [{ "field": "...", "message": "..." }] }
```

  Dùng `ApiResponse.ok(...)` trong `common/response`.
- Lỗi nghiệp vụ: ném `BusinessException`, `NotFoundException` hoặc `BadRequestException` từ Service, `GlobalExceptionHandler` sẽ gói lại đúng khuôn.
- Số điện thoại Việt Nam: `@NotBlank` kèm `@Pattern(regexp = "^0[0-9]{9}$")` (đúng 10 số).
- Enum lưu bằng `EnumType.STRING`. Tiền tệ dùng `BigDecimal` (VND). Quan hệ JPA dùng `FetchType.LAZY`. Truy vấn tự viết dùng `@Query` có tham số, cấm nối chuỗi SQL.
- Xóa mềm bằng cột `deleted_at` (`@SQLDelete` và `@SQLRestriction`).
- Mã hiển thị (`KH012`, `BDS045`, `GD001`...) tạo sau khi lưu bằng `MaHienThi.format(...)`. API dùng `id` số làm khóa.
- URL: tiền tố `/api/v1`, danh từ số nhiều, chữ thường, gạch ngang (ví dụ `/api/v1/bat-dong-san`). Mỗi endpoint có `@Operation` tiếng Việt.
- Tin đăng bất động sản **không có bước duyệt**. Trạng thái chỉ gồm `CO_SAN` và `DA_BAN`. Khi có tin mới, hệ thống gửi email thông báo cho Admin bằng `@Async`.

## Phân quyền

Ba vai trò: `ROLE_ADMIN`, `ROLE_NHAN_VIEN`, `ROLE_KHACH_HANG`. Quyền Admin lấy từ cột `nhan_vien.la_admin`.

- Khai báo quyền trên endpoint bằng `@PreAuthorize`, ví dụ `@PreAuthorize("hasRole('ADMIN')")`.
- Lấy người đang gọi API: `@AuthenticationPrincipal JwtPrincipal principal` (có `userId` là id tài khoản, `username`, `roles`).
- Endpoint công khai phải được liệt kê trong `config/SecurityConfig`. Mặc định mọi endpoint khác đều yêu cầu đăng nhập.
- Tạo tài khoản cho hồ sơ mới: gọi `TaiKhoanService.createTaiKhoanChoNhanVien` hoặc `createTaiKhoanChoKhachHang`. Hệ thống tự gửi email lời mời đặt mật khẩu.

## Quy trình Git

Chi tiết xem [git-flow.md](git-flow.md). Tóm tắt:

1. Luôn tách nhánh từ `develop` mới nhất: `git checkout develop && git pull`, rồi `git checkout -b feature/#<issue>_<mo-ta-ngan>`.
2. Commit có tiền tố: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`.
3. Không commit trực tiếp vào `main` hay `develop`. Mọi thay đổi đi qua Pull Request vào `develop`, cần ít nhất 1 người khác approve.
4. Trước khi tạo PR:
   - `./mvnw clean verify` phải báo `BUILD SUCCESS`.
   - Chạy thử app và kiểm tra API bằng Swagger.
   - Tự đọc lại diff, xóa code thừa và comment.
   - Chụp ảnh Swagger làm bằng chứng đưa vào mô tả PR.
5. Không `force push` lên nhánh chung.

## Lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| Cổng 3307 hoặc 8080 đã bị chiếm | Tắt chương trình đang dùng cổng, hoặc đổi cổng qua biến môi trường |
| App dừng với lỗi `Schema-validation` | Entity lệch với V1, hoặc database cũ chưa tạo lại. Chạy `docker compose down -v && docker compose up -d` rồi thử lại |
| Đăng nhập báo "chưa đặt mật khẩu" | Chưa có `app.dev.default-password` trong `application-local.yml`, hoặc chưa chạy lại app sau khi thêm |
| Gọi API báo 401 dù đã Authorize | Token dán thừa dấu nháy kép, hoặc đã hết hạn (1 ngày) |
| Email không gửi được | Chưa cấu hình SMTP. Lỗi gửi email chỉ ghi log, không làm hỏng thao tác chính |

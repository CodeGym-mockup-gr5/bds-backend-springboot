# Database dự án Quản lý trung tâm môi giới BĐS

MySQL 8.0 chạy bằng Docker. Mọi thành viên dùng cùng cấu hình để tránh lệch môi trường.

## Yêu cầu
- Cài Docker Desktop và chờ đến khi báo "Engine running".
- Windows: Docker Desktop cần WSL 2 (trình cài đặt sẽ hướng dẫn).

## Cài đặt lần đầu
Mở Terminal (PowerShell trên Windows) trong thư mục này (thư mục có `docker-compose.yml` và thư mục con `init`). Cách nhanh: mở thư mục trong File Explorer, bấm vào thanh địa chỉ, gõ `powershell` rồi Enter. Sau đó chạy:

    docker compose up -d

Lần đầu Docker tải image (khoảng 0,5–1 GB) và tự chạy các file trong `init/`:
1. `01_V1__init_schema.sql`: tạo bảng, ràng buộc, view (khớp Logical Data Model, 21 quyết định A01–A21).
2. `02_V2__sample_data.sql`: dữ liệu mẫu (chỉ dùng khi phát triển).

Chờ khoảng 30 giây rồi kiểm tra:

    docker compose ps

Cột STATUS ghi `Up ... (healthy)` là xong.

## Thông tin kết nối
| Mục | Giá trị |
|---|---|
| Host | localhost |
| Port | **3307** |
| Database | bds_broker |
| User | root |
| Password | root |
| JDBC URL | `jdbc:mysql://localhost:3307/bds_broker` |

Mật khẩu `root` chỉ dùng trên máy cá nhân khi phát triển. Không dùng cho môi trường thật.

## Kiểm tra dữ liệu

    docker exec bds-mysql mysql -uroot -proot --default-character-set=utf8mb4 bds_broker -e "SHOW TABLES; SELECT ma_bds, ngay_dang FROM bat_dong_san; SELECT ma_nv, la_admin FROM nhan_vien;"

Kết quả đúng: 10 bảng + 1 view (`v_thong_ke_thang`), 12 bất động sản có cột `ngay_dang`, 6 nhân viên.

## Lệnh thường dùng
| Việc cần làm | Lệnh |
|---|---|
| Tắt (giữ dữ liệu) | `docker compose stop` |
| Bật lại | `docker compose start` |
| Xóa sạch và nạp lại từ đầu | `docker compose down -v` rồi `docker compose up -d` |
| Xem log khi lỗi | `docker compose logs mysql` |

## Khi file SQL trong init/ được cập nhật
Mỗi khi nhóm sửa `01_V1__init_schema.sql` hoặc `02_V2__sample_data.sql` (ví dụ sau khi đổi Logical Data Model), mỗi thành viên làm lại như sau:
1. Lấy bản mới của thư mục này (pull từ Git hoặc giải nén lại file zip nhóm gửi).
2. Chạy `docker compose down -v` rồi `docker compose up -d`.
3. Kiểm tra lại bằng lệnh ở mục "Kiểm tra dữ liệu".

Lần cập nhật gần nhất (issue #34): bảng `khach_hang` thêm cột `trang_thai_duyet`. Phải tạo lại database bằng `docker compose down -v` rồi `docker compose up -d`, nếu không backend sẽ báo lỗi validate schema khi khởi động.

Lệnh `down -v` xóa toàn bộ dữ liệu trong container. Chấp nhận được vì đây chỉ là dữ liệu mẫu.

## Lưu ý
- File trong `init/` chỉ chạy khi database còn trống. Sửa file SQL xong muốn áp dụng thì phải chạy `docker compose down -v` (mất toàn bộ dữ liệu trong container) rồi `up -d` lại.
- Port 3307 bị chiếm: sửa dòng `"3307:3306"` trong `docker-compose.yml` thành cổng khác, và đổi cả JDBC URL tương ứng.
- Lỗi `Conflict. The container name "/bds-mysql" is already in use`, `port is already allocated` hoặc `name already in use`: container cũ tên `bds-mysql` còn tồn tại (thường do từng chạy từ thư mục khác). Chạy `docker rm -f bds-mysql` rồi `docker compose up -d` lại.
- Khi backend Spring Boot dùng Flyway để tạo bảng, bỏ mount `init/` trong `docker-compose.yml` (để Flyway tự quản lý), tránh bảng bị tạo hai lần.
- Tài khoản mẫu chưa có mật khẩu nên chưa đăng nhập được. Tài khoản Admin do backend tạo ở lần chạy đầu (đặt `nhan_vien.la_admin = TRUE`).

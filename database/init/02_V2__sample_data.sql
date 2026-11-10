-- =====================================================================
-- DU LIEU MAU CHO MOI TRUONG PHAT TRIEN (dev) - KHONG chay tren production.
-- Dat trong thu muc db/dev, khong nam trong db/migration cua Flyway.
-- Id bat dau tu 101/201/... de khong dung voi tai khoan Admin do backend
-- tao o lan chay dau tien. Cac tai khoan mau chua co mat khau (mat_khau_hash
-- NULL): dat mat khau qua luong email hoac gan ban bam BCrypt thu cong.
-- Chay SAU V1__init_schema.sql. Cac cot tu tinh (gia_m2, loi_nhuan,
-- bds_da_ban_id) khong duoc chen gia tri.
-- Khop LDM: ma hien thi KH201/BDS301/NC401/GD501/HS001 (A01), bat_dong_san co
-- ngay_dang (A20), khong con nguoi_dang_id, tai_khoan khong con vai_tro.
-- =====================================================================
SET NAMES utf8mb4;

INSERT INTO nhan_vien (id, ma_nv, ho_ten, ngay_sinh, gioi_tinh, dia_chi, so_dien_thoai, email, chuc_vu) VALUES
  (101, 'MNV-101', 'Nguyễn Thành Long', '1988-03-14', 'NAM', '12 ngõ 34 đường Nguyễn Trãi, phường Thượng Đình, quận Thanh Xuân, Hà Nội', '0912345601', 'long.nt@example.com', 'TRUONG_PHONG'),
  (102, 'MNV-102', 'Trần Thu Hà', '1992-07-02', 'NU', '45 đường Lê Văn Sỹ, phường 14, quận 3, TP. Hồ Chí Minh', '0912345602', 'ha.tt@example.com', 'KE_TOAN'),
  (103, 'MNV-103', 'Lê Quang Huy', '1995-11-20', 'NAM', '78 đường Trần Phú, phường Hải Châu 1, quận Hải Châu, Đà Nẵng', '0912345603', 'huy.lq@example.com', 'NHAN_VIEN'),
  (104, 'MNV-104', 'Phạm Ngọc Mai', '1996-01-09', 'NU', '23 đường Nguyễn Văn Cừ, phường An Hòa, quận Ninh Kiều, Cần Thơ', '0912345604', 'mai.pn@example.com', 'NHAN_VIEN'),
  (105, 'MNV-105', 'Vũ Đức Thắng', '1993-09-30', 'NAM', '9 đường Điện Biên Phủ, phường Máy Chai, quận Ngô Quyền, Hải Phòng', '0912345605', 'thang.vd@example.com', 'NHAN_VIEN'),
  (106, 'MNV-106', 'Đặng Hồng Nhung', '1998-05-25', 'NU', '56 đường Hùng Vương, phường Phước Ninh, quận Hải Châu, Đà Nẵng', '0912345606', 'nhung.dh@example.com', 'NHAN_VIEN');

INSERT INTO khach_hang (id, ma_kh, ho_ten, la_kh_mua, la_kh_ban, ngay_sinh, gioi_tinh, dia_chi, so_dien_thoai, email, so_cccd) VALUES
  (201, 'KH201', 'Nguyễn Văn An', FALSE, TRUE, '1980-02-11', 'NAM', '15 đường Láng Hạ, phường Thành Công, quận Ba Đình, Hà Nội', '0987000201', 'an.nv@example.com', '001080000201'),
  (202, 'KH202', 'Trần Thị Bình', FALSE, TRUE, '1985-06-18', 'NU', '102 đường Cách Mạng Tháng Tám, phường 5, quận 3, TP. Hồ Chí Minh', '0987000202', 'binh.tt@example.com', '079085000202'),
  (203, 'KH203', 'Lê Minh Châu', TRUE, TRUE, '1990-12-05', 'NU', '34 đường Lê Duẩn, phường Chính Gián, quận Thanh Khê, Đà Nẵng', '0987000203', 'chau.lm@example.com', '048090000203'),
  (204, 'KH204', 'Phạm Quốc Dũng', FALSE, TRUE, '1978-08-22', 'NAM', '7 đường Trần Hưng Đạo, phường Tân An, quận Ninh Kiều, Cần Thơ', '0987000204', 'dung.pq@example.com', '092078000204'),
  (205, 'KH205', 'Hoàng Thị Em', FALSE, TRUE, '1983-04-03', 'NU', '88 đường Lạch Tray, phường Đằng Giang, quận Ngô Quyền, Hải Phòng', '0987000205', 'em.ht@example.com', '031083000205'),
  (206, 'KH206', 'Vũ Văn Phúc', TRUE, FALSE, '1991-10-17', 'NAM', '21 đường Giải Phóng, phường Phương Mai, quận Đống Đa, Hà Nội', '0987000206', 'phuc.vv@example.com', '001091000206'),
  (207, 'KH207', 'Đặng Thu Giang', TRUE, FALSE, '1994-03-29', 'NU', '66 đường Nguyễn Thị Minh Khai, phường Bến Nghé, quận 1, TP. Hồ Chí Minh', '0987000207', 'giang.dt@example.com', '079094000207'),
  (208, 'KH208', 'Bùi Anh Hùng', TRUE, FALSE, '1987-07-14', 'NAM', '5 đường Nguyễn Huệ, phường Vĩnh Ninh, thành phố Huế', '0987000208', NULL, '046087000208'),
  (209, 'KH209', 'Ngô Thị Lan', TRUE, FALSE, '1996-09-08', 'NU', '19 đường Bạch Đằng, phường Hải Châu 2, quận Hải Châu, Đà Nẵng', '0987000209', 'lan.nt@example.com', NULL),
  (210, 'KH210', 'Đỗ Minh Khoa', TRUE, TRUE, '1982-01-27', 'NAM', '40 đường Hoàng Diệu, phường 12, quận 4, TP. Hồ Chí Minh', '0987000210', 'khoa.dm@example.com', '079082000210');

INSERT INTO tai_khoan (id, ten_dang_nhap, nhan_vien_id, khach_hang_id) VALUES
  (101, 'nv101', 101, NULL),
  (102, 'nv102', 102, NULL),
  (103, 'nv103', 103, NULL),
  (104, 'nv104', 104, NULL),
  (105, 'nv105', 105, NULL),
  (106, 'nv106', 106, NULL),
  (201, 'kh201', NULL, 201),
  (202, 'kh202', NULL, 202),
  (203, 'kh203', NULL, 203),
  (204, 'kh204', NULL, 204),
  (205, 'kh205', NULL, 205);

INSERT INTO bat_dong_san (id, ma_bds, khach_hang_id, loai_bds, dia_chi, vi_tri, huong, dien_tich, gia, phap_ly, tinh_trang, ngay_dang, ghi_chu) VALUES
  (301, 'BDS301', 201, 'DAT', 'Lô 12 đường Nguyễn Chí Thanh, quận Đống Đa, Hà Nội', 'TRUNG_TAM', 'DONG', 80, 2400000000, 'Sổ đỏ chính chủ', 'DA_BAN', '2026-05-20', 'Gần công viên'),
  (302, 'BDS302', 202, 'NHA_O', 'Số 8 đường Phạm Văn Đồng, TP. Thủ Đức, TP. Hồ Chí Minh', 'NGOAI_O', 'TAY', 120, 3600000000, 'Sổ hồng riêng', 'DA_BAN', '2026-05-28', 'Nhà 3 tầng'),
  (303, 'BDS303', 203, 'DAT', 'Lô B5 khu đô thị Hòa Xuân, quận Cẩm Lệ, Đà Nẵng', 'NGOAI_O', 'NAM', 150, 1800000000, 'Sổ đỏ chính chủ', 'CO_SAN', '2026-07-15', 'Đường 10,5m'),
  (304, 'BDS304', 204, 'NHA_O', 'Số 22 đường Hai Bà Trưng, quận Ninh Kiều, Cần Thơ', 'TRUNG_TAM', 'BAC', 65, 4550000000, 'Sổ hồng riêng', 'CO_SAN', '2026-08-05', 'Mặt tiền kinh doanh'),
  (305, 'BDS305', 205, 'DAT', 'Lô 3 đường Lê Hồng Phong, quận Ngô Quyền, Hải Phòng', 'TRUNG_TAM', 'DONG_NAM', 100, 5000000000, 'Sổ đỏ chính chủ', 'DA_BAN', '2026-06-10', NULL),
  (306, 'BDS306', 201, 'NHA_O', 'Số 31 đường Tố Hữu, quận Hà Đông, Hà Nội', 'NGOAI_O', 'TAY_BAC', 90, 2700000000, 'Hợp đồng mua bán', 'CO_SAN', '2026-08-22', 'Nhà mới xây'),
  (307, 'BDS307', 202, 'DAT', 'Thửa 45 xã Phước Kiển, huyện Nhà Bè, TP. Hồ Chí Minh', 'NGOAI_O', 'DONG_BAC', 200, 2400000000, 'Sổ đỏ chính chủ', 'CO_SAN', '2026-09-02', NULL),
  (308, 'BDS308', 203, 'NHA_O', 'Số 17 đường Nguyễn Văn Linh, quận Hải Châu, Đà Nẵng', 'TRUNG_TAM', 'TAY_NAM', 75, 3900000000, 'Sổ hồng riêng', 'CO_SAN', '2026-09-18', 'Gần biển'),
  (309, 'BDS309', 210, 'DAT', 'Lô 9 đường Võ Văn Kiệt, quận 5, TP. Hồ Chí Minh', 'TRUNG_TAM', 'NAM', 60, 3000000000, 'Sổ đỏ chính chủ', 'CO_SAN', '2026-07-01', NULL),
  (310, 'BDS310', 210, 'NHA_O', 'Số 14 đường Nguyễn Duy Trinh, TP. Thủ Đức, TP. Hồ Chí Minh', 'NGOAI_O', 'DONG', 110, 3300000000, 'Sổ hồng riêng', 'DA_BAN', '2026-07-20', NULL),
  (311, 'BDS311', 204, 'DAT', 'Lô 21 khu vực Ô Môn, quận Ô Môn, Cần Thơ', 'NGOAI_O', 'BAC', 130, 1560000000, 'Sổ đỏ chính chủ', 'CO_SAN', '2026-10-05', NULL),
  (312, 'BDS312', 205, 'NHA_O', 'Số 5 đường Điện Biên Phủ, quận Hồng Bàng, Hải Phòng', 'TRUNG_TAM', 'DONG', 55, 3850000000, 'Sổ hồng riêng', 'CO_SAN', '2026-10-12', 'Nhà 4 tầng');

INSERT INTO hinh_anh_bds (bat_dong_san_id, duong_dan, thu_tu) VALUES
  (301, '/uploads/bds/301-1.jpg', 0),
  (301, '/uploads/bds/301-2.jpg', 1),
  (302, '/uploads/bds/302-1.jpg', 0),
  (303, '/uploads/bds/303-1.jpg', 0);

INSERT INTO nhu_cau (id, ma_nc, khach_hang_id, loai_bds, vi_tri, dien_tich_min, dien_tich_max, ghi_chu) VALUES
  (401, 'NC401', 206, 'DAT', 'TRUNG_TAM', 70, 100, 'Gần trường học'),
  (402, 'NC402', 207, 'NHA_O', 'NGOAI_O', 100, 130, NULL),
  (403, 'NC403', 208, 'DAT', 'NGOAI_O', 120, 200, NULL),
  (404, 'NC404', 209, 'NHA_O', 'TRUNG_TAM', 60, 80, 'Ưu tiên có sân đỗ xe'),
  (405, 'NC405', 203, 'NHA_O', 'NGOAI_O', 80, 100, NULL),
  (406, 'NC406', 210, 'DAT', 'TRUNG_TAM', 50, 70, NULL);

INSERT INTO giao_dich (id, ma_gd, nhan_vien_id, kh_mua_id, kh_ban_id, bat_dong_san_id, so_tien, ngay_gd, ty_le_hoa_hong, mo_ta, tinh_trang) VALUES
  (501, 'GD501', 101, 206, 201, 301, 2400000000, '2026-06-12', 1.5, 'Mua đất Đống Đa', 'THANH_CONG'),
  (502, 'GD502', 102, 207, 202, 302, 3600000000, '2026-06-25', 1.5, 'Mua nhà Thủ Đức', 'THANH_CONG'),
  (503, 'GD503', 101, 208, 205, 305, 5000000000, '2026-07-08', 1.0, 'Mua đất Ngô Quyền', 'THANH_CONG'),
  (504, 'GD504', 103, 209, 210, 310, 3300000000, '2026-08-19', 2.0, 'Mua nhà Thủ Đức', 'THANH_CONG'),
  (505, 'GD505', 102, 206, 203, 303, 1800000000, '2026-09-03', 1.5, 'Đang thương lượng giá', 'DANG_XU_LY'),
  (506, 'GD506', 104, 207, 204, 304, 4550000000, '2026-09-21', 1.0, 'Khách rút lui', 'THAT_BAI'),
  (507, 'GD507', 101, 208, 201, 306, 2700000000, '2026-10-10', 1.5, 'Đang làm thủ tục', 'DANG_XU_LY'),
  (508, 'GD508', 105, 209, 203, 308, 3900000000, '2026-10-28', 1.2, 'Đang xem nhà', 'DANG_XU_LY');

INSERT INTO thong_bao (ngay, ten_thong_bao, noi_dung, nguoi_tao_id) VALUES
  ('2026-10-01', 'Lịch nghỉ lễ cuối năm', 'Trung tâm thông báo lịch nghỉ lễ và sắp xếp trực văn phòng cho nhân viên.', 101),
  ('2026-10-15', 'Cập nhật tỷ lệ hoa hồng', 'Tỷ lệ hoa hồng áp dụng cho giao dịch mới được điều chỉnh theo quy định nội bộ.', 101),
  ('2026-10-30', 'Họp tổng kết quý 3', 'Toàn bộ nhân viên tham dự buổi họp tổng kết quý 3 tại phòng họp tầng 2.', 101);

INSERT INTO bieu_mau (id, ma_bm, ten_bieu_mau, noi_dung, duong_dan_tep) VALUES
  (1, 'HS001', 'Hợp đồng đặt cọc', 'Mẫu hợp đồng đặt cọc mua bán bất động sản', '/files/bieu-mau/hop-dong-dat-coc.docx'),
  (2, 'HS002', 'Hợp đồng ủy quyền', 'Mẫu hợp đồng ủy quyền môi giới', '/files/bieu-mau/hop-dong-uy-quyen.docx'),
  (3, 'HS003', 'Phiếu yêu cầu khách hàng', 'Mẫu ghi nhận nhu cầu của khách hàng', '/files/bieu-mau/phieu-yeu-cau.docx');

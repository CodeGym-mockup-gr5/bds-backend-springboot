-- =====================================================================
-- He thong Quan ly trung tam moi gioi bat dong san
-- Schema MySQL 8.0.16+ (CHECK constraint duoc thuc thi tu ban 8.0.16)
-- Nguon: SRS v4.0 + Logical Data Model (21 quyet dinh A01-A21 da chot, 09/11/2026)
--   Khop Logical ERD: 10 bang, khong con created_at/updated_at (A14).
--
-- Tao database truoc khi chay (Flyway/Spring Boot se chay file nay):
--   CREATE DATABASE bds_broker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--
-- Quy uoc:
--   * Khoa chinh la id BIGINT tu tang; ma hien thi (MNV-1, KH012, BDS045, NC007,
--     GD001, HS001; toi thieu 3 chu so, A01) do ung dung sinh ngay sau khi insert,
--     trong cung transaction (Q06). Cot ma_* cho phep NULL o muc DB; bat buoc co ma
--     la quy tac cua ung dung.
--   * Xoa mem: cot deleted_at, NULL = dang hoat dong (Q24). Chi co o nhan_vien,
--     khach_hang, bat_dong_san, giao_dich, thong_bao, bieu_mau (NgayXoa trong LDM).
--   * Gia tri liet ke luu ma ASCII de map voi Java enum (@Enumerated STRING) (Q25).
--   * Tien luu bang VND, khong co phan le (Q26).
-- =====================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- Nhan vien (SE2103). Quyen Admin: cot la_admin (LDM E01).
-- ---------------------------------------------------------------------
CREATE TABLE nhan_vien (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ma_nv           VARCHAR(20)  NULL     COMMENT 'MNV-<id>, ung dung sinh sau khi insert (Q06)',
    ho_ten          VARCHAR(45)  NOT NULL,
    ngay_sinh       DATE         NOT NULL,
    gioi_tinh       VARCHAR(3)   NOT NULL,
    dia_chi         VARCHAR(255) NOT NULL,
    so_dien_thoai   VARCHAR(10)  NOT NULL,
    email           VARCHAR(254) NOT NULL,
    chuc_vu         VARCHAR(20)  NOT NULL,
    la_admin        BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      DATETIME     NULL,
    CONSTRAINT pk_nhan_vien PRIMARY KEY (id),
    CONSTRAINT uq_nhan_vien_ma UNIQUE (ma_nv),
    CONSTRAINT uq_nhan_vien_email UNIQUE (email),
    CONSTRAINT ck_nhan_vien_gioi_tinh CHECK (gioi_tinh IN ('NAM', 'NU')),
    CONSTRAINT ck_nhan_vien_sdt CHECK (so_dien_thoai REGEXP '^0[0-9]{9}$'),
    CONSTRAINT ck_nhan_vien_chuc_vu CHECK (chuc_vu IN ('TRUONG_PHONG', 'KE_TOAN', 'NHAN_VIEN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Khach hang (SE2105). Mot khach hang co the vua mua vua ban (Q07).
-- ---------------------------------------------------------------------
CREATE TABLE khach_hang (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ma_kh           VARCHAR(20)  NULL     COMMENT 'KH<id>, toi thieu 3 chu so (A01)',
    ho_ten          VARCHAR(45)  NOT NULL,
    la_kh_mua       BOOLEAN      NOT NULL DEFAULT FALSE,
    la_kh_ban       BOOLEAN      NOT NULL DEFAULT FALSE,
    ngay_sinh       DATE         NULL,
    gioi_tinh       VARCHAR(3)   NULL,
    dia_chi         VARCHAR(255) NULL,
    so_dien_thoai   VARCHAR(10)  NOT NULL,
    email           VARCHAR(254) NULL,
    so_cccd         CHAR(12)     NULL,
    deleted_at      DATETIME     NULL,
    CONSTRAINT pk_khach_hang PRIMARY KEY (id),
    CONSTRAINT uq_khach_hang_ma UNIQUE (ma_kh),
    CONSTRAINT uq_khach_hang_cccd UNIQUE (so_cccd),
    CONSTRAINT ck_khach_hang_gioi_tinh CHECK (gioi_tinh IS NULL OR gioi_tinh IN ('NAM', 'NU')),
    CONSTRAINT ck_khach_hang_sdt CHECK (so_dien_thoai REGEXP '^0[0-9]{9}$'),
    CONSTRAINT ck_khach_hang_cccd CHECK (so_cccd IS NULL OR so_cccd REGEXP '^[0-9]{12}$')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_khach_hang_sdt ON khach_hang (so_dien_thoai);

-- ---------------------------------------------------------------------
-- Tai khoan dang nhap cho ca nhan vien va khach hang.
-- Nhan vien va khach hang deu dang nhap bang ten_dang_nhap (duy nhat).
-- Khach hang co the chua co tai khoan (A03). Khong co cot email: quen mat khau
-- tim theo ten dang nhap roi gui den nhan_vien.email (A02).
-- Admin tao tai khoan nhan vien -> he thong gui email (ten dang nhap + lien ket
-- dat mat khau) -> nhan vien tu dat mat khau (token_xac_thuc, THIET_LAP_MAT_KHAU).
-- Khoa sau 5 lan sai, mo lai bang Quen mat khau (Q29).
-- ---------------------------------------------------------------------
CREATE TABLE tai_khoan (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    ten_dang_nhap         VARCHAR(50)  NOT NULL,
    mat_khau_hash         VARCHAR(255) NULL     COMMENT 'BCrypt; NULL cho den khi thiet lap qua email (Q05)',
    nhan_vien_id          BIGINT       NULL,
    khach_hang_id         BIGINT       NULL,
    so_lan_dang_nhap_sai  TINYINT      NOT NULL DEFAULT 0,
    bi_khoa               BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_tai_khoan PRIMARY KEY (id),
    CONSTRAINT uq_tai_khoan_ten UNIQUE (ten_dang_nhap),
    CONSTRAINT uq_tai_khoan_nhan_vien UNIQUE (nhan_vien_id),
    CONSTRAINT uq_tai_khoan_khach_hang UNIQUE (khach_hang_id),
    CONSTRAINT fk_tai_khoan_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien (id),
    CONSTRAINT fk_tai_khoan_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang (id),
    -- Chu tai khoan: dung mot trong hai (XOR). Admin = nhan_vien.la_admin.
    CONSTRAINT ck_tai_khoan_chu_so_huu CHECK (
        (nhan_vien_id IS NOT NULL AND khach_hang_id IS NULL)
        OR (nhan_vien_id IS NULL AND khach_hang_id IS NOT NULL)
    ),
    CONSTRAINT ck_tai_khoan_so_lan_sai CHECK (so_lan_dang_nhap_sai BETWEEN 0 AND 5)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Token gui qua email: thiet lap mat khau lan dau (Q05) va dat lai mat khau (Q29).
-- Chi luu ban bam SHA-256 cua token, khong luu token goc.
-- ---------------------------------------------------------------------
CREATE TABLE token_xac_thuc (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tai_khoan_id    BIGINT       NOT NULL,
    token_hash      CHAR(64)     NOT NULL,
    loai            VARCHAR(30)  NOT NULL,
    het_han_luc     DATETIME     NOT NULL,
    da_dung_luc     DATETIME     NULL,
    CONSTRAINT pk_token_xac_thuc PRIMARY KEY (id),
    CONSTRAINT uq_token_xac_thuc_hash UNIQUE (token_hash),
    CONSTRAINT fk_token_tai_khoan FOREIGN KEY (tai_khoan_id) REFERENCES tai_khoan (id),
    CONSTRAINT ck_token_loai CHECK (loai IN ('THIET_LAP_MAT_KHAU', 'DAT_LAI_MAT_KHAU'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Bat dong san (SE2106). khach_hang_id la chu so huu = ben ban (A15); khong con
-- nguoi_dang_id. ngay_dang la ngay dang tin (A20). Them anh va phap ly (Q22, phuong an C).
-- ---------------------------------------------------------------------
CREATE TABLE bat_dong_san (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    ma_bds          VARCHAR(20)   NULL     COMMENT 'BDS<id>, toi thieu 3 chu so (A01)',
    khach_hang_id   BIGINT        NOT NULL COMMENT 'Chu so huu',
    loai_bds        VARCHAR(10)   NOT NULL,
    dia_chi         VARCHAR(255)  NOT NULL,
    vi_tri          VARCHAR(10)   NOT NULL,
    huong           VARCHAR(10)   NOT NULL,
    dien_tich       DECIMAL(10,2) NOT NULL COMMENT 'm2',
    gia             DECIMAL(15,0) NOT NULL COMMENT 'VND',
    gia_m2          DECIMAL(15,0) GENERATED ALWAYS AS (ROUND(gia / dien_tich)) STORED COMMENT 'VND/m2, tu tinh (Q26)',
    phap_ly         VARCHAR(255)  NULL,
    tinh_trang      VARCHAR(10)   NOT NULL DEFAULT 'CO_SAN',
    ngay_dang       DATE          NOT NULL COMMENT 'Ngay dang tin (A20)',
    ghi_chu         VARCHAR(50)   NULL,
    deleted_at      DATETIME      NULL,
    CONSTRAINT pk_bat_dong_san PRIMARY KEY (id),
    CONSTRAINT uq_bat_dong_san_ma UNIQUE (ma_bds),
    CONSTRAINT fk_bds_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang (id),
    CONSTRAINT ck_bds_loai CHECK (loai_bds IN ('DAT', 'NHA_O')),
    CONSTRAINT ck_bds_vi_tri CHECK (vi_tri IN ('TRUNG_TAM', 'NGOAI_O')),
    CONSTRAINT ck_bds_huong CHECK (huong IN ('DONG', 'TAY', 'NAM', 'BAC',
                                              'DONG_BAC', 'DONG_NAM', 'TAY_BAC', 'TAY_NAM')),
    CONSTRAINT ck_bds_dien_tich CHECK (dien_tich > 0),
    CONSTRAINT ck_bds_gia CHECK (gia >= 0),
    CONSTRAINT ck_bds_tinh_trang CHECK (tinh_trang IN ('CO_SAN', 'DA_BAN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Phuc vu tim kiem va quy tac "phu hop" (Q09)
CREATE INDEX ix_bds_tim_kiem ON bat_dong_san (loai_bds, vi_tri, tinh_trang, dien_tich);

CREATE TABLE hinh_anh_bds (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    bat_dong_san_id BIGINT       NOT NULL,
    duong_dan       VARCHAR(500) NOT NULL,
    thu_tu          INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_hinh_anh_bds PRIMARY KEY (id),
    CONSTRAINT fk_hinh_anh_bds FOREIGN KEY (bat_dong_san_id) REFERENCES bat_dong_san (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Nhu cau mua (SE2107). Bo loai nhu cau vi chi con Mua (Q08).
-- Dien tich la khoang Min-Max (Q23).
-- ---------------------------------------------------------------------
CREATE TABLE nhu_cau (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    ma_nc           VARCHAR(20)   NULL     COMMENT 'NC<id>, toi thieu 3 chu so (A01)',
    khach_hang_id   BIGINT        NOT NULL,
    loai_bds        VARCHAR(10)   NOT NULL,
    vi_tri          VARCHAR(10)   NOT NULL,
    dien_tich_min   DECIMAL(10,2) NOT NULL COMMENT 'm2',
    dien_tich_max   DECIMAL(10,2) NOT NULL COMMENT 'm2',
    ghi_chu         VARCHAR(50)   NULL,
    deleted_at      DATETIME      NULL,
    CONSTRAINT pk_nhu_cau PRIMARY KEY (id),
    CONSTRAINT uq_nhu_cau_ma UNIQUE (ma_nc),
    CONSTRAINT fk_nhu_cau_khach_hang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang (id),
    CONSTRAINT ck_nhu_cau_loai CHECK (loai_bds IN ('DAT', 'NHA_O')),
    CONSTRAINT ck_nhu_cau_vi_tri CHECK (vi_tri IN ('TRUNG_TAM', 'NGOAI_O')),
    CONSTRAINT ck_nhu_cau_dien_tich CHECK (dien_tich_min > 0 AND dien_tich_max >= dien_tich_min)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Giao dich (SE2108). Loi nhuan = so tien x ty le hoa hong (Q10).
-- Moi BDS chi co toi da 1 giao dich THANH_CONG con hieu luc (chong ban trung, Q11).
-- Viec chuyen BDS sang DA_BAN khi giao dich thanh cong lam o service (Q11).
-- ---------------------------------------------------------------------
CREATE TABLE giao_dich (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    ma_gd             VARCHAR(20)   NULL     COMMENT 'GD<id>, toi thieu 3 chu so (A01)',
    nhan_vien_id      BIGINT        NOT NULL COMMENT 'Nhan vien giao dich',
    kh_mua_id         BIGINT        NOT NULL,
    kh_ban_id         BIGINT        NOT NULL,
    bat_dong_san_id   BIGINT        NOT NULL,
    so_tien           DECIMAL(15,0) NOT NULL COMMENT 'VND',
    ngay_gd           DATE          NOT NULL,
    ty_le_hoa_hong    DECIMAL(5,2)  NOT NULL COMMENT 'Phan tram, vi du 1.00 = 1%',
    loi_nhuan         DECIMAL(15,0) GENERATED ALWAYS AS (ROUND(so_tien * ty_le_hoa_hong / 100)) STORED,
    mo_ta             VARCHAR(255)  NULL,
    tinh_trang        VARCHAR(12)   NOT NULL DEFAULT 'DANG_XU_LY',
    bds_da_ban_id     BIGINT        GENERATED ALWAYS AS (
                          CASE WHEN tinh_trang = 'THANH_CONG' AND deleted_at IS NULL
                               THEN bat_dong_san_id END) STORED,
    deleted_at        DATETIME      NULL,
    CONSTRAINT pk_giao_dich PRIMARY KEY (id),
    CONSTRAINT uq_giao_dich_ma UNIQUE (ma_gd),
    CONSTRAINT uq_giao_dich_bds_da_ban UNIQUE (bds_da_ban_id),
    CONSTRAINT fk_gd_nhan_vien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien (id),
    CONSTRAINT fk_gd_kh_mua FOREIGN KEY (kh_mua_id) REFERENCES khach_hang (id),
    CONSTRAINT fk_gd_kh_ban FOREIGN KEY (kh_ban_id) REFERENCES khach_hang (id),
    CONSTRAINT fk_gd_bat_dong_san FOREIGN KEY (bat_dong_san_id) REFERENCES bat_dong_san (id),
    CONSTRAINT ck_gd_hai_ben CHECK (kh_mua_id <> kh_ban_id),
    CONSTRAINT ck_gd_so_tien CHECK (so_tien > 0),
    CONSTRAINT ck_gd_ty_le CHECK (ty_le_hoa_hong BETWEEN 0 AND 100),
    CONSTRAINT ck_gd_tinh_trang CHECK (tinh_trang IN ('DANG_XU_LY', 'THANH_CONG', 'THAT_BAI'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_giao_dich_ngay ON giao_dich (ngay_gd, tinh_trang);

-- ---------------------------------------------------------------------
-- Thong bao (SE2104)
-- ---------------------------------------------------------------------
CREATE TABLE thong_bao (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ngay            DATE         NOT NULL,
    ten_thong_bao   VARCHAR(255) NOT NULL,
    noi_dung        TEXT         NOT NULL,
    nguoi_tao_id    BIGINT       NOT NULL,
    deleted_at      DATETIME     NULL,
    CONSTRAINT pk_thong_bao PRIMARY KEY (id),
    CONSTRAINT fk_thong_bao_nguoi_tao FOREIGN KEY (nguoi_tao_id) REFERENCES nhan_vien (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Ho so bieu mau (SE2109)
-- ---------------------------------------------------------------------
CREATE TABLE bieu_mau (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    ma_bm           VARCHAR(20)  NULL     COMMENT 'HS<id>, toi thieu 3 chu so (A01)',
    ten_bieu_mau    VARCHAR(255) NOT NULL,
    noi_dung        TEXT         NULL     COMMENT 'Mo ta noi dung bieu mau',
    duong_dan_tep   VARCHAR(500) NOT NULL,
    deleted_at      DATETIME     NULL,
    CONSTRAINT pk_bieu_mau PRIMARY KEY (id),
    CONSTRAINT uq_bieu_mau_ma UNIQUE (ma_bm)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Thong ke theo thang (SE2110, Q10, Q27)
-- ---------------------------------------------------------------------
CREATE VIEW v_thong_ke_thang AS
SELECT
    YEAR(ngay_gd)                                                  AS nam,
    MONTH(ngay_gd)                                                 AS thang,
    COUNT(*)                                                       AS tong_giao_dich,
    SUM(CASE WHEN tinh_trang = 'THANH_CONG' THEN 1 ELSE 0 END)         AS so_gd_thanh_cong,
    SUM(CASE WHEN tinh_trang = 'THANH_CONG' THEN loi_nhuan ELSE 0 END) AS tong_thu_nhap
FROM giao_dich
WHERE deleted_at IS NULL
GROUP BY YEAR(ngay_gd), MONTH(ngay_gd);

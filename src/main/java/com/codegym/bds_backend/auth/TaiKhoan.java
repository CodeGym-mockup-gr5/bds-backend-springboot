package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.nhanvien.NhanVien;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tai_khoan")
@Getter
@Setter
@NoArgsConstructor
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_dang_nhap", nullable = false, length = 50)
    private String tenDangNhap;

    @Column(name = "mat_khau_hash")
    private String matKhauHash;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "so_lan_dang_nhap_sai", nullable = false)
    private Integer soLanDangNhapSai = 0;

    @Column(name = "bi_khoa", nullable = false)
    private boolean biKhoa;
}

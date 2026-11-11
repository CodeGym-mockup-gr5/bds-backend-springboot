package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.common.enums.GioiTinh;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "khach_hang")
@SQLDelete(sql = "UPDATE khach_hang SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_kh", length = 20)
    private String maKh;

    @Column(name = "ho_ten", nullable = false, length = 45)
    private String hoTen;

    @Column(name = "la_kh_mua", nullable = false)
    private boolean laKhMua;

    @Column(name = "la_kh_ban", nullable = false)
    private boolean laKhBan;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "gioi_tinh", length = 3)
    private GioiTinh gioiTinh;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "so_dien_thoai", nullable = false, length = 10)
    private String soDienThoai;

    @Column(name = "email", length = 254)
    private String email;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "so_cccd", length = 12)
    private String soCccd;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

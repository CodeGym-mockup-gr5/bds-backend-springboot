package com.codegym.bds_backend.nhanvien;

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
@Table(name = "nhan_vien")
@SQLDelete(sql = "UPDATE nhan_vien SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_nv", length = 20)
    private String maNv;

    @Column(name = "ho_ten", nullable = false, length = 45)
    private String hoTen;

    @Column(name = "ngay_sinh", nullable = false)
    private LocalDate ngaySinh;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "gioi_tinh", nullable = false, length = 3)
    private GioiTinh gioiTinh;

    @Column(name = "dia_chi", nullable = false)
    private String diaChi;

    @Column(name = "so_dien_thoai", nullable = false, length = 10)
    private String soDienThoai;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "chuc_vu", nullable = false, length = 20)
    private ChucVu chucVu;

    @Column(name = "la_admin", nullable = false)
    private boolean laAdmin;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

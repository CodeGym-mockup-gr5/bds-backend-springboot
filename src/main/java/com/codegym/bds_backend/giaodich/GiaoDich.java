package com.codegym.bds_backend.giaodich;

import com.codegym.bds_backend.batdongsan.BatDongSan;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.nhanvien.NhanVien;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
@Table(name = "giao_dich")
@SQLDelete(sql = "UPDATE giao_dich SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class GiaoDich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_gd", length = 20)
    private String maGd;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kh_mua_id", nullable = false)
    private KhachHang khMua;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kh_ban_id", nullable = false)
    private KhachHang khBan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bat_dong_san_id", nullable = false)
    private BatDongSan batDongSan;

    @Column(name = "so_tien", nullable = false, precision = 15, scale = 0)
    private BigDecimal soTien;

    @Column(name = "ngay_gd", nullable = false)
    private LocalDate ngayGd;

    @Column(name = "ty_le_hoa_hong", nullable = false, precision = 5, scale = 2)
    private BigDecimal tyLeHoaHong;

    @Column(name = "loi_nhuan", insertable = false, updatable = false)
    private BigDecimal loiNhuan;

    @Column(name = "mo_ta")
    private String moTa;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tinh_trang", nullable = false, length = 12)
    private TinhTrangGiaoDich tinhTrang = TinhTrangGiaoDich.DANG_XU_LY;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

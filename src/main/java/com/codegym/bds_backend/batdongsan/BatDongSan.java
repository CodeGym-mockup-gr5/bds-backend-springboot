package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import com.codegym.bds_backend.khachhang.KhachHang;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bat_dong_san")
@SQLDelete(sql = "UPDATE bat_dong_san SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class BatDongSan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_bds", length = 20)
    private String maBds;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "loai_bds", nullable = false, length = 10)
    private LoaiBds loaiBds;

    @Column(name = "dia_chi", nullable = false)
    private String diaChi;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "vi_tri", nullable = false, length = 10)
    private ViTri viTri;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "huong", nullable = false, length = 10)
    private Huong huong;

    @Column(name = "dien_tich", nullable = false, precision = 10, scale = 2)
    private BigDecimal dienTich;

    @Column(name = "gia", nullable = false, precision = 15, scale = 0)
    private BigDecimal gia;

    @Column(name = "gia_m2", insertable = false, updatable = false)
    private BigDecimal giaM2;

    @Column(name = "phap_ly")
    private String phapLy;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tinh_trang", nullable = false, length = 10)
    private TinhTrangBds tinhTrang = TinhTrangBds.CO_SAN;

    @Column(name = "ngay_dang", nullable = false)
    private LocalDate ngayDang;

    @Column(name = "ghi_chu", length = 50)
    private String ghiChu;

    @OneToMany(mappedBy = "batDongSan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("thuTu ASC")
    private List<HinhAnhBds> hinhAnh = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

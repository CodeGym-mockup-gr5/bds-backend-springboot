package com.codegym.bds_backend.thongbao;

import com.codegym.bds_backend.auth.TaiKhoan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "thong_bao_nguoi_dung",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_thong_bao_nguoi_dung",
                columnNames = {"thong_bao_id", "tai_khoan_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ThongBaoNguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thong_bao_id", nullable = false)
    private ThongBao thongBao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tai_khoan_id", nullable = false)
    private TaiKhoan taiKhoan;

    @Column(name = "da_xem", nullable = false)
    private boolean daXem;

    @Column(name = "xem_luc")
    private LocalDateTime xemLuc;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

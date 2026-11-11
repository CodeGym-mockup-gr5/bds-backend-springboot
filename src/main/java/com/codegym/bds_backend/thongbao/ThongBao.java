package com.codegym.bds_backend.thongbao;

import com.codegym.bds_backend.nhanvien.NhanVien;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "thong_bao")
@SQLDelete(sql = "UPDATE thong_bao SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ngay", nullable = false)
    private LocalDate ngay;

    @Column(name = "ten_thong_bao", nullable = false)
    private String tenThongBao;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "noi_dung", nullable = false, columnDefinition = "text")
    private String noiDung;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nguoi_tao_id", nullable = false)
    private NhanVien nguoiTao;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

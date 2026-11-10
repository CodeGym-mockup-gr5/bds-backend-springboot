package com.codegym.bds_backend.bieumau;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "bieu_mau")
@SQLDelete(sql = "UPDATE bieu_mau SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class BieuMau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_bm", length = 20)
    private String maBm;

    @Column(name = "ten_bieu_mau", nullable = false)
    private String tenBieuMau;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "noi_dung", columnDefinition = "text")
    private String noiDung;

    @Column(name = "duong_dan_tep", nullable = false, length = 500)
    private String duongDanTep;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

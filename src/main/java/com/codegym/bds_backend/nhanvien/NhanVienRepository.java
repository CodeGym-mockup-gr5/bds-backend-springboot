package com.codegym.bds_backend.nhanvien;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    Optional<NhanVien> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByLaAdminTrue();

    @Query("SELECT nv FROM NhanVien nv "
            + "WHERE (:maNv IS NULL OR LOWER(nv.maNv) LIKE LOWER(CONCAT('%', :maNv, '%'))) "
            + "AND (:hoTen IS NULL OR LOWER(nv.hoTen) LIKE LOWER(CONCAT('%', :hoTen, '%'))) "
            + "AND (:chucVu IS NULL OR nv.chucVu = :chucVu) "
            + "AND (:email IS NULL OR LOWER(nv.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<NhanVien> searchNhanVien(
            @Param("maNv") String maNv,
            @Param("hoTen") String hoTen,
            @Param("chucVu") ChucVu chucVu,
            @Param("email") String email,
            Pageable pageable);
}

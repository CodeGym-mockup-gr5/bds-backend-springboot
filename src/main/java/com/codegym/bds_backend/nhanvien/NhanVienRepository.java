package com.codegym.bds_backend.nhanvien;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    Optional<NhanVien> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    @Query(value = "SELECT COUNT(*) > 0 FROM nhan_vien WHERE LOWER(email) = LOWER(:email) AND id <> :id", nativeQuery = true)
    boolean existsByEmailIgnoreCaseAndIdNot(@Param("email") String email, @Param("id") Long id);

    boolean existsByLaAdminTrue();

    @Query("select t.nhanVien from TaiKhoan t where t.id = :taiKhoanId and t.nhanVien is not null")
    Optional<NhanVien> findByTaiKhoanId(@Param("taiKhoanId") Long taiKhoanId);
}
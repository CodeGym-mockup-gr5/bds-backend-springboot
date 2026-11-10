package com.codegym.bds_backend.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);

    List<TaiKhoan> findAllByMatKhauHashIsNull();

    @Query("select t from TaiKhoan t left join fetch t.nhanVien left join fetch t.khachHang "
            + "where t.tenDangNhap = :tenDangNhap")
    Optional<TaiKhoan> findWithChuSoHuuByTenDangNhap(@Param("tenDangNhap") String tenDangNhap);

    @Query("select t from TaiKhoan t left join fetch t.nhanVien left join fetch t.khachHang where t.id = :id")
    Optional<TaiKhoan> findWithChuSoHuuById(@Param("id") Long id);
}

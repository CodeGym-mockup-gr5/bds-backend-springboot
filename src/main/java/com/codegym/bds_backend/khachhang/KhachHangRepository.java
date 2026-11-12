package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findBySoCccd(String soCccd);

    boolean existsBySoCccd(String soCccd);

    boolean existsByEmailIgnoreCase(String email);

    Page<KhachHang> findByLaKhBanTrueAndTrangThaiDuyet(TrangThaiDuyet trangThaiDuyet, Pageable pageable);
}

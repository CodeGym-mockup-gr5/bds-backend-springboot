package com.codegym.bds_backend.khachhang;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findBySoCccd(String soCccd);

    boolean existsBySoCccd(String soCccd);

    boolean existsByEmailIgnoreCase(String email);
}

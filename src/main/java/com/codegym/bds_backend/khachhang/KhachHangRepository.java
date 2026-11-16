package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findBySoCccd(String soCccd);

    boolean existsBySoCccd(String soCccd);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query(value = "SELECT COUNT(*) FROM khach_hang WHERE so_cccd = :soCccd AND id <> :id",
            nativeQuery = true)
    long countSoCccdIncludingDeleted(@Param("soCccd") String soCccd, @Param("id") Long id);

    Page<KhachHang> findByLaKhBanTrueAndTrangThaiDuyet(TrangThaiDuyet trangThaiDuyet, Pageable pageable);
}

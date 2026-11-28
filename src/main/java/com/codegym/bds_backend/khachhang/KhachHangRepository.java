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

    @Query(value = "SELECT COUNT(*) FROM khach_hang WHERE so_cccd = :soCccd",
            nativeQuery = true)
    long countAllSoCccdIncludingDeleted(@Param("soCccd") String soCccd);

    @Query(value = "SELECT COUNT(*) FROM khach_hang WHERE so_cccd = :soCccd AND id <> :id",
            nativeQuery = true)
    long countSoCccdIncludingDeleted(@Param("soCccd") String soCccd, @Param("id") Long id);

    @Query("SELECT kh FROM KhachHang kh "
            + "WHERE (:laKhMua IS NULL OR kh.laKhMua = :laKhMua) "
            + "AND (:laKhBan IS NULL OR kh.laKhBan = :laKhBan) "
            + "AND (:keyword IS NULL "
            + "OR LOWER(kh.maKh) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(COALESCE(kh.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR (:includePhoneSearch = true AND kh.soDienThoai LIKE CONCAT('%', :keyword, '%')))")
    Page<KhachHang> searchKhachHang(
            @Param("keyword") String keyword,
            @Param("laKhMua") Boolean laKhMua,
            @Param("laKhBan") Boolean laKhBan,
            @Param("includePhoneSearch") boolean includePhoneSearch,
            Pageable pageable);

    Page<KhachHang> findByLaKhBanTrueAndTrangThaiDuyet(TrangThaiDuyet trangThaiDuyet, Pageable pageable);
}

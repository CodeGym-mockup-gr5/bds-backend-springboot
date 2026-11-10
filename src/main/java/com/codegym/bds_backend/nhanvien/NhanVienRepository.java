package com.codegym.bds_backend.nhanvien;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    Optional<NhanVien> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByLaAdminTrue();
}

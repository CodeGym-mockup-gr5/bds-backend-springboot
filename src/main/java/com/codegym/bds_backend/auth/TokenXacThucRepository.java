package com.codegym.bds_backend.auth;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenXacThucRepository extends JpaRepository<TokenXacThuc, Long> {
    Optional<TokenXacThuc> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TokenXacThuc t set t.daDungLuc = :luc "
            + "where t.taiKhoan.id = :taiKhoanId and t.daDungLuc is null")
    int markDaDungByTaiKhoanId(@Param("taiKhoanId") Long taiKhoanId, @Param("luc") LocalDateTime luc);
}

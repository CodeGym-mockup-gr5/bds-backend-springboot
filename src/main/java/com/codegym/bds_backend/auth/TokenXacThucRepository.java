package com.codegym.bds_backend.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenXacThucRepository extends JpaRepository<TokenXacThuc, Long> {
    Optional<TokenXacThuc> findByTokenHash(String tokenHash);
}

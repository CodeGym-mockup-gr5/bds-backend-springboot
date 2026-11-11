package com.codegym.bds_backend.batdongsan;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HinhAnhBdsRepository extends JpaRepository<HinhAnhBds, Long> {

    @Query("""
            SELECT h FROM HinhAnhBds h
            WHERE h.batDongSan.id IN :batDongSanIds
            ORDER BY h.batDongSan.id ASC, h.thuTu ASC, h.id ASC
            """)
    List<HinhAnhBds> findByBatDongSanIds(@Param("batDongSanIds") Collection<Long> batDongSanIds);
}

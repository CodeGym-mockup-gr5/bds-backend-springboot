package com.codegym.bds_backend.batdongsan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BatDongSanRepository
        extends JpaRepository<BatDongSan, Long>, JpaSpecificationExecutor<BatDongSan> {

    /** Tải kèm chủ sở hữu trong cùng truy vấn để tránh N+1 khi hiển thị danh sách. */
    @Override
    @EntityGraph(attributePaths = "khachHang")
    Page<BatDongSan> findAll(Specification<BatDongSan> spec, Pageable pageable);
}

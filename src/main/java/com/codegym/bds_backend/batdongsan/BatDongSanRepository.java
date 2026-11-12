package com.codegym.bds_backend.batdongsan;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BatDongSanRepository
        extends JpaRepository<BatDongSan, Long>, JpaSpecificationExecutor<BatDongSan> {

    @Override
    @EntityGraph(attributePaths = "khachHang")
    Page<BatDongSan> findAll(Specification<BatDongSan> spec, Pageable pageable);

    Page<BatDongSan> findAllByOrderByNgayDangDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"khachHang", "hinhAnh"})
    Optional<BatDongSan> findWithChiTietById(Long id);
}

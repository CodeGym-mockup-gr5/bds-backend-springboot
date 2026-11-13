package com.codegym.bds_backend.thongbao;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {

    @EntityGraph(attributePaths = {"nguoiTao"})
    Page<ThongBao> findAllByOrderByNgayDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"nguoiTao"})
    Optional<ThongBao> findWithNguoiTaoById(Long id);
}

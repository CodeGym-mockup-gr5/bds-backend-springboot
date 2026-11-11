package com.codegym.bds_backend.thongbao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {

    @EntityGraph(attributePaths = {"nguoiTao"})
    @Query("select t from ThongBao t where (:tuKhoa is null or lower(t.tenThongBao) like lower(concat('%', :tuKhoa, '%'))) "
            + "order by t.ngay desc, t.id desc")
    Page<ThongBao> findThongBaoByTuKhoa(@Param("tuKhoa") String tuKhoa, Pageable pageable);

    @EntityGraph(attributePaths = {"nguoiTao"})
    @Query("select t from ThongBao t order by t.ngay desc, t.id desc")
    List<ThongBao> findTopMoiNhat(Pageable pageable);

    @EntityGraph(attributePaths = {"nguoiTao"})
    Optional<ThongBao> findWithNguoiTaoById(Long id);
}

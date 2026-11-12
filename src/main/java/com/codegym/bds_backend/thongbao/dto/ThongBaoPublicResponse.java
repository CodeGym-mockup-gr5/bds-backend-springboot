package com.codegym.bds_backend.thongbao.dto;

import com.codegym.bds_backend.thongbao.ThongBao;
import java.time.LocalDate;

public record ThongBaoPublicResponse(
        Long id,
        LocalDate ngay,
        String tenThongBao,
        String noiDung
) {
    public static ThongBaoPublicResponse fromEntity(ThongBao thongBao) {
        return new ThongBaoPublicResponse(
                thongBao.getId(),
                thongBao.getNgay(),
                thongBao.getTenThongBao(),
                thongBao.getNoiDung()
        );
    }
}
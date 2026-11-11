package com.codegym.bds_backend.thongbao.dto;

import com.codegym.bds_backend.thongbao.ThongBao;
import java.time.LocalDate;

public record ThongBaoResponse(
        Long id,
        LocalDate ngay,
        String tenThongBao,
        String noiDung,
        Long nguoiTaoId,
        String nguoiTaoTen
) {
    public static ThongBaoResponse fromEntity(ThongBao thongBao) {
        return new ThongBaoResponse(
                thongBao.getId(),
                thongBao.getNgay(),
                thongBao.getTenThongBao(),
                thongBao.getNoiDung(),
                thongBao.getNguoiTao() != null ? thongBao.getNguoiTao().getId() : null,
                thongBao.getNguoiTao() != null ? thongBao.getNguoiTao().getHoTen() : null
        );
    }
}

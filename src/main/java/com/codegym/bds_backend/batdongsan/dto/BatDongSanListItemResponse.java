package com.codegym.bds_backend.batdongsan.dto;

import com.codegym.bds_backend.batdongsan.Huong;
import com.codegym.bds_backend.batdongsan.TinhTrangBds;
import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BatDongSanListItemResponse(
        Long id,
        String maBds,
        Long khachHangId,
        String maKh,
        String tenChuSoHuu,
        LoaiBds loaiBds,
        String diaChi,
        ViTri viTri,
        Huong huong,
        BigDecimal dienTich,
        BigDecimal gia,
        BigDecimal giaM2,
        TinhTrangBds tinhTrang,
        String ghiChu,
        LocalDate ngayDang,
        String anhDaiDien) {
}

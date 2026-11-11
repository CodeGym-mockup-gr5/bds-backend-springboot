package com.codegym.bds_backend.batdongsan.dto;

import com.codegym.bds_backend.batdongsan.Huong;
import com.codegym.bds_backend.batdongsan.TinhTrangBds;
import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BatDongSanDetailResponse(
        Long id,
        String maBds,
        LoaiBds loaiBds,
        String diaChi,
        ViTri viTri,
        Huong huong,
        BigDecimal dienTich,
        BigDecimal gia,
        BigDecimal giaM2,
        String phapLy,
        TinhTrangBds tinhTrang,
        String ghiChu,
        LocalDate ngayDang,
        List<HinhAnhResponse> hinhAnh,
        ChuSoHuuResponse chuSoHuu) {
}

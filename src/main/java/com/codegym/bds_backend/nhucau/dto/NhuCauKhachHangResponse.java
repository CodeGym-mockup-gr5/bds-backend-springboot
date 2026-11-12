package com.codegym.bds_backend.nhucau.dto;

import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.nhucau.NhuCau;
import java.math.BigDecimal;

public record NhuCauKhachHangResponse(
        Long id,
        String maNc,
        Long khachHangId,
        String maKh,
        LoaiBds loaiBds,
        ViTri viTri,
        BigDecimal dienTichMin,
        BigDecimal dienTichMax,
        String ghiChu) {

    public static NhuCauKhachHangResponse fromEntity(NhuCau nhuCau, KhachHang khachHang) {
        return new NhuCauKhachHangResponse(
                nhuCau.getId(),
                nhuCau.getMaNc(),
                khachHang.getId(),
                khachHang.getMaKh(),
                nhuCau.getLoaiBds(),
                nhuCau.getViTri(),
                nhuCau.getDienTichMin(),
                nhuCau.getDienTichMax(),
                nhuCau.getGhiChu());
    }
}

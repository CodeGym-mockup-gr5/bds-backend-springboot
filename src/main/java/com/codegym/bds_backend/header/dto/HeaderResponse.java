package com.codegym.bds_backend.header.dto;

import java.util.List;

public record HeaderResponse(
        String logo,
        String tenThuongHieu,
        boolean daDangNhap,
        boolean coNutDangTin,
        boolean coThongBao,
        List<MenuItemResponse> danhSachMenu,
        List<ActionButtonResponse> danhSachHanhDong,
        HeaderUserResponse nguoiDung
) {
}

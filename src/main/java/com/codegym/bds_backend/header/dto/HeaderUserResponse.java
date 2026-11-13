package com.codegym.bds_backend.header.dto;

import java.util.List;

public record HeaderUserResponse(
        Long id,
        String tenDangNhap,
        String hoTen,
        String kyTuDaiDien,
        List<String> vaiTro
) {
}

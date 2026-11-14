package com.codegym.bds_backend.khachhang.dto;

import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import java.time.LocalDate;

public record KhachHangProfileResponse(
        Long id,
        String maKh,
        String hoTen,
        LocalDate ngaySinh,
        GioiTinh gioiTinh,
        String diaChi,
        String soDienThoai,
        String email,
        String soCccd,
        boolean laKhMua,
        boolean laKhBan,
        TrangThaiDuyet trangThaiDuyet) {
}

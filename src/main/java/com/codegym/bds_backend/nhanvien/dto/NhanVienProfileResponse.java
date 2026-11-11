package com.codegym.bds_backend.nhanvien.dto;

import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.nhanvien.ChucVu;
import java.time.LocalDate;

public record NhanVienProfileResponse(
        Long id,
        String maNv,
        String hoTen,
        LocalDate ngaySinh,
        GioiTinh gioiTinh,
        String diaChi,
        String soDienThoai,
        String email,
        ChucVu chucVu,
        boolean laAdmin) {
}


package com.codegym.bds_backend.nhanvien.dto;

import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.nhanvien.ChucVu;
import com.codegym.bds_backend.nhanvien.NhanVien;
import java.time.LocalDate;

public record NhanVienResponse(
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

    public static NhanVienResponse fromEntity(NhanVien nv) {
        return new NhanVienResponse(
                nv.getId(),
                nv.getMaNv(),
                nv.getHoTen(),
                nv.getNgaySinh(),
                nv.getGioiTinh(),
                nv.getDiaChi(),
                nv.getSoDienThoai(),
                nv.getEmail(),
                nv.getChucVu(),
                nv.isLaAdmin());
    }
}


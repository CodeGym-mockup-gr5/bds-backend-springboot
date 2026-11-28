package com.codegym.bds_backend.khachhang.dto;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.khachhang.KhachHang;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Thông tin chi tiết khách hàng")
public record KhachHangResponse(
        Long id,
        String maKh,
        String hoTen,
        boolean laKhMua,
        boolean laKhBan,
        TrangThaiDuyet trangThaiDuyet,
        LocalDate ngaySinh,
        GioiTinh gioiTinh,
        String diaChi,
        @Schema(description = "Số điện thoại (chỉ Admin thấy, Nhân viên nhận null)")
        String soDienThoai,
        String email,
        String soCccd,
        Long taiKhoanId,
        String tenDangNhap,
        Boolean daDatMatKhau) {

    public static KhachHangResponse fromEntity(KhachHang khachHang, TaiKhoan taiKhoan, boolean showPhone) {
        return new KhachHangResponse(
                khachHang.getId(),
                khachHang.getMaKh(),
                khachHang.getHoTen(),
                khachHang.isLaKhMua(),
                khachHang.isLaKhBan(),
                khachHang.getTrangThaiDuyet(),
                khachHang.getNgaySinh(),
                khachHang.getGioiTinh(),
                khachHang.getDiaChi(),
                showPhone ? khachHang.getSoDienThoai() : null,
                khachHang.getEmail(),
                khachHang.getSoCccd(),
                taiKhoan != null ? taiKhoan.getId() : null,
                taiKhoan != null ? taiKhoan.getTenDangNhap() : null,
                taiKhoan != null ? taiKhoan.getMatKhauHash() != null : null);
    }
}

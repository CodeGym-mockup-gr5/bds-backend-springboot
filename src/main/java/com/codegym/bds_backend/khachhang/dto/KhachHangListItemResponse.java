package com.codegym.bds_backend.khachhang.dto;

import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.khachhang.KhachHang;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin tóm tắt khách hàng trong danh sách")
public record KhachHangListItemResponse(
        Long id,
        String maKh,
        String hoTen,
        boolean laKhMua,
        boolean laKhBan,
        TrangThaiDuyet trangThaiDuyet,
        @Schema(description = "Số điện thoại (chỉ Admin thấy, Nhân viên nhận null)")
        String soDienThoai,
        String email,
        String soCccd) {

    public static KhachHangListItemResponse fromEntity(KhachHang khachHang, boolean showPhone) {
        return new KhachHangListItemResponse(
                khachHang.getId(),
                khachHang.getMaKh(),
                khachHang.getHoTen(),
                khachHang.isLaKhMua(),
                khachHang.isLaKhBan(),
                khachHang.getTrangThaiDuyet(),
                showPhone ? khachHang.getSoDienThoai() : null,
                khachHang.getEmail(),
                khachHang.getSoCccd());
    }
}

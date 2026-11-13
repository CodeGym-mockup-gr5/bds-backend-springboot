package com.codegym.bds_backend.khachhang.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Khách hàng đang chờ Admin duyệt đăng tin bán")
public record KhachHangChoDuyetResponse(
        @Schema(description = "Mã số khách hàng", example = "12")
        Long id,
        @Schema(description = "Mã hiển thị khách hàng", example = "KH012")
        String maKh,
        @Schema(description = "Họ tên khách hàng", example = "Nguyễn Văn An")
        String hoTen,
        @Schema(description = "Số điện thoại", example = "0912345678")
        String soDienThoai,
        @Schema(description = "Email", example = "an@example.com")
        String email) {
}

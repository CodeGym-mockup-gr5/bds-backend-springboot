package com.codegym.bds_backend.khachhang.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả Admin duyệt đăng tin bán cho khách hàng")
public record DuyetKhachHangResponse(
        @Schema(description = "Mã số khách hàng", example = "12")
        Long id,
        @Schema(description = "Mã hiển thị khách hàng", example = "KH012")
        String maKh,
        @Schema(description = "Khách đã đăng ký đăng tin bán hay chưa", example = "true")
        Boolean laKhBan,
        @Schema(description = "Trạng thái duyệt đăng tin bán sau khi duyệt", example = "DA_DUYET")
        String trangThaiDuyet) {
}

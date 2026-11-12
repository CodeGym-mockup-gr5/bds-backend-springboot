package com.codegym.bds_backend.khachhang.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kết quả gửi yêu cầu đăng tin bán của khách hàng")
public record YeuCauDangTinResponse(
        @Schema(description = "Khách đã đăng ký đăng tin bán hay chưa", example = "true")
        Boolean laKhBan,
        @Schema(description = "Trạng thái duyệt đăng tin bán: CHO_DUYET hoặc DA_DUYET", example = "CHO_DUYET")
        String trangThaiDuyet) {
}

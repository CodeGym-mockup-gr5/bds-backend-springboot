package com.codegym.bds_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ThongTinTaiKhoan(
        Long id,
        String tenDangNhap,
        String hoTen,
        List<String> vaiTro,
        @Schema(description = "Khách đã đăng ký đăng tin bán hay chưa. Tài khoản nhân viên trả null", example = "true")
        Boolean laKhBan,
        @Schema(description = "Trạng thái duyệt đăng tin bán của khách: CHO_DUYET hoặc DA_DUYET. "
                + "Tài khoản nhân viên trả null", example = "CHO_DUYET")
        String trangThaiDuyet) {
}

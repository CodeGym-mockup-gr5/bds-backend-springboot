package com.codegym.bds_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 45, message = "Họ tên tối đa 45 ký tự") String hoTen,
        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ") String soDienThoai,
        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        @Size(max = 254, message = "Email tối đa 254 ký tự") String email,
        @Pattern(regexp = "^[0-9]{12}$", message = "Số CCCD gồm đúng 12 chữ số") String soCccd,
        @NotBlank(message = "Vui lòng nhập tên đăng nhập")
        @Size(min = 4, max = 50, message = "Tên đăng nhập từ 4 đến 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                message = "Tên đăng nhập chỉ gồm chữ, số, dấu chấm, gạch ngang, gạch dưới")
        String tenDangNhap,
        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @Size(min = 8, max = 64, message = "Mật khẩu từ 8 đến 64 ký tự") String matKhau,
        @Schema(description = "Khách muốn đăng tin bán. true thì tài khoản chờ Admin duyệt mới được đăng tin; "
                + "bỏ trống hoặc false coi như không đăng ký bán", example = "false")
        Boolean muonDangTinBan) {
}

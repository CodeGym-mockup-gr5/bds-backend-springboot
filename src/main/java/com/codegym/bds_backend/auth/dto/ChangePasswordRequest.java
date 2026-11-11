package com.codegym.bds_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Xin mời nhập mật khẩu cũ")
        String matKhauCu,

        @NotBlank(message = "Xin mời nhập mật khẩu mới")
        @Size(min = 6, max = 32, message = "Mật khẩu mới phải từ 6 đến 32 ký tự")
        @Pattern(regexp = "^\\S+$", message = "Mật khẩu mới không được chứa khoảng trắng")
        String matKhauMoi,

        @NotBlank(message = "Xin mời xác nhận mật khẩu")
        String xacNhanMatKhau) {
}

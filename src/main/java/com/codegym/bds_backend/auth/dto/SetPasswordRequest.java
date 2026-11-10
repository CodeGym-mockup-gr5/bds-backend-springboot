package com.codegym.bds_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordRequest(
        @NotBlank(message = "Thiếu mã xác thực") String token,
        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @Size(min = 8, max = 64, message = "Mật khẩu từ 8 đến 64 ký tự") String matKhau) {
}

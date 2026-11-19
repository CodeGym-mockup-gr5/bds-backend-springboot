package com.codegym.bds_backend.auth.dto;

import com.codegym.bds_backend.auth.validation.PasswordPolicy;
import com.codegym.bds_backend.auth.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SetPasswordRequest(
        @NotBlank(message = "Thiếu mã xác thực") String token,
        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @ValidPassword
        @Schema(description = PasswordPolicy.SCHEMA_DESCRIPTION)
        String matKhau) {
}

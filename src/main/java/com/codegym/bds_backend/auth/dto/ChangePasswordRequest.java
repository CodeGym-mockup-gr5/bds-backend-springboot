package com.codegym.bds_backend.auth.dto;

import com.codegym.bds_backend.auth.validation.PasswordPolicy;
import com.codegym.bds_backend.auth.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Xin mời nhập mật khẩu cũ")
        String matKhauCu,

        @NotBlank(message = "Xin mời nhập mật khẩu mới")
        @ValidPassword
        @Schema(description = PasswordPolicy.SCHEMA_DESCRIPTION)
        String matKhauMoi,

        @NotBlank(message = "Xin mời xác nhận mật khẩu")
        String xacNhanMatKhau) {
}

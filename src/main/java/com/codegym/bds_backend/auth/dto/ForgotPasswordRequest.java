package com.codegym.bds_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank(message = "Vui lòng nhập tên đăng nhập") String tenDangNhap) {
}

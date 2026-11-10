package com.codegym.bds_backend.auth.dto;

public record LoginResponse(String token, ThongTinTaiKhoan taiKhoan) {
}

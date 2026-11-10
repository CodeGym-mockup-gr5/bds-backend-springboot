package com.codegym.bds_backend.auth;

import java.util.List;

/** Thông tin người dùng nằm trong JWT. roles là tên quyền dạng ROLE_ADMIN, ROLE_NHAN_VIEN, ROLE_KHACH_HANG. */
public record JwtPrincipal(Long userId, String username, List<String> roles) {
}

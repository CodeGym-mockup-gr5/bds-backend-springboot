package com.codegym.bds_backend.auth.dto;

import java.util.List;

public record ThongTinTaiKhoan(Long id, String tenDangNhap, String hoTen, List<String> vaiTro) {
}

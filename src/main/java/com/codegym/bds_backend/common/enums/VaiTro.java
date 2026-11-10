package com.codegym.bds_backend.common.enums;

/** Vai trò dùng cho phân quyền. Admin lấy từ nhan_vien.la_admin. */
public enum VaiTro {
    ADMIN,
    NHAN_VIEN,
    KHACH_HANG;

    /** Tên quyền theo Spring Security, dùng với hasRole("ADMIN"). */
    public String authority() {
        return "ROLE_" + name();
    }
}

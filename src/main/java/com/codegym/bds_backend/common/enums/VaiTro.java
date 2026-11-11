package com.codegym.bds_backend.common.enums;

public enum VaiTro {
    ADMIN,
    NHAN_VIEN,
    KHACH_HANG;

    public String authority() {
        return "ROLE_" + name();
    }
}

package com.codegym.bds_backend.common.util;

public final class MaHienThi {
    public static final String KHACH_HANG = "KH";
    public static final String BAT_DONG_SAN = "BDS";
    public static final String NHU_CAU = "NC";
    public static final String GIAO_DICH = "GD";
    public static final String BIEU_MAU = "HS";
    public static final String NHAN_VIEN = "MNV-";

    private MaHienThi() {
    }

    public static String format(String prefix, long id) {
        return prefix + String.format("%03d", id);
    }
}

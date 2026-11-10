package com.codegym.bds_backend.common.util;

/** Mã hiển thị sinh từ id sau khi lưu: tiền tố + id, tối thiểu 3 chữ số (KH012, BDS045...). */
public final class MaHienThi {

    public static final String KHACH_HANG = "KH";
    public static final String BAT_DONG_SAN = "BDS";
    public static final String NHU_CAU = "NC";
    public static final String GIAO_DICH = "GD";
    public static final String BIEU_MAU = "HS";

    private MaHienThi() {
    }

    public static String format(String prefix, long id) {
        return prefix + String.format("%03d", id);
    }
}

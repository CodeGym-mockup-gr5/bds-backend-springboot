package com.codegym.bds_backend.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codegym.bds_backend.common.util.MaHienThi;
import org.junit.jupiter.api.Test;

class MaHienThiTest {
    @Test
    void shouldPadToThreeDigits() {
        assertEquals("KH012", MaHienThi.format(MaHienThi.KHACH_HANG, 12));
        assertEquals("BDS001", MaHienThi.format(MaHienThi.BAT_DONG_SAN, 1));
    }

    @Test
    void shouldKeepMoreThanThreeDigits() {
        assertEquals("GD1234", MaHienThi.format(MaHienThi.GIAO_DICH, 1234));
    }
}

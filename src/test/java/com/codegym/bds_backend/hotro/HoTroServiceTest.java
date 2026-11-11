package com.codegym.bds_backend.hotro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.hotro.dto.HuongDanResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HoTroServiceTest {
    private HoTroService hoTroService;

    @BeforeEach
    void setUp() {
        hoTroService = new HoTroService();
    }

    @Test
    void findDanhSachHuongDan_traVeDuBonMucHuongDanTheoSRS() {
        List<HuongDanResponse> danhSach = hoTroService.findDanhSachHuongDan();

        assertNotNull(danhSach);
        assertEquals(4, danhSach.size());
        assertEquals("Hướng dẫn sử dụng tài khoản", danhSach.get(0).tieuDe());
        assertEquals("Hướng dẫn sử dụng lập hợp đồng", danhSach.get(1).tieuDe());
        assertEquals("Hướng dẫn đổi mật khẩu", danhSach.get(2).tieuDe());
        assertEquals("Hướng dẫn giao dịch với khách hàng", danhSach.get(3).tieuDe());
    }

    @Test
    void findChiTietHuongDan_tonTai_traVeNoiDungChiTiet() {
        HuongDanResponse response = hoTroService.findChiTietHuongDan("tai-khoan");

        assertNotNull(response);
        assertEquals("tai-khoan", response.maHuongDan());
        assertEquals("Hướng dẫn sử dụng tài khoản", response.tieuDe());
        assertNotNull(response.noiDung());
    }

    @Test
    void findChiTietHuongDan_khongTonTai_nemNotFoundException() {
        assertThrows(NotFoundException.class, () -> hoTroService.findChiTietHuongDan("khong-ton-tai"));
    }
}

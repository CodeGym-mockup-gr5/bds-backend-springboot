package com.codegym.bds_backend.hotro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.hotro.dto.HuongDanResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HoTroServiceTest {
    private HoTroService hoTroService;

    @BeforeEach
    void setUp() {
        hoTroService = new HoTroService();
    }

    @Test
    @DisplayName("Lấy danh sách hướng dẫn: trả về đủ 4 mục theo SRS")
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
    @DisplayName("Xem chi tiết hướng dẫn tồn tại: trả về nội dung chính xác")
    void findChiTietHuongDan_tonTai_traVeNoiDungChiTiet() {
        HuongDanResponse response = hoTroService.findChiTietHuongDan("tai-khoan");

        assertNotNull(response);
        assertEquals("tai-khoan", response.maHuongDan());
        assertEquals("Hướng dẫn sử dụng tài khoản", response.tieuDe());
        assertNotNull(response.noiDung());
    }

    @Test
    @DisplayName("Xem chi tiết với mã không tồn tại: ném NotFoundException")
    void findChiTietHuongDan_khongTonTai_nemNotFoundException() {
        assertThrows(NotFoundException.class, () -> hoTroService.findChiTietHuongDan("khong-ton-tai"));
    }

    @Test
    @DisplayName("Xem chi tiết với mã rỗng hoặc null: ném NotFoundException")
    void findChiTietHuongDan_maRongHoacNull_nemNotFoundException() {
        assertThrows(NotFoundException.class, () -> hoTroService.findChiTietHuongDan(null));
        assertThrows(NotFoundException.class, () -> hoTroService.findChiTietHuongDan("   "));
    }

    @Test
    @DisplayName("Chuẩn hóa mã độc lập Locale (tr-TR với TAI-KHOAN): không bị lỗi chữ i không chấm")
    void findChiTietHuongDan_localeDocLap_hoatDongChinhXacKhiChayVoiLocaleThoNhiKy() {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            HuongDanResponse response = hoTroService.findChiTietHuongDan("TAI-KHOAN");
            assertNotNull(response);
            assertEquals("tai-khoan", response.maHuongDan());
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}

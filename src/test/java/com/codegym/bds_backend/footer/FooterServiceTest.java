package com.codegym.bds_backend.footer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.codegym.bds_backend.footer.dto.FooterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FooterServiceTest {

    private FooterService footerService;

    @BeforeEach
    void setUp() {
        footerService = new FooterService();
    }

    @Test
    @DisplayName("Lấy thông tin footer: trả về đầy đủ thương hiệu, slogan, liên hệ và bản quyền")
    void findThongTinFooter_traVeDuThongTinCoBan() {
        FooterResponse response = footerService.findThongTinFooter();

        assertNotNull(response);
        assertEquals("REALIA", response.tenThuongHieu());
        assertNotNull(response.slogan());
        assertNotNull(response.banQuyen());

        assertNotNull(response.lienHe());
        assertEquals("1900 6868", response.lienHe().soDienThoai());
        assertEquals("lienhe@realia.com.vn", response.lienHe().email());
        assertEquals("123 Đường Nguyễn Văn Linh, Quận Hải Châu, TP. Đà Nẵng", response.lienHe().diaChi());
    }

    @Test
    @DisplayName("Lấy thông tin footer: trả về đủ 3 chuyên mục liên kết điều hướng")
    void findThongTinFooter_traVeDuCacChuyenMucDieuHuong() {
        FooterResponse response = footerService.findThongTinFooter();

        assertNotNull(response.danhSachChuyenMuc());
        assertEquals(3, response.danhSachChuyenMuc().size());

        assertEquals("Về REALIA", response.danhSachChuyenMuc().get(0).tieuDe());
        assertFalse(response.danhSachChuyenMuc().get(0).danhSachLienKet().isEmpty());

        assertEquals("Hỗ trợ khách hàng", response.danhSachChuyenMuc().get(1).tieuDe());
        assertEquals(4, response.danhSachChuyenMuc().get(1).danhSachLienKet().size());

        assertEquals("Chính sách & Quy định", response.danhSachChuyenMuc().get(2).tieuDe());
        assertEquals(3, response.danhSachChuyenMuc().get(2).danhSachLienKet().size());
    }

    @Test
    @DisplayName("Lấy thông tin footer: trả về đủ danh sách liên kết mạng xã hội")
    void findThongTinFooter_traVeDuMangXaHoi() {
        FooterResponse response = footerService.findThongTinFooter();

        assertNotNull(response.mangXaHoi());
        assertEquals(3, response.mangXaHoi().size());
        assertEquals("Facebook", response.mangXaHoi().get(0).nhan());
        assertEquals("YouTube", response.mangXaHoi().get(1).nhan());
        assertEquals("Zalo", response.mangXaHoi().get(2).nhan());
    }
}

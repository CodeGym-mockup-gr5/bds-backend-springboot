package com.codegym.bds_backend.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.header.dto.HeaderResponse;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.nhanvien.NhanVien;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeaderServiceTest {

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @InjectMocks
    private HeaderService headerService;

    @Test
    @DisplayName("Chưa đăng nhập (userId null): không có nút đăng tin, có nút đăng ký và đăng nhập")
    void findThongTinHeader_khiChuaDangNhap_traVeHeaderChuaDangNhap() {
        HeaderResponse response = headerService.findThongTinHeader(null);

        assertNotNull(response);
        assertEquals("REALIA", response.tenThuongHieu());
        assertFalse(response.daDangNhap());
        assertFalse(response.coNutDangTin());
        assertFalse(response.coThongBao());
        assertNull(response.nguoiDung());

        assertEquals(3, response.danhSachMenu().size());
        assertEquals("Mua bán", response.danhSachMenu().get(0).nhan());
        assertEquals("Dự án", response.danhSachMenu().get(1).nhan());
        assertEquals("Hỗ trợ", response.danhSachMenu().get(2).nhan());

        assertEquals(2, response.danhSachHanhDong().size());
        assertEquals("DANG_KY", response.danhSachHanhDong().get(0).maHanhDong());
        assertEquals("DANG_NHAP", response.danhSachHanhDong().get(1).maHanhDong());
    }

    @Test
    @DisplayName("Đã đăng nhập với tài khoản nhân viên: có nút đăng tin, có chuông thông báo và thông tin user")
    void findThongTinHeader_khiDaDangNhapNhanVien_traVeHeaderDaDangNhap() {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen("Minh Anh");
        nhanVien.setLaAdmin(false);

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(10L);
        taiKhoan.setTenDangNhap("minhanh");
        taiKhoan.setNhanVien(nhanVien);

        when(taiKhoanRepository.findWithChuSoHuuById(10L)).thenReturn(Optional.of(taiKhoan));

        HeaderResponse response = headerService.findThongTinHeader(10L);

        assertNotNull(response);
        assertTrue(response.daDangNhap());
        assertTrue(response.coNutDangTin());
        assertTrue(response.coThongBao());
        assertNotNull(response.nguoiDung());
        assertEquals("Minh Anh", response.nguoiDung().hoTen());
        assertEquals("MA", response.nguoiDung().kyTuDaiDien());
        assertTrue(response.nguoiDung().vaiTro().contains("ROLE_NHAN_VIEN"));

        assertEquals(2, response.danhSachHanhDong().size());
        assertEquals("THONG_BAO", response.danhSachHanhDong().get(0).maHanhDong());
        assertEquals("DANG_TIN", response.danhSachHanhDong().get(1).maHanhDong());
    }

    @Test
    @DisplayName("Đã đăng nhập với tài khoản khách hàng: lấy họ tên khách hàng chính xác")
    void findThongTinHeader_khiDaDangNhapKhachHang_traVeHoTenKhachHang() {
        KhachHang khachHang = new KhachHang();
        khachHang.setHoTen("Nguyễn Văn Bình");

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(20L);
        taiKhoan.setTenDangNhap("binhnv");
        taiKhoan.setKhachHang(khachHang);

        when(taiKhoanRepository.findWithChuSoHuuById(20L)).thenReturn(Optional.of(taiKhoan));

        HeaderResponse response = headerService.findThongTinHeader(20L);

        assertNotNull(response);
        assertTrue(response.daDangNhap());
        assertEquals("Nguyễn Văn Bình", response.nguoiDung().hoTen());
        assertEquals("NB", response.nguoiDung().kyTuDaiDien());
        assertTrue(response.nguoiDung().vaiTro().contains("ROLE_KHACH_HANG"));
    }

    @Test
    @DisplayName("UserId không tồn tại trong DB: fallback về trạng thái chưa đăng nhập")
    void findThongTinHeader_khiUserIdKhongTonTai_fallbackChuaDangNhap() {
        when(taiKhoanRepository.findWithChuSoHuuById(999L)).thenReturn(Optional.empty());

        HeaderResponse response = headerService.findThongTinHeader(999L);

        assertNotNull(response);
        assertFalse(response.daDangNhap());
        assertFalse(response.coNutDangTin());
        assertNull(response.nguoiDung());
    }
}

package com.codegym.bds_backend.khachhang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.auth.TaiKhoanService;
import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.dto.CreateKhachHangRequest;
import com.codegym.bds_backend.khachhang.dto.KhachHangListItemResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangResponse;
import com.codegym.bds_backend.khachhang.dto.LoaiKhachHang;
import com.codegym.bds_backend.khachhang.dto.UpdateKhachHangRequest;
import com.codegym.bds_backend.nhanvien.NhanVienRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class KhachHangServiceTest {

    @Mock
    private KhachHangRepository khachHangRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private TaiKhoanRepository taiKhoanRepository;
    @Mock
    private TaiKhoanService taiKhoanService;

    @InjectMocks
    private KhachHangService khachHangService;

    @Test
    void shouldShowPhoneAndSearchByPhoneWhenAdminQueriesList() {
        Pageable pageable = PageRequest.of(0, 10);
        KhachHang kh = sampleKhachHang(12L);
        when(khachHangRepository.searchKhachHang("0912", Boolean.TRUE, null, true, pageable))
                .thenReturn(new PageImpl<>(List.of(kh), pageable, 1));

        PageResponse<KhachHangListItemResponse> result = khachHangService.findDanhSachKhachHang(
                " 0912 ", LoaiKhachHang.MUA, pageable, adminPrincipal());

        assertEquals(1, result.content().size());
        assertEquals("0912345678", result.content().get(0).soDienThoai());
        assertEquals("001234567890", result.content().get(0).soCccd());
    }

    @Test
    void shouldHidePhoneAndDisablePhoneSearchWhenNhanVienQueriesList() {
        Pageable pageable = PageRequest.of(0, 10);
        KhachHang kh = sampleKhachHang(12L);
        when(khachHangRepository.searchKhachHang("an", null, Boolean.TRUE, false, pageable))
                .thenReturn(new PageImpl<>(List.of(kh), pageable, 1));

        PageResponse<KhachHangListItemResponse> result = khachHangService.findDanhSachKhachHang(
                "an", LoaiKhachHang.BAN, pageable, nhanVienPrincipal());

        assertEquals(1, result.content().size());
        assertNull(result.content().get(0).soDienThoai());
    }

    @Test
    void shouldReturnDetailWithAccountAndHidePhoneForNhanVien() {
        KhachHang kh = sampleKhachHang(12L);
        TaiKhoan tk = new TaiKhoan();
        tk.setId(50L);
        tk.setTenDangNhap("kh012");
        tk.setMatKhauHash("hash");
        when(khachHangRepository.findById(12L)).thenReturn(Optional.of(kh));
        when(taiKhoanRepository.findByKhachHangId(12L)).thenReturn(Optional.of(tk));

        KhachHangResponse adminView = khachHangService.findKhachHangById(12L, adminPrincipal());
        KhachHangResponse nvView = khachHangService.findKhachHangById(12L, nhanVienPrincipal());

        assertEquals("0912345678", adminView.soDienThoai());
        assertEquals("kh012", adminView.tenDangNhap());
        assertEquals(Boolean.TRUE, adminView.daDatMatKhau());
        assertNull(nvView.soDienThoai());
    }

    @Test
    void shouldThrowNotFoundWhenDetailMissing() {
        when(khachHangRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> khachHangService.findKhachHangById(99L, adminPrincipal()));
    }

    @Test
    void shouldCreateKhachHangWithoutAccountWhenTenDangNhapBlank() {
        CreateKhachHangRequest req = new CreateKhachHangRequest(
                "Nguyễn Văn An", true, false, LocalDate.of(1995, 5, 20), GioiTinh.NAM,
                "Hà Nội", "0912345678", "an@example.com", "001234567890", "   ");
        when(khachHangRepository.existsByEmailIgnoreCase("an@example.com")).thenReturn(false);
        when(nhanVienRepository.existsEmailIgnoreCaseIncludingDeleted("an@example.com")).thenReturn(false);
        when(khachHangRepository.countAllSoCccdIncludingDeleted("001234567890")).thenReturn(0L);
        when(khachHangRepository.saveAndFlush(any(KhachHang.class))).thenAnswer(inv -> {
            KhachHang saved = inv.getArgument(0);
            saved.setId(15L);
            return saved;
        });

        KhachHangResponse res = khachHangService.createKhachHang(req, adminPrincipal());

        assertEquals("KH015", res.maKh());
        assertEquals(TrangThaiDuyet.DA_DUYET, res.trangThaiDuyet());
        assertNull(res.tenDangNhap());
        verify(taiKhoanService, never()).createTaiKhoanChoKhachHang(any(), any());
    }

    @Test
    void shouldCreateKhachHangAndInviteAccountWhenTenDangNhapProvided() {
        CreateKhachHangRequest req = new CreateKhachHangRequest(
                "Nguyễn Văn An", false, true, LocalDate.of(1995, 5, 20), GioiTinh.NAM,
                "Hà Nội", "0912345678", "an@example.com", "001234567890", "kh_an01");
        when(taiKhoanRepository.existsByTenDangNhap("kh_an01")).thenReturn(false);
        when(khachHangRepository.existsByEmailIgnoreCase("an@example.com")).thenReturn(false);
        when(nhanVienRepository.existsEmailIgnoreCaseIncludingDeleted("an@example.com")).thenReturn(false);
        when(khachHangRepository.countAllSoCccdIncludingDeleted("001234567890")).thenReturn(0L);
        when(khachHangRepository.saveAndFlush(any(KhachHang.class))).thenAnswer(inv -> {
            KhachHang saved = inv.getArgument(0);
            saved.setId(16L);
            return saved;
        });
        TaiKhoan tk = new TaiKhoan();
        tk.setId(88L);
        tk.setTenDangNhap("kh_an01");
        when(taiKhoanService.createTaiKhoanChoKhachHang(any(KhachHang.class), eq("kh_an01"))).thenReturn(tk);

        KhachHangResponse res = khachHangService.createKhachHang(req, nhanVienPrincipal());

        assertEquals("KH016", res.maKh());
        assertEquals(TrangThaiDuyet.DA_DUYET, res.trangThaiDuyet());
        assertTrue(res.laKhBan());
        assertEquals("kh_an01", res.tenDangNhap());
        assertEquals(Boolean.FALSE, res.daDatMatKhau());
        assertNull(res.soDienThoai());
        verify(taiKhoanService).createTaiKhoanChoKhachHang(any(KhachHang.class), eq("kh_an01"));
    }

    @Test
    void shouldThrowBadRequestWhenTenDangNhapProvidedWithoutEmail() {
        CreateKhachHangRequest req = new CreateKhachHangRequest(
                "Nguyễn Văn An", true, false, null, null, null, "0912345678", null, null, "kh_an01");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> khachHangService.createKhachHang(req, adminPrincipal()));

        assertEquals("email", ex.getErrors().get(0).field());
    }

    @Test
    void shouldThrowBadRequestWhenNoLoaiKhachHangSelected() {
        CreateKhachHangRequest req = new CreateKhachHangRequest(
                "Nguyễn Văn An", false, false, null, null, null, "0912345678", null, null, null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> khachHangService.createKhachHang(req, adminPrincipal()));

        assertEquals("laKhMua", ex.getErrors().get(0).field());
    }

    @Test
    void shouldThrowConflictWhenSoCccdAlreadyExistsIncludingDeleted() {
        CreateKhachHangRequest req = new CreateKhachHangRequest(
                "Nguyễn Văn An", true, false, null, null, null, "0912345678", null, "001234567890", null);
        when(khachHangRepository.countAllSoCccdIncludingDeleted("001234567890")).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> khachHangService.createKhachHang(req, adminPrincipal()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("soCccd", ex.getErrors().get(0).field());
    }

    @Test
    void shouldUpdateKhachHangAndKeepSoCccdUnchangedAndApproveWhenEnablingSeller() {
        KhachHang kh = sampleKhachHang(12L);
        kh.setLaKhBan(false);
        kh.setTrangThaiDuyet(TrangThaiDuyet.CHO_DUYET);
        when(khachHangRepository.findById(12L)).thenReturn(Optional.of(kh));
        when(taiKhoanRepository.findByKhachHangId(12L)).thenReturn(Optional.empty());
        when(khachHangRepository.existsByEmailIgnoreCaseAndIdNot("moi@example.com", 12L)).thenReturn(false);
        when(nhanVienRepository.existsEmailIgnoreCaseIncludingDeleted("moi@example.com")).thenReturn(false);
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateKhachHangRequest req = new UpdateKhachHangRequest(
                "Nguyễn Văn Bình", true, true, LocalDate.of(1990, 1, 1), GioiTinh.NAM,
                "Đà Nẵng", "0988777666", "moi@example.com");

        KhachHangResponse res = khachHangService.updateKhachHang(12L, req, adminPrincipal());

        assertEquals("Nguyễn Văn Bình", res.hoTen());
        assertEquals("0988777666", res.soDienThoai());
        assertEquals("moi@example.com", res.email());
        assertEquals("001234567890", res.soCccd());
        assertEquals(TrangThaiDuyet.DA_DUYET, res.trangThaiDuyet());
    }

    @Test
    void shouldRejectClearingEmailWhenKhachHangHasAccount() {
        KhachHang kh = sampleKhachHang(12L);
        TaiKhoan tk = new TaiKhoan();
        tk.setId(50L);
        when(khachHangRepository.findById(12L)).thenReturn(Optional.of(kh));
        when(taiKhoanRepository.findByKhachHangId(12L)).thenReturn(Optional.of(tk));

        UpdateKhachHangRequest req = new UpdateKhachHangRequest(
                "Nguyễn Văn An", true, false, null, null, null, "0912345678", null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> khachHangService.updateKhachHang(12L, req, adminPrincipal()));

        assertEquals("email", ex.getErrors().get(0).field());
    }

    @Test
    void shouldSoftDeleteKhachHangWhenExists() {
        KhachHang kh = sampleKhachHang(12L);
        when(khachHangRepository.findById(12L)).thenReturn(Optional.of(kh));

        khachHangService.deleteKhachHang(12L);

        verify(khachHangRepository).delete(kh);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingMissingKhachHang() {
        when(khachHangRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> khachHangService.deleteKhachHang(99L));
    }

    private static KhachHang sampleKhachHang(Long id) {
        KhachHang kh = new KhachHang();
        kh.setId(id);
        kh.setMaKh("KH012");
        kh.setHoTen("Nguyễn Văn An");
        kh.setLaKhMua(true);
        kh.setLaKhBan(true);
        kh.setTrangThaiDuyet(TrangThaiDuyet.DA_DUYET);
        kh.setSoDienThoai("0912345678");
        kh.setEmail("an@example.com");
        kh.setSoCccd("001234567890");
        return kh;
    }

    private static JwtPrincipal adminPrincipal() {
        return new JwtPrincipal(1L, "admin", List.of("ROLE_NHAN_VIEN", "ROLE_ADMIN"));
    }

    private static JwtPrincipal nhanVienPrincipal() {
        return new JwtPrincipal(2L, "nv01", List.of("ROLE_NHAN_VIEN"));
    }
}

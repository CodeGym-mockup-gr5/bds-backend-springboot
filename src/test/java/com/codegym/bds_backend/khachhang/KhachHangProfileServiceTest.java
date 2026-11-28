package com.codegym.bds_backend.khachhang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.khachhang.dto.KhachHangProfileResponse;
import com.codegym.bds_backend.khachhang.dto.UpdateKhachHangProfileRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KhachHangProfileServiceTest {

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @InjectMocks
    private KhachHangProfileService profileService;

    private TaiKhoan taiKhoan;
    private KhachHang khachHang;

    @BeforeEach
    void setUp() {
        khachHang = new KhachHang();
        khachHang.setId(20L);
        khachHang.setMaKh("MKH-0020");
        khachHang.setHoTen("Trần Thị Hoa");
        khachHang.setNgaySinh(LocalDate.of(1995, 8, 15));
        khachHang.setGioiTinh(GioiTinh.NU);
        khachHang.setDiaChi("10 Lê Lợi, Đà Nẵng");
        khachHang.setSoDienThoai("0905123456");
        khachHang.setEmail("hoa.tran@example.com");
        khachHang.setSoCccd("048195000123");
        khachHang.setLaKhMua(true);
        khachHang.setLaKhBan(false);
        khachHang.setTrangThaiDuyet(TrangThaiDuyet.DA_DUYET);

        taiKhoan = new TaiKhoan();
        taiKhoan.setId(5L);
        taiKhoan.setKhachHang(khachHang);
    }

    @Test
    void shouldGetProfileKhachHangSuccessfully() {
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));

        KhachHangProfileResponse response = profileService.getProfileKhachHang(5L);

        assertNotNull(response);
        assertEquals(20L, response.id());
        assertEquals("MKH-0020", response.maKh());
        assertEquals("Trần Thị Hoa", response.hoTen());
        assertEquals(LocalDate.of(1995, 8, 15), response.ngaySinh());
        assertEquals(GioiTinh.NU, response.gioiTinh());
        assertEquals("10 Lê Lợi, Đà Nẵng", response.diaChi());
        assertEquals("0905123456", response.soDienThoai());
        assertEquals("hoa.tran@example.com", response.email());
        assertEquals("048195000123", response.soCccd());
        assertTrue(response.laKhMua());
        assertFalse(response.laKhBan());
        assertEquals(TrangThaiDuyet.DA_DUYET, response.trangThaiDuyet());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTaiKhoanNotFound() {
        when(taiKhoanRepository.findWithChuSoHuuById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> profileService.getProfileKhachHang(999L));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenKhachHangDeleted() {
        khachHang.setDeletedAt(LocalDateTime.now());
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));

        assertThrows(NotFoundException.class, () -> profileService.getProfileKhachHang(5L));
    }

    @Test
    void shouldUpdateAllowedProfileFieldsAndKeepLockedFieldsUnchanged() {
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateKhachHangProfileRequest request = new UpdateKhachHangProfileRequest(
                "  Trần Thị Hoa Mới  ",
                LocalDate.of(1996, 1, 2),
                GioiTinh.NU,
                "  20 Bạch Đằng, Đà Nẵng  ");

        KhachHangProfileResponse updated = profileService.updateProfileKhachHang(5L, request);

        assertEquals("Trần Thị Hoa Mới", updated.hoTen());
        assertEquals(LocalDate.of(1996, 1, 2), updated.ngaySinh());
        assertEquals(GioiTinh.NU, updated.gioiTinh());
        assertEquals("20 Bạch Đằng, Đà Nẵng", updated.diaChi());
        assertEquals("0905123456", updated.soDienThoai());
        assertEquals("hoa.tran@example.com", updated.email());
        assertEquals("048195000123", updated.soCccd());
        assertEquals("MKH-0020", updated.maKh());
        assertTrue(updated.laKhMua());
        assertFalse(updated.laKhBan());
        assertEquals(TrangThaiDuyet.DA_DUYET, updated.trangThaiDuyet());
        verify(khachHangRepository).save(khachHang);
    }

    @Test
    void shouldNormalizeBlankDiaChiToNull() {
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateKhachHangProfileRequest request = new UpdateKhachHangProfileRequest(
                "Trần Thị Hoa",
                null,
                null,
                "   ");

        KhachHangProfileResponse updated = profileService.updateProfileKhachHang(5L, request);

        assertNull(updated.diaChi());
        assertEquals("048195000123", updated.soCccd());
        assertEquals("hoa.tran@example.com", updated.email());
        assertEquals("0905123456", updated.soDienThoai());
    }
}

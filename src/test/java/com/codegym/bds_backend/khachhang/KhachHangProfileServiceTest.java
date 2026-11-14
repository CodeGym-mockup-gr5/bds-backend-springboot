package com.codegym.bds_backend.khachhang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.khachhang.dto.KhachHangProfileResponse;
import com.codegym.bds_backend.khachhang.dto.UpdateKhachHangProfileRequest;
import com.codegym.bds_backend.nhanvien.NhanVienRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class KhachHangProfileServiceTest {

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

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
    void shouldUpdateProfileKhachHangSuccessfullyAndKeepReadOnlyFields() {
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));
        when(khachHangRepository.existsByEmailIgnoreCaseAndIdNot("hoa.new@example.com", 20L)).thenReturn(false);
        when(nhanVienRepository.existsByEmailIgnoreCase("hoa.new@example.com")).thenReturn(false);
        when(khachHangRepository.countSoCccdIncludingDeleted("048195000999", 20L)).thenReturn(0L);
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateKhachHangProfileRequest request = new UpdateKhachHangProfileRequest(
                "  Trần Thị Hoa Mới  ",
                LocalDate.of(1996, 1, 2),
                GioiTinh.NU,
                "  20 Bạch Đằng, Đà Nẵng  ",
                "0911223344",
                "  HOA.NEW@EXAMPLE.COM  ",
                "048195000999");

        KhachHangProfileResponse updated = profileService.updateProfileKhachHang(5L, request);

        assertEquals("Trần Thị Hoa Mới", updated.hoTen());
        assertEquals(LocalDate.of(1996, 1, 2), updated.ngaySinh());
        assertEquals(GioiTinh.NU, updated.gioiTinh());
        assertEquals("20 Bạch Đằng, Đà Nẵng", updated.diaChi());
        assertEquals("0911223344", updated.soDienThoai());
        assertEquals("hoa.new@example.com", updated.email());
        assertEquals("048195000999", updated.soCccd());
        assertEquals("MKH-0020", updated.maKh());
        assertTrue(updated.laKhMua());
        assertFalse(updated.laKhBan());
        assertEquals(TrangThaiDuyet.DA_DUYET, updated.trangThaiDuyet());
        verify(khachHangRepository).save(khachHang);
    }

    @Test
    void shouldNormalizeBlankSoCccdToNullAndSkipDuplicateCheck() {
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));
        when(khachHangRepository.existsByEmailIgnoreCaseAndIdNot("hoa.tran@example.com", 20L)).thenReturn(false);
        when(nhanVienRepository.existsByEmailIgnoreCase("hoa.tran@example.com")).thenReturn(false);
        when(khachHangRepository.save(any(KhachHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateKhachHangProfileRequest request = new UpdateKhachHangProfileRequest(
                "Trần Thị Hoa",
                null,
                null,
                "   ",
                "0905123456",
                "hoa.tran@example.com",
                "   ");

        KhachHangProfileResponse updated = profileService.updateProfileKhachHang(5L, request);

        assertNull(updated.soCccd());
        assertNull(updated.diaChi());
        verify(khachHangRepository, never()).countSoCccdIncludingDeleted(anyString(), anyLong());
    }

    @Test
    void shouldThrowFieldConflictWhenEmailAlreadyExistsInKhachHang() {
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));
        when(khachHangRepository.existsByEmailIgnoreCaseAndIdNot("dup@example.com", 20L)).thenReturn(true);

        UpdateKhachHangProfileRequest request = new UpdateKhachHangProfileRequest(
                "Trần Thị Hoa",
                LocalDate.of(1995, 8, 15),
                GioiTinh.NU,
                "10 Lê Lợi",
                "0905123456",
                "dup@example.com",
                "048195000123");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> profileService.updateProfileKhachHang(5L, request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("email", exception.getErrors().get(0).field());
        assertEquals("Email đã được sử dụng", exception.getErrors().get(0).message());
        verify(khachHangRepository, never()).save(any());
    }

    @Test
    void shouldThrowFieldConflictWhenEmailAlreadyExistsInNhanVien() {
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));
        when(khachHangRepository.existsByEmailIgnoreCaseAndIdNot("nv@example.com", 20L)).thenReturn(false);
        when(nhanVienRepository.existsByEmailIgnoreCase("nv@example.com")).thenReturn(true);

        UpdateKhachHangProfileRequest request = new UpdateKhachHangProfileRequest(
                "Trần Thị Hoa",
                LocalDate.of(1995, 8, 15),
                GioiTinh.NU,
                "10 Lê Lợi",
                "0905123456",
                "nv@example.com",
                "048195000123");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> profileService.updateProfileKhachHang(5L, request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("email", exception.getErrors().get(0).field());
        verify(khachHangRepository, never()).save(any());
    }

    @Test
    void shouldThrowFieldConflictWhenSoCccdAlreadyExists() {
        when(taiKhoanRepository.findWithChuSoHuuById(5L)).thenReturn(Optional.of(taiKhoan));
        when(khachHangRepository.existsByEmailIgnoreCaseAndIdNot("hoa.tran@example.com", 20L)).thenReturn(false);
        when(nhanVienRepository.existsByEmailIgnoreCase("hoa.tran@example.com")).thenReturn(false);
        when(khachHangRepository.countSoCccdIncludingDeleted("048195000888", 20L)).thenReturn(1L);

        UpdateKhachHangProfileRequest request = new UpdateKhachHangProfileRequest(
                "Trần Thị Hoa",
                LocalDate.of(1995, 8, 15),
                GioiTinh.NU,
                "10 Lê Lợi",
                "0905123456",
                "hoa.tran@example.com",
                "048195000888");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> profileService.updateProfileKhachHang(5L, request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("soCccd", exception.getErrors().get(0).field());
        assertEquals("Số CCCD đã tồn tại", exception.getErrors().get(0).message());
        verify(khachHangRepository, never()).save(any());
    }
}

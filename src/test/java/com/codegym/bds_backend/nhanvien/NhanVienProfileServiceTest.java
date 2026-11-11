package com.codegym.bds_backend.nhanvien;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.nhanvien.dto.NhanVienProfileResponse;
import com.codegym.bds_backend.nhanvien.dto.UpdateNhanVienProfileRequest;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NhanVienProfileServiceTest {

    @Mock
    private NhanVienRepository nhanVienRepository;

    @InjectMocks
    private NhanVienProfileService profileService;

    private NhanVien nhanVien;

    @BeforeEach
    void setUp() {
        nhanVien = new NhanVien();
        nhanVien.setId(101L);
        nhanVien.setMaNv("MNV-101");
        nhanVien.setHoTen("Nguyễn Thành Long");
        nhanVien.setNgaySinh(LocalDate.of(1988, 3, 14));
        nhanVien.setGioiTinh(GioiTinh.NAM);
        nhanVien.setDiaChi("12 ngõ 34 đường Nguyễn Trãi, Hà Nội");
        nhanVien.setSoDienThoai("0912345601");
        nhanVien.setEmail("long.nt@example.com");
        nhanVien.setChucVu(ChucVu.TRUONG_PHONG);
        nhanVien.setLaAdmin(true);
    }

    @Test
    void shouldGetProfileNhanVienSuccessfully() {
        when(nhanVienRepository.findByTaiKhoanId(1L)).thenReturn(Optional.of(nhanVien));

        NhanVienProfileResponse response = profileService.getProfileNhanVien(1L);

        assertNotNull(response);
        assertEquals("MNV-101", response.maNv());
        assertEquals("Nguyễn Thành Long", response.hoTen());
        assertEquals("long.nt@example.com", response.email());
        assertEquals(ChucVu.TRUONG_PHONG, response.chucVu());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNhanVienNotFound() {
        when(nhanVienRepository.findByTaiKhoanId(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> profileService.getProfileNhanVien(999L));
    }

    @Test
    void shouldUpdateProfileNhanVienSuccessfully() {
        when(nhanVienRepository.findByTaiKhoanId(1L)).thenReturn(Optional.of(nhanVien));
        when(nhanVienRepository.existsByEmailIgnoreCaseAndIdNot("long.new@example.com", 101L)).thenReturn(false);
        when(nhanVienRepository.save(any(NhanVien.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateNhanVienProfileRequest request = new UpdateNhanVienProfileRequest(
                "Nguyễn Thành Long Mới",
                LocalDate.of(1990, 5, 20),
                GioiTinh.NAM,
                "Địa chỉ mới cập nhật",
                "0987654321",
                "long.new@example.com");

        NhanVienProfileResponse updated = profileService.updateProfileNhanVien(1L, request);

        assertEquals("Nguyễn Thành Long Mới", updated.hoTen());
        assertEquals("long.new@example.com", updated.email());
        assertEquals("0987654321", updated.soDienThoai());
        assertEquals("Địa chỉ mới cập nhật", updated.diaChi());
        assertEquals(ChucVu.TRUONG_PHONG, updated.chucVu());
        verify(nhanVienRepository).save(nhanVien);
    }

    @Test
    void shouldThrowBusinessExceptionWhenEmailAlreadyExistsForAnotherNhanVien() {
        when(nhanVienRepository.findByTaiKhoanId(1L)).thenReturn(Optional.of(nhanVien));
        when(nhanVienRepository.existsByEmailIgnoreCaseAndIdNot("duplicate@example.com", 101L)).thenReturn(true);

        UpdateNhanVienProfileRequest request = new UpdateNhanVienProfileRequest(
                "Nguyễn Thành Long",
                LocalDate.of(1988, 3, 14),
                GioiTinh.NAM,
                "Địa chỉ",
                "0912345601",
                "duplicate@example.com");

        assertThrows(BusinessException.class, () -> profileService.updateProfileNhanVien(1L, request));
        verify(nhanVienRepository, never()).save(any());
    }
}


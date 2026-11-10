package com.codegym.bds_backend.nhanvien;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
import com.codegym.bds_backend.nhanvien.dto.UpdateNhanVienRequest;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NhanVienServiceTest {
    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    private NhanVienService service;

    @BeforeEach
    void setUp() {
        service = new NhanVienService(nhanVienRepository, khachHangRepository);
    }

    @Test
    void shouldFindNhanVienByIdSuccessfully() {
        NhanVien nv = createSampleNhanVien(1L, "MNV-001", "Trần Thị B", "b@example.com", "0912345679");
        when(nhanVienRepository.findById(1L)).thenReturn(Optional.of(nv));

        NhanVienResponse response = service.findNhanVienById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("MNV-001", response.maNv());
        assertEquals("Trần Thị B", response.hoTen());
    }

    @Test
    void shouldThrowNotFoundWhenFindingNonExistentId() {
        when(nhanVienRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findNhanVienById(999L));
    }

    @Test
    void shouldUpdateNhanVienSuccessfully() {
        NhanVien existing = createSampleNhanVien(1L, "MNV-001", "Cũ", "cu@example.com", "0912345678");
        when(nhanVienRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(nhanVienRepository.existsByEmailIgnoreCaseAndIdNot("moi@example.com", 1L)).thenReturn(false);
        when(khachHangRepository.existsByEmailIgnoreCase("moi@example.com")).thenReturn(false);
        when(nhanVienRepository.existsBySoDienThoaiAndIdNot("0987654321", 1L)).thenReturn(false);
        when(nhanVienRepository.save(any(NhanVien.class))).thenAnswer(i -> i.getArgument(0));

        UpdateNhanVienRequest request = new UpdateNhanVienRequest(
                "Mới",
                LocalDate.of(1992, 1, 1),
                GioiTinh.NU,
                "Địa chỉ mới",
                "0987654321",
                "moi@example.com",
                ChucVu.KE_TOAN,
                true);

        NhanVienResponse response = service.updateNhanVien(1L, request);

        assertNotNull(response);
        assertEquals("Mới", response.hoTen());
        assertEquals("moi@example.com", response.email());
        assertEquals("0987654321", response.soDienThoai());
        assertEquals(ChucVu.KE_TOAN, response.chucVu());
        assertTrue(response.laAdmin());
    }

    @Test
    void shouldThrowWhenUpdatingWithDuplicateEmail() {
        NhanVien existing = createSampleNhanVien(1L, "MNV-001", "Tên", "cu@example.com", "0912345678");
        when(nhanVienRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(nhanVienRepository.existsByEmailIgnoreCaseAndIdNot("trung@example.com", 1L)).thenReturn(true);

        UpdateNhanVienRequest request = new UpdateNhanVienRequest(
                "Tên",
                LocalDate.of(1992, 1, 1),
                GioiTinh.NAM,
                "Địa chỉ",
                "0912345678",
                "trung@example.com",
                ChucVu.NHAN_VIEN,
                false);

        BusinessException e = assertThrows(BusinessException.class, () -> service.updateNhanVien(1L, request));
        assertEquals("Email đã được sử dụng", e.getMessage());
    }

    @Test
    void shouldThrowWhenUpdatingWithDuplicatePhone() {
        NhanVien existing = createSampleNhanVien(1L, "MNV-001", "Tên", "email@example.com", "0912345678");
        when(nhanVienRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(nhanVienRepository.existsBySoDienThoaiAndIdNot("0999999999", 1L)).thenReturn(true);

        UpdateNhanVienRequest request = new UpdateNhanVienRequest(
                "Tên",
                LocalDate.of(1992, 1, 1),
                GioiTinh.NAM,
                "Địa chỉ",
                "0999999999",
                "email@example.com",
                ChucVu.NHAN_VIEN,
                false);

        BusinessException e = assertThrows(BusinessException.class, () -> service.updateNhanVien(1L, request));
        assertEquals("Số điện thoại đã được sử dụng", e.getMessage());
    }

    private static NhanVien createSampleNhanVien(
            Long id, String maNv, String hoTen, String email, String phone) {
        NhanVien nv = new NhanVien();
        nv.setId(id);
        nv.setMaNv(maNv);
        nv.setHoTen(hoTen);
        nv.setNgaySinh(LocalDate.of(1990, 1, 1));
        nv.setGioiTinh(GioiTinh.NAM);
        nv.setDiaChi("123 Phố Huế, Hà Nội");
        nv.setSoDienThoai(phone);
        nv.setEmail(email);
        nv.setChucVu(ChucVu.NHAN_VIEN);
        nv.setLaAdmin(false);
        return nv;
    }
}

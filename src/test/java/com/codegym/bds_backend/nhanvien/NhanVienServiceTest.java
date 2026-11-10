package com.codegym.bds_backend.nhanvien;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NhanVienServiceTest {
    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    private NhanVienService service;

    @BeforeEach
    void setUp() {
        service = new NhanVienService(nhanVienRepository, taiKhoanRepository);
    }

    @Test
    void shouldFindDanhSachNhanVienWithPagingAndFilter() {
        NhanVien nv = createSampleNhanVien(1L, "MNV-001", "Nguyễn Văn A", "a@example.com", "0912345678");
        Pageable pageable = PageRequest.of(0, 10);
        when(nhanVienRepository.searchNhanVien(
                eq("MNV-001"), eq("Nguyễn"), eq(ChucVu.NHAN_VIEN), eq("a@example.com"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(nv), pageable, 1));

        PageResponse<NhanVienResponse> result = service.findDanhSachNhanVien(
                "MNV-001", "Nguyễn", ChucVu.NHAN_VIEN, "a@example.com", pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals("MNV-001", result.content().get(0).maNv());
        assertEquals("Nguyễn Văn A", result.content().get(0).hoTen());
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
    void shouldDeleteNhanVienSuccessfully() {
        NhanVien existing = createSampleNhanVien(5L, "MNV-005", "Xóa", "xoa@example.com", "0912345688");
        when(nhanVienRepository.findById(5L)).thenReturn(Optional.of(existing));

        JwtPrincipal principal = new JwtPrincipal(1L, "admin", List.of("ROLE_ADMIN"));
        TaiKhoan adminAccount = new TaiKhoan();
        adminAccount.setId(1L);
        NhanVien adminNv = createSampleNhanVien(1L, "MNV-001", "Admin", "admin@example.com", "0912345601");
        adminAccount.setNhanVien(adminNv);

        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(adminAccount));

        service.deleteNhanVien(5L, principal);

        verify(nhanVienRepository).delete(existing);
    }

    @Test
    void shouldThrowWhenAdminDeletesSelf() {
        NhanVien adminNv = createSampleNhanVien(1L, "MNV-001", "Admin", "admin@example.com", "0912345601");
        when(nhanVienRepository.findById(1L)).thenReturn(Optional.of(adminNv));

        JwtPrincipal principal = new JwtPrincipal(101L, "admin", List.of("ROLE_ADMIN"));
        TaiKhoan adminAccount = new TaiKhoan();
        adminAccount.setId(101L);
        adminAccount.setNhanVien(adminNv);

        when(taiKhoanRepository.findWithChuSoHuuById(101L)).thenReturn(Optional.of(adminAccount));

        BusinessException e = assertThrows(BusinessException.class, () -> service.deleteNhanVien(1L, principal));
        assertEquals("Admin không thể tự xóa tài khoản của chính mình", e.getMessage());
        verify(nhanVienRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenDeletingNonExistentId() {
        when(nhanVienRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteNhanVien(999L, null));
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


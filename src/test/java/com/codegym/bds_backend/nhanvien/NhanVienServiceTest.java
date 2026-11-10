package com.codegym.bds_backend.nhanvien;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.auth.TaiKhoanService;
import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhanvien.dto.CreateNhanVienRequest;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
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
    private TaiKhoanRepository taiKhoanRepository;

    @Mock
    private TaiKhoanService taiKhoanService;

    @Mock
    private KhachHangRepository khachHangRepository;

    private NhanVienService service;

    @BeforeEach
    void setUp() {
        service = new NhanVienService(
                nhanVienRepository,
                taiKhoanRepository,
                taiKhoanService,
                khachHangRepository);
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
    void shouldCreateNhanVienSuccessfully() {
        CreateNhanVienRequest request = new CreateNhanVienRequest(
                "Lê Văn C",
                LocalDate.of(1995, 5, 20),
                GioiTinh.NAM,
                "123 Nguyễn Huệ, Đà Nẵng",
                "0912345680",
                "c.le@example.com",
                ChucVu.NHAN_VIEN,
                false,
                "c.le");

        when(nhanVienRepository.existsByEmailIgnoreCase("c.le@example.com")).thenReturn(false);
        when(khachHangRepository.existsByEmailIgnoreCase("c.le@example.com")).thenReturn(false);
        when(nhanVienRepository.existsBySoDienThoai("0912345680")).thenReturn(false);
        when(taiKhoanRepository.existsByTenDangNhap("c.le")).thenReturn(false);

        NhanVien saved = createSampleNhanVien(10L, null, "Lê Văn C", "c.le@example.com", "0912345680");
        when(nhanVienRepository.saveAndFlush(any(NhanVien.class))).thenReturn(saved);

        NhanVienResponse response = service.createNhanVien(request);

        assertNotNull(response);
        assertEquals("MNV-010", response.maNv());
        assertEquals("Lê Văn C", response.hoTen());
        verify(taiKhoanService).createTaiKhoanChoNhanVien(saved, "c.le");
    }

    @Test
    void shouldThrowWhenCreatingWithExistingEmail() {
        CreateNhanVienRequest request = new CreateNhanVienRequest(
                "Lê Văn C",
                LocalDate.of(1995, 5, 20),
                GioiTinh.NAM,
                "123 Nguyễn Huệ",
                "0912345680",
                "trung@example.com",
                ChucVu.NHAN_VIEN,
                false,
                null);

        when(nhanVienRepository.existsByEmailIgnoreCase("trung@example.com")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> service.createNhanVien(request));
        assertEquals("Email đã được sử dụng", e.getMessage());
        verify(nhanVienRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowWhenCreatingWithExistingPhone() {
        CreateNhanVienRequest request = new CreateNhanVienRequest(
                "Lê Văn C",
                LocalDate.of(1995, 5, 20),
                GioiTinh.NAM,
                "123 Nguyễn Huệ",
                "0912345680",
                "c@example.com",
                ChucVu.NHAN_VIEN,
                false,
                null);

        when(nhanVienRepository.existsByEmailIgnoreCase("c@example.com")).thenReturn(false);
        when(khachHangRepository.existsByEmailIgnoreCase("c@example.com")).thenReturn(false);
        when(nhanVienRepository.existsBySoDienThoai("0912345680")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> service.createNhanVien(request));
        assertEquals("Số điện thoại đã được sử dụng", e.getMessage());
        verify(nhanVienRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowWhenCreatingWithExistingUsername() {
        CreateNhanVienRequest request = new CreateNhanVienRequest(
                "Lê Văn C",
                LocalDate.of(1995, 5, 20),
                GioiTinh.NAM,
                "123 Nguyễn Huệ",
                "0912345680",
                "c@example.com",
                ChucVu.NHAN_VIEN,
                false,
                "c_user");

        when(nhanVienRepository.existsByEmailIgnoreCase("c@example.com")).thenReturn(false);
        when(khachHangRepository.existsByEmailIgnoreCase("c@example.com")).thenReturn(false);
        when(nhanVienRepository.existsBySoDienThoai("0912345680")).thenReturn(false);
        when(taiKhoanRepository.existsByTenDangNhap("c_user")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> service.createNhanVien(request));
        assertEquals("Tên đăng nhập đã tồn tại", e.getMessage());
        verify(nhanVienRepository, never()).saveAndFlush(any());
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

package com.codegym.bds_backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.dto.ChangePasswordRequest;
import com.codegym.bds_backend.auth.dto.LoginRequest;
import com.codegym.bds_backend.auth.dto.LoginResponse;
import com.codegym.bds_backend.auth.dto.RegisterRequest;
import com.codegym.bds_backend.auth.dto.ThongTinTaiKhoan;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.config.AppProperties;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhanvien.NhanVien;
import com.codegym.bds_backend.nhanvien.NhanVienRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private TokenXacThucService tokenXacThucService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties(
                new AppProperties.Security(5, 24, 30, new AppProperties.Jwt("x", 60)),
                new AppProperties.Cors(List.of()),
                new AppProperties.Mail("no-reply@test.local", "http://localhost:5173"),
                new AppProperties.Cloudinary("", "", ""),
                new AppProperties.BootstrapAdmin("admin", "Quản trị viên", "admin@test.local", "0900000000"));
        service = new AuthService(
                taiKhoanRepository,
                khachHangRepository,
                nhanVienRepository,
                tokenXacThucService,
                tokenBlacklistService,
                mailService,
                passwordEncoder,
                jwtService,
                props);
    }

    @Test
    void shouldLockAccountAfterFifthWrongPassword() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(4, false);
        when(taiKhoanRepository.findWithChuSoHuuByTenDangNhap("nv101")).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("sai", "hash")).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.loginTaiKhoan(new LoginRequest("nv101", "sai")));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertTrue(taiKhoan.isBiKhoa());
        assertEquals(5, taiKhoan.getSoLanDangNhapSai());
    }

    @Test
    void shouldCountWrongPasswordBeforeLimit() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(1, false);
        when(taiKhoanRepository.findWithChuSoHuuByTenDangNhap("nv101")).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("sai", "hash")).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.loginTaiKhoan(new LoginRequest("nv101", "sai")));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        assertFalse(taiKhoan.isBiKhoa());
        assertEquals(2, taiKhoan.getSoLanDangNhapSai());
    }

    @Test
    void shouldRejectLockedAccountWithoutCheckingPassword() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(5, false);
        taiKhoan.setBiKhoa(true);
        when(taiKhoanRepository.findWithChuSoHuuByTenDangNhap("nv101")).thenReturn(Optional.of(taiKhoan));

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.loginTaiKhoan(new LoginRequest("nv101", "dung")));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void shouldResetCounterAndIssueTokenWithRolesOnSuccess() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(3, true);
        when(taiKhoanRepository.findWithChuSoHuuByTenDangNhap("nv101")).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("dung", "hash")).thenReturn(true);
        when(jwtService.generateToken(any(JwtPrincipal.class))).thenReturn("jwt");

        LoginResponse response = service.loginTaiKhoan(new LoginRequest("nv101", "dung"));

        assertEquals("jwt", response.token());
        assertEquals(0, taiKhoan.getSoLanDangNhapSai());
        assertEquals(List.of("ROLE_NHAN_VIEN", "ROLE_ADMIN"), response.taiKhoan().vaiTro());
    }

    @Test
    void shouldRejectRegistrationWithExistingCccd() {
        when(taiKhoanRepository.existsByTenDangNhap("khach01")).thenReturn(false);
        when(khachHangRepository.existsBySoCccd("012345678901")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> service.registerKhachHang(request()));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        verify(khachHangRepository, never()).saveAndFlush(any(KhachHang.class));
    }

    @Test
    void shouldRejectRegistrationWithExistingEmail() {
        when(taiKhoanRepository.existsByTenDangNhap("khach01")).thenReturn(false);
        when(khachHangRepository.existsBySoCccd("012345678901")).thenReturn(false);
        when(khachHangRepository.existsByEmailIgnoreCase("an@example.com")).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> service.registerKhachHang(request()));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        verify(khachHangRepository, never()).saveAndFlush(any(KhachHang.class));
    }

    @Test
    void shouldCreateKhachHangWithDisplayCodeAndHashedPassword() {
        when(taiKhoanRepository.existsByTenDangNhap("khach01")).thenReturn(false);
        when(khachHangRepository.existsBySoCccd("012345678901")).thenReturn(false);
        when(khachHangRepository.existsByEmailIgnoreCase("an@example.com")).thenReturn(false);
        when(nhanVienRepository.existsByEmailIgnoreCase("an@example.com")).thenReturn(false);
        when(khachHangRepository.saveAndFlush(any(KhachHang.class))).thenAnswer(call -> {
            KhachHang khachHang = call.getArgument(0);
            khachHang.setId(12L);
            return khachHang;
        });
        when(passwordEncoder.encode("matkhau123")).thenReturn("encoded");
        when(taiKhoanRepository.save(any(TaiKhoan.class))).thenAnswer(call -> {
            TaiKhoan taiKhoan = call.getArgument(0);
            taiKhoan.setId(7L);
            return taiKhoan;
        });

        ThongTinTaiKhoan result = service.registerKhachHang(request());

        assertEquals(7L, result.id());
        assertEquals(List.of("ROLE_KHACH_HANG"), result.vaiTro());
        ArgumentCaptor<KhachHang> captor = ArgumentCaptor.forClass(KhachHang.class);
        verify(khachHangRepository).saveAndFlush(captor.capture());
        assertEquals("KH012", captor.getValue().getMaKh());
    }

    @Test
    void shouldRevokeTokenAndClearSecurityContextWhenLogoutWithValidBearer() {
        String token = "valid-mock-jwt-token";
        String authHeader = "Bearer " + token;
        Date expiration = new Date(System.currentTimeMillis() + 60000);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "pass", List.of()));

        when(jwtService.extractExpiration(token)).thenReturn(Optional.of(expiration));

        service.logoutTaiKhoan(authHeader);

        verify(tokenBlacklistService).revokeToken(eq(token), eq(expiration));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldClearSecurityContextEvenWhenAuthHeaderIsBlank() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "pass", List.of()));

        service.logoutTaiKhoan(null);

        verify(tokenBlacklistService, never()).revokeToken(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(2, false);
        when(taiKhoanRepository.findById(1L)).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("OldPass123", "hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPass123", "hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPass123")).thenReturn("new-hash");

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123", "NewPass123", "NewPass123");

        service.changePasswordTaiKhoan(1L, request);

        assertEquals("new-hash", taiKhoan.getMatKhauHash());
        assertEquals(0, taiKhoan.getSoLanDangNhapSai());
        assertFalse(taiKhoan.isBiKhoa());
        verify(taiKhoanRepository).save(taiKhoan);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenConfirmPasswordDoesNotMatch() {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123", "NewPass123", "MismatchPass");
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(taiKhoanRepository.findById(1L)).thenReturn(Optional.of(taiKhoan));

        BadRequestException ex = assertThrows(
                BadRequestException.class, () -> service.changePasswordTaiKhoan(1L, request));
        assertEquals("Mật khẩu mới phải từ 6 đến 32 ký tự và khớp nhau", ex.getMessage());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessExceptionWhenOldPasswordIsIncorrect() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(taiKhoanRepository.findById(1L)).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("WrongOldPass", "hash")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("WrongOldPass", "NewPass123", "NewPass123");

        BusinessException ex = assertThrows(
                BusinessException.class, () -> service.changePasswordTaiKhoan(1L, request));
        assertEquals("Mật khẩu cũ không đúng", ex.getMessage());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessExceptionWhenNewPasswordMatchesOldPassword() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(taiKhoanRepository.findById(1L)).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("SamePass123", "hash")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest("SamePass123", "SamePass123", "SamePass123");

        BusinessException ex = assertThrows(
                BusinessException.class, () -> service.changePasswordTaiKhoan(1L, request));
        assertEquals("Mật khẩu mới không được trùng với mật khẩu cũ", ex.getMessage());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExist() {
        when(taiKhoanRepository.findById(999L)).thenReturn(Optional.empty());

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass123", "NewPass123", "NewPass123");

        assertThrows(NotFoundException.class, () -> service.changePasswordTaiKhoan(999L, request));
    }

    private static RegisterRequest request() {
        return new RegisterRequest(
                "Nguyễn Văn An", "0912345678", "an@example.com", "012345678901", "khach01", "matkhau123");
    }

    private static TaiKhoan taiKhoanNhanVien(int soLanSai, boolean laAdmin) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen("Nguyễn Thành Long");
        nhanVien.setLaAdmin(laAdmin);
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(1L);
        taiKhoan.setTenDangNhap("nv101");
        taiKhoan.setMatKhauHash("hash");
        taiKhoan.setNhanVien(nhanVien);
        taiKhoan.setSoLanDangNhapSai(soLanSai);
        return taiKhoan;
    }
}

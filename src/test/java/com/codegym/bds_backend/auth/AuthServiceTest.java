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
import com.codegym.bds_backend.auth.dto.SetPasswordRequest;
import com.codegym.bds_backend.auth.dto.ThongTinTaiKhoan;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
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
        assertNull(response.taiKhoan().laKhBan());
        assertNull(response.taiKhoan().trangThaiDuyet());
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
        stubSuccessfulRegistration();

        ThongTinTaiKhoan result = service.registerKhachHang(request());

        assertEquals(7L, result.id());
        assertEquals(List.of("ROLE_KHACH_HANG"), result.vaiTro());
        ArgumentCaptor<KhachHang> captor = ArgumentCaptor.forClass(KhachHang.class);
        verify(khachHangRepository).saveAndFlush(captor.capture());
        assertEquals("KH012", captor.getValue().getMaKh());
    }

    @Test
    void shouldSetChoDuyetAndLaKhBanWhenRegisterWithMuonDangTinBan() {
        stubSuccessfulRegistration();

        ThongTinTaiKhoan result = service.registerKhachHang(request(true));

        ArgumentCaptor<KhachHang> captor = ArgumentCaptor.forClass(KhachHang.class);
        verify(khachHangRepository).saveAndFlush(captor.capture());
        KhachHang khachHang = captor.getValue();
        assertTrue(khachHang.isLaKhMua());
        assertTrue(khachHang.isLaKhBan());
        assertEquals(TrangThaiDuyet.CHO_DUYET, khachHang.getTrangThaiDuyet());
        assertEquals(Boolean.TRUE, result.laKhBan());
        assertEquals("CHO_DUYET", result.trangThaiDuyet());
    }

    @Test
    void shouldSetDaDuyetWithoutLaKhBanWhenRegisterWithoutMuonDangTinBan() {
        stubSuccessfulRegistration();

        ThongTinTaiKhoan result = service.registerKhachHang(request(false));

        ArgumentCaptor<KhachHang> captor = ArgumentCaptor.forClass(KhachHang.class);
        verify(khachHangRepository).saveAndFlush(captor.capture());
        KhachHang khachHang = captor.getValue();
        assertTrue(khachHang.isLaKhMua());
        assertFalse(khachHang.isLaKhBan());
        assertEquals(TrangThaiDuyet.DA_DUYET, khachHang.getTrangThaiDuyet());
        assertEquals(Boolean.FALSE, result.laKhBan());
        assertEquals("DA_DUYET", result.trangThaiDuyet());
    }

    @Test
    void shouldTreatNullMuonDangTinBanAsFalseWhenRegister() {
        stubSuccessfulRegistration();

        ThongTinTaiKhoan result = service.registerKhachHang(request(null));

        assertEquals(Boolean.FALSE, result.laKhBan());
        assertEquals("DA_DUYET", result.trangThaiDuyet());
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
    void shouldRejectRegistrationWhenPasswordContainsUsername() {
        RegisterRequest req = new RegisterRequest(
                "Nguyễn Văn An", "0912345678", "an@example.com", "012345678901",
                "khach01", "MyKhach01@Pass", false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.registerKhachHang(req));

        assertEquals("Mật khẩu không được chứa tên đăng nhập hoặc email", ex.getMessage());
        assertEquals("matKhau", ex.getErrors().get(0).field());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldRejectRegistrationWhenPasswordContainsEmailLocalPart() {
        RegisterRequest req = new RegisterRequest(
                "Nguyễn Văn An", "0912345678", "nguyenvanan@example.com", "012345678901",
                "kh01", "MyNguyenvanan@1", false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.registerKhachHang(req));

        assertEquals("Mật khẩu không được chứa tên đăng nhập hoặc email", ex.getMessage());
        assertEquals("matKhau", ex.getErrors().get(0).field());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(2, false);
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("OldPass@123", "hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPass@123", "hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("new-hash");

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass@123", "NewPass@123", "NewPass@123");

        service.changePasswordTaiKhoan(1L, request);

        assertEquals("new-hash", taiKhoan.getMatKhauHash());
        assertEquals(0, taiKhoan.getSoLanDangNhapSai());
        assertFalse(taiKhoan.isBiKhoa());
        verify(taiKhoanRepository).save(taiKhoan);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenConfirmPasswordDoesNotMatch() {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass@123", "NewPass@123", "Mismatch@123");
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));

        BadRequestException ex = assertThrows(
                BadRequestException.class, () -> service.changePasswordTaiKhoan(1L, request));
        assertEquals("Xác nhận mật khẩu không khớp", ex.getMessage());
        assertEquals("xacNhanMatKhau", ex.getErrors().get(0).field());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessExceptionWhenOldPasswordIsIncorrect() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("WrongOld@123", "hash")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("WrongOld@123", "NewPass@123", "NewPass@123");

        BusinessException ex = assertThrows(
                BusinessException.class, () -> service.changePasswordTaiKhoan(1L, request));
        assertEquals("Mật khẩu cũ không đúng", ex.getMessage());
        assertEquals("matKhauCu", ex.getErrors().get(0).field());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessExceptionWhenNewPasswordMatchesOldPassword() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("SamePass@123", "hash")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest("SamePass@123", "SamePass@123", "SamePass@123");

        BusinessException ex = assertThrows(
                BusinessException.class, () -> service.changePasswordTaiKhoan(1L, request));
        assertEquals("Mật khẩu mới không được trùng với mật khẩu cũ", ex.getMessage());
        assertEquals("matKhauMoi", ex.getErrors().get(0).field());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldRejectChangePasswordWhenNewPasswordContainsUsername() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("OldPass@123", "hash")).thenReturn(true);
        when(passwordEncoder.matches("MyNv101@Pass", "hash")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass@123", "MyNv101@Pass", "MyNv101@Pass");

        BadRequestException ex = assertThrows(
                BadRequestException.class, () -> service.changePasswordTaiKhoan(1L, request));
        assertEquals("Mật khẩu không được chứa tên đăng nhập hoặc email", ex.getMessage());
        assertEquals("matKhauMoi", ex.getErrors().get(0).field());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldRejectChangePasswordWhenNewPasswordContainsEmailLocalPart() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));
        when(passwordEncoder.matches("OldPass@123", "hash")).thenReturn(true);
        when(passwordEncoder.matches("MyLong.nt@1", "hash")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass@123", "MyLong.nt@1", "MyLong.nt@1");

        BadRequestException ex = assertThrows(
                BadRequestException.class, () -> service.changePasswordTaiKhoan(1L, request));
        assertEquals("Mật khẩu không được chứa tên đăng nhập hoặc email", ex.getMessage());
        assertEquals("matKhauMoi", ex.getErrors().get(0).field());
        verify(taiKhoanRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExist() {
        when(taiKhoanRepository.findWithChuSoHuuById(999L)).thenReturn(Optional.empty());

        ChangePasswordRequest request = new ChangePasswordRequest("OldPass@123", "NewPass@123", "NewPass@123");

        assertThrows(NotFoundException.class, () -> service.changePasswordTaiKhoan(999L, request));
    }

    @Test
    void shouldSetPasswordSuccessfullyWhenAccountHasNoPasswordYet() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(3, false);
        taiKhoan.setMatKhauHash(null);
        taiKhoan.setBiKhoa(true);
        when(tokenXacThucService.consumeToken("raw-token")).thenReturn(taiKhoan);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("encoded-pass");

        service.setMatKhau(new SetPasswordRequest("raw-token", "NewPass@123"));

        assertEquals("encoded-pass", taiKhoan.getMatKhauHash());
        assertEquals(0, taiKhoan.getSoLanDangNhapSai());
        assertFalse(taiKhoan.isBiKhoa());
    }

    @Test
    void shouldRejectSetPasswordWhenMatchesOldPassword() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(tokenXacThucService.consumeToken("raw-token")).thenReturn(taiKhoan);
        when(passwordEncoder.matches("OldPass@123", "hash")).thenReturn(true);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.setMatKhau(new SetPasswordRequest("raw-token", "OldPass@123")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals("matKhau", ex.getErrors().get(0).field());
        assertEquals("Mật khẩu mới không được trùng với mật khẩu cũ", ex.getMessage());
    }

    @Test
    void shouldRejectSetPasswordWhenContainsUsername() {
        TaiKhoan taiKhoan = taiKhoanNhanVien(0, false);
        when(tokenXacThucService.consumeToken("raw-token")).thenReturn(taiKhoan);
        when(passwordEncoder.matches("PassNv101@1", "hash")).thenReturn(false);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> service.setMatKhau(new SetPasswordRequest("raw-token", "PassNv101@1")));

        assertEquals("matKhau", ex.getErrors().get(0).field());
        assertEquals("Mật khẩu không được chứa tên đăng nhập hoặc email", ex.getMessage());
    }

    private void stubSuccessfulRegistration() {
        when(taiKhoanRepository.existsByTenDangNhap("khach01")).thenReturn(false);
        when(khachHangRepository.existsBySoCccd("012345678901")).thenReturn(false);
        when(khachHangRepository.existsByEmailIgnoreCase("an@example.com")).thenReturn(false);
        when(nhanVienRepository.existsByEmailIgnoreCase("an@example.com")).thenReturn(false);
        when(khachHangRepository.saveAndFlush(any(KhachHang.class))).thenAnswer(call -> {
            KhachHang khachHang = call.getArgument(0);
            khachHang.setId(12L);
            return khachHang;
        });
        when(passwordEncoder.encode("MatKhau@123")).thenReturn("encoded");
        when(taiKhoanRepository.save(any(TaiKhoan.class))).thenAnswer(call -> {
            TaiKhoan taiKhoan = call.getArgument(0);
            taiKhoan.setId(7L);
            return taiKhoan;
        });
    }

    private static RegisterRequest request() {
        return request(null);
    }

    private static RegisterRequest request(Boolean muonDangTinBan) {
        return new RegisterRequest("Nguyễn Văn An", "0912345678", "an@example.com", "012345678901", "khach01",
                "MatKhau@123", muonDangTinBan);
    }

    private static TaiKhoan taiKhoanNhanVien(int soLanSai, boolean laAdmin) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen("Nguyễn Thành Long");
        nhanVien.setEmail("long.nt@example.com");
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

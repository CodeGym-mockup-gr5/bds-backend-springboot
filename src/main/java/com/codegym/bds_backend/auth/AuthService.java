package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.auth.dto.ChangePasswordRequest;
import com.codegym.bds_backend.auth.dto.ForgotPasswordRequest;
import com.codegym.bds_backend.auth.dto.LoginRequest;
import com.codegym.bds_backend.auth.dto.LoginResponse;
import com.codegym.bds_backend.auth.dto.RegisterRequest;
import com.codegym.bds_backend.auth.dto.SetPasswordRequest;
import com.codegym.bds_backend.auth.dto.ThongTinTaiKhoan;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.enums.VaiTro;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.util.MaHienThi;
import com.codegym.bds_backend.config.AppProperties;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhanvien.NhanVienRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String MSG_INVALID_LOGIN = "Tên đăng nhập hoặc mật khẩu không đúng";
    private static final String MSG_LOCKED =
            "Tài khoản đã bị khóa do nhập sai quá số lần cho phép. Vui lòng dùng chức năng quên mật khẩu";
    private static final String MSG_NO_PASSWORD =
            "Tài khoản chưa đặt mật khẩu. Vui lòng kiểm tra email lời mời hoặc dùng chức năng quên mật khẩu";
    private static final String MSG_CONTACT = "vui lòng liên hệ trung tâm để được hỗ trợ";

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final TokenXacThucService tokenXacThucService;
    private final TokenBlacklistService tokenBlacklistService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties props;

    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse loginTaiKhoan(LoginRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository
                .findWithChuSoHuuByTenDangNhap(request.tenDangNhap().trim())
                .orElseThrow(AuthService::invalidLogin);
        if (!taiKhoan.chuSoHuuConHieuLuc()) {
            throw invalidLogin();
        }
        if (taiKhoan.isBiKhoa()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, MSG_LOCKED);
        }
        if (taiKhoan.getMatKhauHash() == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, MSG_NO_PASSWORD);
        }
        if (!passwordEncoder.matches(request.matKhau(), taiKhoan.getMatKhauHash())) {
            if (recordFailedLogin(taiKhoan)) {
                throw new BusinessException(HttpStatus.FORBIDDEN, MSG_LOCKED);
            }
            throw invalidLogin();
        }
        taiKhoan.setSoLanDangNhapSai(0);
        ThongTinTaiKhoan thongTin = toThongTin(taiKhoan);
        String token = jwtService.generateToken(
                new JwtPrincipal(taiKhoan.getId(), taiKhoan.getTenDangNhap(), thongTin.vaiTro()));
        return new LoginResponse(token, thongTin);
    }

    public void logoutTaiKhoan(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            jwtService.extractExpiration(token).ifPresent(expiresAt ->
                    tokenBlacklistService.revokeToken(token, expiresAt));
        }
        SecurityContextHolder.clearContext();
        log.info("Người dùng đã đăng xuất thành công");
    }

    @Transactional
    public ThongTinTaiKhoan registerKhachHang(RegisterRequest request) {
        String tenDangNhap = request.tenDangNhap().trim();
        String email = request.email().trim().toLowerCase();
        String soCccd = request.soCccd() == null || request.soCccd().isBlank() ? null : request.soCccd().trim();
        if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        if (soCccd != null && khachHangRepository.existsBySoCccd(soCccd)) {
            throw new BusinessException("Số CCCD đã tồn tại, " + MSG_CONTACT);
        }
        if (khachHangRepository.existsByEmailIgnoreCase(email) || nhanVienRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email đã được sử dụng, " + MSG_CONTACT);
        }
        KhachHang khachHang = new KhachHang();
        khachHang.setHoTen(request.hoTen().trim());
        khachHang.setSoDienThoai(request.soDienThoai());
        khachHang.setEmail(email);
        khachHang.setSoCccd(soCccd);
        boolean muonDangTinBan = Boolean.TRUE.equals(request.muonDangTinBan());
        khachHang.setLaKhMua(true);
        khachHang.setLaKhBan(muonDangTinBan);
        khachHang.setTrangThaiDuyet(muonDangTinBan ? TrangThaiDuyet.CHO_DUYET : TrangThaiDuyet.DA_DUYET);
        khachHang = khachHangRepository.saveAndFlush(khachHang);
        khachHang.setMaKh(MaHienThi.format(MaHienThi.KHACH_HANG, khachHang.getId()));
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(tenDangNhap);
        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.matKhau()));
        taiKhoan.setKhachHang(khachHang);
        taiKhoan = taiKhoanRepository.save(taiKhoan);
        log.info("Đăng ký tài khoản khách hàng mới, id tài khoản {}", taiKhoan.getId());
        return toThongTin(taiKhoan);
    }

    @Transactional
    public void forgotMatKhau(ForgotPasswordRequest request) {
        taiKhoanRepository.findWithChuSoHuuByTenDangNhap(request.tenDangNhap().trim()).ifPresent(taiKhoan -> {
            String email = taiKhoan.emailLienHe();
            if (!taiKhoan.chuSoHuuConHieuLuc() || email == null || email.isBlank()) {
                log.warn("Không thể gửi email đặt lại mật khẩu cho tài khoản {}", taiKhoan.getId());
                return;
            }
            String token = tokenXacThucService.createToken(taiKhoan, LoaiToken.DAT_LAI_MAT_KHAU);
            mailService.sendDatLaiMatKhau(
                    email, taiKhoan.hoTenChuSoHuu(), token, props.security().resetPasswordTokenMinutes());
        });
    }

    @Transactional
    public void setMatKhau(SetPasswordRequest request) {
        TaiKhoan taiKhoan = tokenXacThucService.consumeToken(request.token());
        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.matKhau()));
        taiKhoan.setSoLanDangNhapSai(0);
        taiKhoan.setBiKhoa(false);
    }

    @Transactional
    public void changePasswordTaiKhoan(Long taiKhoanId, ChangePasswordRequest request) {
        TaiKhoan taiKhoan = taiKhoanRepository
                .findById(taiKhoanId)
                .filter(TaiKhoan::chuSoHuuConHieuLuc)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        if (!request.matKhauMoi().equals(request.xacNhanMatKhau())) {
            throw new BadRequestException("Mật khẩu mới phải từ 6 đến 32 ký tự và khớp nhau");
        }
        if (taiKhoan.getMatKhauHash() == null
                || !passwordEncoder.matches(request.matKhauCu(), taiKhoan.getMatKhauHash())) {
            throw new BusinessException("Mật khẩu cũ không đúng");
        }
        if (passwordEncoder.matches(request.matKhauMoi(), taiKhoan.getMatKhauHash())) {
            throw new BusinessException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.matKhauMoi()));
        taiKhoan.setSoLanDangNhapSai(0);
        taiKhoan.setBiKhoa(false);
        taiKhoanRepository.save(taiKhoan);
        log.info("Đổi mật khẩu thành công cho tài khoản {}", taiKhoan.getTenDangNhap());
    }

    @Transactional(readOnly = true)
    public ThongTinTaiKhoan findThongTinTaiKhoan(Long taiKhoanId) {
        TaiKhoan taiKhoan = taiKhoanRepository
                .findWithChuSoHuuById(taiKhoanId)
                .filter(TaiKhoan::chuSoHuuConHieuLuc)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        return toThongTin(taiKhoan);
    }

    private boolean recordFailedLogin(TaiKhoan taiKhoan) {
        int soLan = taiKhoan.getSoLanDangNhapSai() + 1;
        taiKhoan.setSoLanDangNhapSai(soLan);
        if (soLan >= props.security().maxFailedLogins()) {
            taiKhoan.setBiKhoa(true);
            log.warn("Khóa tài khoản {} sau {} lần đăng nhập sai", taiKhoan.getId(), soLan);
            return true;
        }
        return false;
    }

    private static ThongTinTaiKhoan toThongTin(TaiKhoan taiKhoan) {
        List<String> vaiTro = new ArrayList<>();
        if (taiKhoan.getNhanVien() != null) {
            vaiTro.add(VaiTro.NHAN_VIEN.authority());
            if (taiKhoan.getNhanVien().isLaAdmin()) {
                vaiTro.add(VaiTro.ADMIN.authority());
            }
        }
        KhachHang khachHang = taiKhoan.getKhachHang();
        Boolean laKhBan = null;
        String trangThaiDuyet = null;
        if (khachHang != null) {
            vaiTro.add(VaiTro.KHACH_HANG.authority());
            laKhBan = khachHang.isLaKhBan();
            trangThaiDuyet = khachHang.getTrangThaiDuyet() != null ? khachHang.getTrangThaiDuyet().name() : null;
        }
        return new ThongTinTaiKhoan(taiKhoan.getId(), taiKhoan.getTenDangNhap(), taiKhoan.hoTenChuSoHuu(),
                List.copyOf(vaiTro), laKhBan, trangThaiDuyet);
    }

    private static BusinessException invalidLogin() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, MSG_INVALID_LOGIN);
    }
}

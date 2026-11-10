package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.common.enums.VaiTro;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.config.AppProperties;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.nhanvien.NhanVien;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaiKhoanService {
    private final TaiKhoanRepository taiKhoanRepository;
    private final TokenXacThucService tokenXacThucService;
    private final MailService mailService;
    private final AppProperties props;

    @Transactional
    public TaiKhoan createTaiKhoanChoNhanVien(NhanVien nhanVien, String tenDangNhap) {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setNhanVien(nhanVien);
        return saveAndInvite(taiKhoan, tenDangNhap);
    }

    @Transactional
    public TaiKhoan createTaiKhoanChoKhachHang(KhachHang khachHang, String tenDangNhap) {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setKhachHang(khachHang);
        return saveAndInvite(taiKhoan, tenDangNhap);
    }

    @Transactional
    public void resendLoiMoi(Long taiKhoanId, JwtPrincipal nguoiGui) {
        TaiKhoan taiKhoan = taiKhoanRepository
                .findWithChuSoHuuById(taiKhoanId)
                .filter(TaiKhoan::chuSoHuuConHieuLuc)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        if (taiKhoan.getNhanVien() != null && !nguoiGui.roles().contains(VaiTro.ADMIN.authority())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Chỉ Admin được gửi lại lời mời cho nhân viên");
        }
        if (taiKhoan.getMatKhauHash() != null) {
            throw new BusinessException("Tài khoản đã đặt mật khẩu, không cần gửi lại lời mời");
        }
        sendLoiMoi(taiKhoan);
    }

    private TaiKhoan saveAndInvite(TaiKhoan taiKhoan, String tenDangNhap) {
        if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        taiKhoan.setTenDangNhap(tenDangNhap);
        TaiKhoan saved = taiKhoanRepository.save(taiKhoan);
        sendLoiMoi(saved);
        return saved;
    }

    private void sendLoiMoi(TaiKhoan taiKhoan) {
        String email = taiKhoan.emailLienHe();
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Hồ sơ chưa có email để gửi lời mời");
        }
        String token = tokenXacThucService.createToken(taiKhoan, LoaiToken.THIET_LAP_MAT_KHAU);
        mailService.sendLoiMoi(email, taiKhoan.hoTenChuSoHuu(), token, props.security().setPasswordTokenHours());
    }
}

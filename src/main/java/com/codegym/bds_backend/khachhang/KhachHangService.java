package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.auth.TaiKhoanService;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.enums.VaiTro;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.common.util.MaHienThi;
import com.codegym.bds_backend.khachhang.dto.CreateKhachHangRequest;
import com.codegym.bds_backend.khachhang.dto.KhachHangListItemResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangResponse;
import com.codegym.bds_backend.khachhang.dto.LoaiKhachHang;
import com.codegym.bds_backend.khachhang.dto.UpdateKhachHangRequest;
import com.codegym.bds_backend.nhanvien.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhachHangService {
    private static final String MSG_NOT_FOUND = "Không tìm thấy khách hàng";
    private static final String MSG_CHON_LOAI_KH = "Vui lòng chọn ít nhất một loại khách hàng";

    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiKhoanService taiKhoanService;

    @Transactional(readOnly = true)
    public PageResponse<KhachHangListItemResponse> findDanhSachKhachHang(
            String keyword, LoaiKhachHang loaiKh, Pageable pageable, JwtPrincipal principal) {
        boolean isAdmin = isAdmin(principal);
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Boolean laKhMua = loaiKh == LoaiKhachHang.MUA ? Boolean.TRUE : null;
        Boolean laKhBan = loaiKh == LoaiKhachHang.BAN ? Boolean.TRUE : null;

        Page<KhachHang> page = khachHangRepository.searchKhachHang(
                cleanKeyword, laKhMua, laKhBan, isAdmin, pageable);
        return PageResponse.of(page.map(kh -> KhachHangListItemResponse.fromEntity(kh, isAdmin)));
    }

    @Transactional(readOnly = true)
    public KhachHangResponse findKhachHangById(Long id, JwtPrincipal principal) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MSG_NOT_FOUND));
        TaiKhoan taiKhoan = taiKhoanRepository.findByKhachHangId(id).orElse(null);
        return KhachHangResponse.fromEntity(khachHang, taiKhoan, isAdmin(principal));
    }

    @Transactional
    public KhachHangResponse createKhachHang(CreateKhachHangRequest request, JwtPrincipal principal) {
        validateLoaiKhachHang(request.laKhMua(), request.laKhBan());

        String email = request.email();
        String tenDangNhap = request.tenDangNhap();
        if (tenDangNhap != null) {
            if (email == null) {
                throw BadRequestException.fieldError("email", "Vui lòng nhập email khi tạo tài khoản đăng nhập");
            }
            if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
                throw BusinessException.fieldConflict("tenDangNhap", "Tên đăng nhập đã tồn tại");
            }
        }

        if (email != null && (khachHangRepository.existsByEmailIgnoreCase(email)
                || nhanVienRepository.existsEmailIgnoreCaseIncludingDeleted(email))) {
            throw BusinessException.fieldConflict("email", "Email đã được sử dụng");
        }

        String soCccd = request.soCccd();
        if (soCccd != null && khachHangRepository.countAllSoCccdIncludingDeleted(soCccd) > 0) {
            throw BusinessException.fieldConflict("soCccd", "Số CCCD đã tồn tại");
        }

        KhachHang kh = new KhachHang();
        kh.setHoTen(request.hoTen().trim());
        kh.setLaKhMua(request.laKhMua());
        kh.setLaKhBan(request.laKhBan());
        kh.setTrangThaiDuyet(TrangThaiDuyet.DA_DUYET);
        kh.setNgaySinh(request.ngaySinh());
        kh.setGioiTinh(request.gioiTinh());
        kh.setDiaChi(request.diaChi());
        kh.setSoDienThoai(request.soDienThoai().trim());
        kh.setEmail(email);
        kh.setSoCccd(soCccd);

        kh = khachHangRepository.saveAndFlush(kh);
        kh.setMaKh(MaHienThi.format(MaHienThi.KHACH_HANG, kh.getId()));

        TaiKhoan taiKhoan = null;
        if (tenDangNhap != null) {
            taiKhoan = taiKhoanService.createTaiKhoanChoKhachHang(kh, tenDangNhap);
        }

        log.info("Tạo mới khách hàng thành công, id: {}, mã: {}", kh.getId(), kh.getMaKh());
        return KhachHangResponse.fromEntity(kh, taiKhoan, isAdmin(principal));
    }

    @Transactional
    public KhachHangResponse updateKhachHang(Long id, UpdateKhachHangRequest request, JwtPrincipal principal) {
        validateLoaiKhachHang(request.laKhMua(), request.laKhBan());

        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MSG_NOT_FOUND));
        TaiKhoan taiKhoan = taiKhoanRepository.findByKhachHangId(id).orElse(null);

        String newEmail = request.email();
        if (taiKhoan != null && newEmail == null) {
            throw BadRequestException.fieldError("email", "Khách hàng đã có tài khoản, không thể bỏ trống email");
        }
        if (newEmail != null && !newEmail.equalsIgnoreCase(kh.getEmail())) {
            if (khachHangRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, id)
                    || nhanVienRepository.existsEmailIgnoreCaseIncludingDeleted(newEmail)) {
                throw BusinessException.fieldConflict("email", "Email đã được sử dụng");
            }
        }

        boolean vuaBatKhachBan = !kh.isLaKhBan() && request.laKhBan();
        kh.setHoTen(request.hoTen().trim());
        kh.setLaKhMua(request.laKhMua());
        kh.setLaKhBan(request.laKhBan());
        if (vuaBatKhachBan) {
            kh.setTrangThaiDuyet(TrangThaiDuyet.DA_DUYET);
        }
        kh.setNgaySinh(request.ngaySinh());
        kh.setGioiTinh(request.gioiTinh());
        kh.setDiaChi(request.diaChi());
        kh.setSoDienThoai(request.soDienThoai().trim());
        kh.setEmail(newEmail);

        kh = khachHangRepository.save(kh);
        log.info("Cập nhật thông tin khách hàng thành công, id: {}", id);
        return KhachHangResponse.fromEntity(kh, taiKhoan, isAdmin(principal));
    }

    @Transactional
    public void deleteKhachHang(Long id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MSG_NOT_FOUND));
        khachHangRepository.delete(kh);
        log.info("Xóa mềm khách hàng thành công, id: {}", id);
    }

    private static void validateLoaiKhachHang(boolean laKhMua, boolean laKhBan) {
        if (!laKhMua && !laKhBan) {
            throw BadRequestException.fieldError("laKhMua", MSG_CHON_LOAI_KH);
        }
    }

    private static boolean isAdmin(JwtPrincipal principal) {
        return principal != null
                && principal.roles() != null
                && principal.roles().contains(VaiTro.ADMIN.authority());
    }
}

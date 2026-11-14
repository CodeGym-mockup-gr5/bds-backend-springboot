package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.khachhang.dto.KhachHangProfileResponse;
import com.codegym.bds_backend.khachhang.dto.UpdateKhachHangProfileRequest;
import com.codegym.bds_backend.nhanvien.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhachHangProfileService {

    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    @Transactional(readOnly = true)
    public KhachHangProfileResponse getProfileKhachHang(Long taiKhoanId) {
        return toResponse(findKhachHangByTaiKhoanId(taiKhoanId));
    }

    @Transactional
    public KhachHangProfileResponse updateProfileKhachHang(Long taiKhoanId, UpdateKhachHangProfileRequest request) {
        KhachHang khachHang = findKhachHangByTaiKhoanId(taiKhoanId);

        String email = request.email().trim().toLowerCase();
        if (khachHangRepository.existsByEmailIgnoreCaseAndIdNot(email, khachHang.getId())
                || nhanVienRepository.existsByEmailIgnoreCase(email)) {
            throw BusinessException.fieldConflict("email", "Email đã được sử dụng");
        }

        String soCccd = request.soCccd();
        if (soCccd != null && khachHangRepository.countSoCccdIncludingDeleted(soCccd, khachHang.getId()) > 0) {
            throw BusinessException.fieldConflict("soCccd", "Số CCCD đã tồn tại");
        }

        String diaChi = (request.diaChi() == null || request.diaChi().isBlank()) ? null : request.diaChi().trim();

        khachHang.setHoTen(request.hoTen().trim());
        khachHang.setNgaySinh(request.ngaySinh());
        khachHang.setGioiTinh(request.gioiTinh());
        khachHang.setDiaChi(diaChi);
        khachHang.setSoDienThoai(request.soDienThoai().trim());
        khachHang.setEmail(email);
        khachHang.setSoCccd(soCccd);

        KhachHang saved = khachHangRepository.save(khachHang);
        log.info("Khách hàng {} cập nhật thông tin cá nhân", saved.getId());
        return toResponse(saved);
    }

    private KhachHang findKhachHangByTaiKhoanId(Long taiKhoanId) {
        TaiKhoan taiKhoan = taiKhoanRepository.findWithChuSoHuuById(taiKhoanId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        KhachHang khachHang = taiKhoan.getKhachHang();
        if (khachHang == null || khachHang.getDeletedAt() != null) {
            throw new NotFoundException("Không tìm thấy thông tin khách hàng cho tài khoản này");
        }
        return khachHang;
    }

    private KhachHangProfileResponse toResponse(KhachHang khachHang) {
        return new KhachHangProfileResponse(
                khachHang.getId(),
                khachHang.getMaKh(),
                khachHang.getHoTen(),
                khachHang.getNgaySinh(),
                khachHang.getGioiTinh(),
                khachHang.getDiaChi(),
                khachHang.getSoDienThoai(),
                khachHang.getEmail(),
                khachHang.getSoCccd(),
                khachHang.isLaKhMua(),
                khachHang.isLaKhBan(),
                khachHang.getTrangThaiDuyet());
    }
}

package com.codegym.bds_backend.nhanvien;

import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.nhanvien.dto.NhanVienProfileResponse;
import com.codegym.bds_backend.nhanvien.dto.UpdateNhanVienProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhanVienProfileService {

    private final NhanVienRepository nhanVienRepository;

    @Transactional(readOnly = true)
    public NhanVienProfileResponse getProfileNhanVien(Long taiKhoanId) {
        NhanVien nhanVien = findNhanVienByTaiKhoanId(taiKhoanId);
        return toResponse(nhanVien);
    }

    @Transactional
    public NhanVienProfileResponse updateProfileNhanVien(Long taiKhoanId, UpdateNhanVienProfileRequest request) {
        NhanVien nhanVien = findNhanVienByTaiKhoanId(taiKhoanId);

        String email = request.email().trim().toLowerCase();
        if (nhanVienRepository.existsByEmailIgnoreCaseAndIdNot(email, nhanVien.getId())) {
            throw new BusinessException("Email đã được sử dụng bởi nhân viên khác");
        }

        nhanVien.setHoTen(request.hoTen().trim());
        nhanVien.setNgaySinh(request.ngaySinh());
        nhanVien.setGioiTinh(request.gioiTinh());
        nhanVien.setDiaChi(request.diaChi().trim());
        nhanVien.setSoDienThoai(request.soDienThoai().trim());
        nhanVien.setEmail(email);

        NhanVien saved = nhanVienRepository.save(nhanVien);
        log.info("Cập nhật thông tin cá nhân thành công cho nhân viên: {}", saved.getMaNv());
        return toResponse(saved);
    }

    private NhanVien findNhanVienByTaiKhoanId(Long taiKhoanId) {
        return nhanVienRepository.findByTaiKhoanId(taiKhoanId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin nhân viên cho tài khoản này"));
    }

    private static NhanVienProfileResponse toResponse(NhanVien nv) {
        return new NhanVienProfileResponse(
                nv.getId(),
                nv.getMaNv(),
                nv.getHoTen(),
                nv.getNgaySinh(),
                nv.getGioiTinh(),
                nv.getDiaChi(),
                nv.getSoDienThoai(),
                nv.getEmail(),
                nv.getChucVu(),
                nv.isLaAdmin());
    }
}


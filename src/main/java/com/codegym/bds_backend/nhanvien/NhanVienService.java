package com.codegym.bds_backend.nhanvien;

import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
import com.codegym.bds_backend.nhanvien.dto.UpdateNhanVienRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhanVienService {
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;

    @Transactional(readOnly = true)
    public NhanVienResponse findNhanVienById(Long id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên"));
        return NhanVienResponse.fromEntity(nv);
    }

    @Transactional
    public NhanVienResponse updateNhanVien(Long id, UpdateNhanVienRequest request) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên"));

        String newEmail = request.email().trim().toLowerCase();
        String newPhone = request.soDienThoai().trim();

        if (!nv.getEmail().equalsIgnoreCase(newEmail)) {
            if (nhanVienRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, id)
                    || khachHangRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new BusinessException("Email đã được sử dụng");
            }
            nv.setEmail(newEmail);
        }

        if (!nv.getSoDienThoai().equals(newPhone)) {
            if (nhanVienRepository.existsBySoDienThoaiAndIdNot(newPhone, id)) {
                throw new BusinessException("Số điện thoại đã được sử dụng");
            }
            nv.setSoDienThoai(newPhone);
        }

        nv.setHoTen(request.hoTen().trim());
        nv.setNgaySinh(request.ngaySinh());
        nv.setGioiTinh(request.gioiTinh());
        nv.setDiaChi(request.diaChi().trim());
        nv.setChucVu(request.chucVu());
        if (request.laAdmin() != null) {
            nv.setLaAdmin(request.laAdmin());
        }

        nv = nhanVienRepository.save(nv);
        log.info("Cập nhật thông tin nhân viên thành công, id: {}", id);
        return NhanVienResponse.fromEntity(nv);
    }
}

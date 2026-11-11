package com.codegym.bds_backend.nhanvien;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NhanVienService {
    private final NhanVienRepository nhanVienRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    @Transactional(readOnly = true)
    public PageResponse<NhanVienResponse> findDanhSachNhanVien(
            String maNv, String hoTen, ChucVu chucVu, String email, Pageable pageable) {
        String cleanMaNv = (maNv != null && !maNv.isBlank()) ? maNv.trim() : null;
        String cleanHoTen = (hoTen != null && !hoTen.isBlank()) ? hoTen.trim() : null;
        String cleanEmail = (email != null && !email.isBlank()) ? email.trim() : null;

        Page<NhanVien> page = nhanVienRepository.searchNhanVien(
                cleanMaNv, cleanHoTen, chucVu, cleanEmail, pageable);
        return PageResponse.of(page.map(NhanVienResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public NhanVienResponse findNhanVienById(Long id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên"));
        return NhanVienResponse.fromEntity(nv);
    }

    @Transactional
    public void deleteNhanVien(Long id, JwtPrincipal principal) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên"));

        if (principal != null && principal.userId() != null) {
            taiKhoanRepository.findWithChuSoHuuById(principal.userId()).ifPresent(tk -> {
                if (tk.getNhanVien() != null && tk.getNhanVien().getId().equals(id)) {
                    throw new BusinessException("Admin không thể tự xóa tài khoản của chính mình");
                }
            });
        }

        nhanVienRepository.delete(nv);
        log.info("Xóa mềm nhân viên thành công, id: {}", id);
    }
}


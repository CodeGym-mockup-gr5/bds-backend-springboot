package com.codegym.bds_backend.nhanvien;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.auth.TaiKhoanService;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.common.util.MaHienThi;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhanvien.dto.CreateNhanVienRequest;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
import com.codegym.bds_backend.nhanvien.dto.UpdateNhanVienRequest;
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
    private final TaiKhoanService taiKhoanService;
    private final KhachHangRepository khachHangRepository;

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
    public NhanVienResponse createNhanVien(CreateNhanVienRequest request) {
        String hoTen = request.hoTen().trim();
        String email = request.email().trim().toLowerCase();
        String soDienThoai = request.soDienThoai().trim();
        String diaChi = request.diaChi().trim();

        if (nhanVienRepository.existsByEmailIgnoreCase(email)
                || khachHangRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Email đã được sử dụng");
        }
        if (nhanVienRepository.existsBySoDienThoai(soDienThoai)) {
            throw new BusinessException("Số điện thoại đã được sử dụng");
        }

        String tenDangNhap = (request.tenDangNhap() != null && !request.tenDangNhap().isBlank())
                ? request.tenDangNhap().trim()
                : email.substring(0, email.indexOf('@'));

        if (taiKhoanRepository.existsByTenDangNhap(tenDangNhap)) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        NhanVien nv = new NhanVien();
        nv.setHoTen(hoTen);
        nv.setNgaySinh(request.ngaySinh());
        nv.setGioiTinh(request.gioiTinh());
        nv.setDiaChi(diaChi);
        nv.setSoDienThoai(soDienThoai);
        nv.setEmail(email);
        nv.setChucVu(request.chucVu());
        nv.setLaAdmin(Boolean.TRUE.equals(request.laAdmin()));

        nv = nhanVienRepository.saveAndFlush(nv);
        nv.setMaNv(MaHienThi.format(MaHienThi.NHAN_VIEN, nv.getId()));

        taiKhoanService.createTaiKhoanChoNhanVien(nv, tenDangNhap);
        log.info("Tạo mới nhân viên thành công, id: {}, mã: {}", nv.getId(), nv.getMaNv());
        return NhanVienResponse.fromEntity(nv);
    }

    @Transactional
    public NhanVienResponse updateNhanVien(Long id, UpdateNhanVienRequest request, JwtPrincipal principal) {
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
            validateThayDoiQuyenAdmin(nv, request.laAdmin(), principal);
            nv.setLaAdmin(request.laAdmin());
        }

        nv = nhanVienRepository.save(nv);
        log.info("Cập nhật thông tin nhân viên thành công, id: {}", id);
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
    private void validateThayDoiQuyenAdmin(NhanVien nv, boolean laAdminMoi, JwtPrincipal principal) {
        boolean dangHaQuyen = nv.isLaAdmin() && !laAdminMoi;
        if (!dangHaQuyen) {
            return;
        }

        // Chặn 2: không tự hạ quyền chính mình
        if (principal != null && principal.userId() != null) {
            taiKhoanRepository.findWithChuSoHuuById(principal.userId()).ifPresent(tk -> {
                if (tk.getNhanVien() != null && tk.getNhanVien().getId().equals(nv.getId())) {
                    throw new BusinessException("Admin không thể tự hạ quyền Admin của chính mình");
                }
            });
        }

        // Chặn 1: không bỏ quyền Admin cuối cùng
        if (nhanVienRepository.countByLaAdminTrue() <= 1) {
            throw new BusinessException("Không thể bỏ quyền Admin cuối cùng của hệ thống");
        }
    }
}

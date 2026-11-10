package com.codegym.bds_backend.nhanvien;

import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.auth.TaiKhoanService;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.util.MaHienThi;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhanvien.dto.CreateNhanVienRequest;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}

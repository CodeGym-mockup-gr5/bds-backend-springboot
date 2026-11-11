package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.util.MaHienThi;
import com.codegym.bds_backend.config.AppProperties;
import com.codegym.bds_backend.nhanvien.ChucVu;
import com.codegym.bds_backend.nhanvien.NhanVien;
import com.codegym.bds_backend.nhanvien.NhanVienRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBootstrapService {
    private final NhanVienRepository nhanVienRepository;
    private final TaiKhoanService taiKhoanService;
    private final AppProperties props;

    @Transactional
    public void createAdminIfAbsent() {
        if (nhanVienRepository.existsByLaAdminTrue()) {
            return;
        }
        AppProperties.BootstrapAdmin config = props.bootstrapAdmin();
        NhanVien admin = new NhanVien();
        admin.setHoTen(config.hoTen());
        admin.setNgaySinh(LocalDate.of(1990, 1, 1));
        admin.setGioiTinh(GioiTinh.NAM);
        admin.setDiaChi("Chưa cập nhật");
        admin.setSoDienThoai(config.soDienThoai());
        admin.setEmail(config.email());
        admin.setChucVu(ChucVu.TRUONG_PHONG);
        admin.setLaAdmin(true);
        admin = nhanVienRepository.saveAndFlush(admin);
        admin.setMaNv(MaHienThi.format(MaHienThi.NHAN_VIEN, admin.getId()));
        taiKhoanService.createTaiKhoanChoNhanVien(admin, config.tenDangNhap());
        log.info("Đã tạo tài khoản Admin ban đầu '{}', lời mời gửi tới email cấu hình", config.tenDangNhap());
    }
}

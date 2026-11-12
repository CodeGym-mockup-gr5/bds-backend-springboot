package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.khachhang.dto.YeuCauDangTinResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class YeuCauDangTinService {
    private static final String MSG_DANG_CHO_DUYET = "Yêu cầu đang chờ Admin duyệt";
    private static final String MSG_DA_DUYET = "Tài khoản đã được duyệt đăng tin bán";

    private final TaiKhoanRepository taiKhoanRepository;

    @Transactional
    public YeuCauDangTinResponse createYeuCauDangTin(Long taiKhoanId) {
        KhachHang khachHang = findKhachHangByTaiKhoanId(taiKhoanId);
        if (khachHang.isLaKhBan()) {
            if (khachHang.getTrangThaiDuyet() == TrangThaiDuyet.CHO_DUYET) {
                throw new BusinessException(MSG_DANG_CHO_DUYET);
            }
            throw new BusinessException(MSG_DA_DUYET);
        }
        khachHang.setLaKhBan(true);
        khachHang.setTrangThaiDuyet(TrangThaiDuyet.CHO_DUYET);
        log.info("Khách hàng {} gửi yêu cầu đăng tin bán", khachHang.getId());
        return new YeuCauDangTinResponse(khachHang.isLaKhBan(), khachHang.getTrangThaiDuyet().name());
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
}

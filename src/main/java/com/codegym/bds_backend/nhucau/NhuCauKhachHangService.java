package com.codegym.bds_backend.nhucau;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhucau.dto.NhuCauKhachHangFilter;
import com.codegym.bds_backend.nhucau.dto.NhuCauKhachHangResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NhuCauKhachHangService {
    private static final int SIZE_MAC_DINH = 10;
    private static final int SIZE_TOI_DA = 50;

    private final NhuCauRepository nhuCauRepository;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public PageResponse<NhuCauKhachHangResponse> findNhuCauCuaKhachHang(
            Long taiKhoanId, Long khachHangId, NhuCauKhachHangFilter filter, int page, int size) {
        TaiKhoan taiKhoan = findTaiKhoanHopLe(taiKhoanId);
        kiemTraQuyenXemNhuCau(taiKhoan, khachHangId);
        return searchNhuCau(khachHangId, filter, page, size);
    }

    public PageResponse<NhuCauKhachHangResponse> findNhuCauCuaToi(
            Long taiKhoanId, NhuCauKhachHangFilter filter, int page, int size) {
        TaiKhoan taiKhoan = findTaiKhoanHopLe(taiKhoanId);
        KhachHang khachHang = taiKhoan.getKhachHang();
        if (khachHang == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Chức năng này chỉ dành cho khách hàng");
        }
        return searchNhuCau(khachHang.getId(), filter, page, size);
    }

    private TaiKhoan findTaiKhoanHopLe(Long taiKhoanId) {
        return taiKhoanRepository
                .findWithChuSoHuuById(taiKhoanId)
                .filter(TaiKhoan::chuSoHuuConHieuLuc)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
    }

    private void kiemTraQuyenXemNhuCau(TaiKhoan taiKhoan, Long khachHangId) {
        if (taiKhoan.getNhanVien() != null) {
            return;
        }
        KhachHang chuSoHuu = taiKhoan.getKhachHang();
        if (chuSoHuu == null || !chuSoHuu.getId().equals(khachHangId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Bạn chỉ được xem nhu cầu của chính mình");
        }
    }

    private PageResponse<NhuCauKhachHangResponse> searchNhuCau(
            Long khachHangId, NhuCauKhachHangFilter filter, int page, int size) {
        KhachHang khachHang = khachHangRepository
                .findById(khachHangId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng"));
        Pageable pageable = PageRequest.of(
                Math.max(page, 0), chuanHoaSize(size), Sort.by(Sort.Direction.DESC, "id"));
        Page<NhuCau> ketQua = nhuCauRepository.findAll(
                NhuCauSpecification.timNhuCauCuaKhachHang(khachHangId, filter), pageable);
        log.info("Tìm thấy {} nhu cầu của khách hàng id={}", ketQua.getTotalElements(), khachHangId);
        return PageResponse.of(ketQua.map(nhuCau -> NhuCauKhachHangResponse.fromEntity(nhuCau, khachHang)));
    }

    private static int chuanHoaSize(int size) {
        if (size <= 0) {
            return SIZE_MAC_DINH;
        }
        return Math.min(size, SIZE_TOI_DA);
    }
}

package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.dto.DuyetKhachHangResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangChoDuyetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhachHangDuyetService {
    private static final String MSG_KHONG_CHO_DUYET = "Khách hàng không ở trạng thái chờ duyệt";

    private final KhachHangRepository khachHangRepository;

    @Transactional(readOnly = true)
    public PageResponse<KhachHangChoDuyetResponse> getDanhSachKhachHangChoDuyet(Pageable pageable) {
        Pageable theoIdTangDan = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        return PageResponse.of(khachHangRepository
                .findByLaKhBanTrueAndTrangThaiDuyet(TrangThaiDuyet.CHO_DUYET, theoIdTangDan)
                .map(KhachHangDuyetService::toChoDuyetResponse));
    }

    @Transactional
    public DuyetKhachHangResponse approveKhachHang(Long id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng"));
        if (!khachHang.isLaKhBan() || khachHang.getTrangThaiDuyet() != TrangThaiDuyet.CHO_DUYET) {
            throw new BusinessException(MSG_KHONG_CHO_DUYET);
        }
        khachHang.setTrangThaiDuyet(TrangThaiDuyet.DA_DUYET);
        log.info("Admin duyệt đăng tin bán cho khách hàng {}", khachHang.getId());
        return new DuyetKhachHangResponse(khachHang.getId(), khachHang.getMaKh(), khachHang.isLaKhBan(),
                khachHang.getTrangThaiDuyet().name());
    }

    private static KhachHangChoDuyetResponse toChoDuyetResponse(KhachHang khachHang) {
        return new KhachHangChoDuyetResponse(khachHang.getId(), khachHang.getMaKh(), khachHang.getHoTen(),
                khachHang.getSoDienThoai(), khachHang.getEmail());
    }
}

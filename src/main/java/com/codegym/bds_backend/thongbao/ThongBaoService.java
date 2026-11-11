package com.codegym.bds_backend.thongbao;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.nhanvien.NhanVien;
import com.codegym.bds_backend.thongbao.dto.CreateThongBaoRequest;
import com.codegym.bds_backend.thongbao.dto.ThongBaoResponse;
import com.codegym.bds_backend.thongbao.dto.UpdateThongBaoRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThongBaoService {
    private final ThongBaoRepository thongBaoRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public PageResponse<ThongBaoResponse> findThongBao(String tuKhoa, Pageable pageable) {
        String normalized = (tuKhoa != null && !tuKhoa.isBlank()) ? tuKhoa.trim() : null;
        Page<ThongBao> page = thongBaoRepository.findThongBaoByTuKhoa(normalized, pageable);
        return PageResponse.of(page.map(ThongBaoResponse::fromEntity));
    }

    public List<ThongBaoResponse> findTopThongBaoMoiNhat(int limit) {
        int size = (limit <= 0 || limit > 20) ? 5 : limit;
        Pageable pageable = PageRequest.of(0, size);
        return thongBaoRepository.findTopMoiNhat(pageable).stream()
                .map(ThongBaoResponse::fromEntity)
                .toList();
    }

    public ThongBaoResponse findThongBaoById(Long id) {
        ThongBao thongBao = thongBaoRepository.findWithNguoiTaoById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông báo với mã " + id));
        return ThongBaoResponse.fromEntity(thongBao);
    }

    @Transactional
    public ThongBaoResponse createThongBao(CreateThongBaoRequest request, Long userId) {
        TaiKhoan taiKhoan = taiKhoanRepository.findWithChuSoHuuById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản người tạo"));
        NhanVien nhanVien = taiKhoan.getNhanVien();
        if (nhanVien == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Tài khoản hiện tại không có hồ sơ nhân viên");
        }

        ThongBao thongBao = new ThongBao();
        thongBao.setTenThongBao(request.tenThongBao().trim());
        thongBao.setNoiDung(request.noiDung().trim());
        thongBao.setNgay(request.ngay() != null ? request.ngay() : LocalDate.now());
        thongBao.setNguoiTao(nhanVien);

        ThongBao saved = thongBaoRepository.save(thongBao);
        log.info("Tạo mới thông báo id={} bởi nhân viên id={}", saved.getId(), nhanVien.getId());
        return ThongBaoResponse.fromEntity(saved);
    }

    @Transactional
    public ThongBaoResponse updateThongBao(Long id, UpdateThongBaoRequest request) {
        ThongBao thongBao = thongBaoRepository.findWithNguoiTaoById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông báo với mã " + id));

        thongBao.setTenThongBao(request.tenThongBao().trim());
        thongBao.setNoiDung(request.noiDung().trim());
        if (request.ngay() != null) {
            thongBao.setNgay(request.ngay());
        }

        ThongBao updated = thongBaoRepository.save(thongBao);
        log.info("Cập nhật thông báo id={}", updated.getId());
        return ThongBaoResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteThongBao(Long id) {
        ThongBao thongBao = thongBaoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông báo với mã " + id));
        thongBaoRepository.delete(thongBao);
        log.info("Xóa thông báo id={}", id);
    }
}

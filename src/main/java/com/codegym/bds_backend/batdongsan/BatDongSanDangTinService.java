package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanResponse;
import com.codegym.bds_backend.batdongsan.dto.HinhAnhBdsDto;
import com.codegym.bds_backend.batdongsan.dto.ThemBatDongSanRequest;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.common.util.MaHienThi;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BatDongSanDangTinService {
    private static final String ROLE_KHACH_HANG = "ROLE_KHACH_HANG";
    private static final String MSG_CHUA_DANG_KY_BAN =
            "Tài khoản chưa đăng ký đăng tin bán. Vui lòng gửi yêu cầu đăng tin để Admin duyệt";
    private static final String MSG_CHO_DUYET = "Tài khoản đang chờ Admin duyệt để đăng tin bán";

    private final BatDongSanRepository batDongSanRepository;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BatDongSanResponse createBatDongSan(JwtPrincipal principal, ThemBatDongSanRequest request) {
        KhachHang khachHang;
        if (isKhachHang(principal)) {
            khachHang = findKhachHangCuaTaiKhoan(principal);
            checkQuyenDangTinBan(khachHang);
        } else {
            khachHang = findKhachHangDuocDangThay(request);
            khachHang.setLaKhBan(true);
            khachHang.setTrangThaiDuyet(TrangThaiDuyet.DA_DUYET);
        }

        BatDongSan bds = new BatDongSan();
        bds.setKhachHang(khachHang);
        bds.setLoaiBds(request.loaiBds());
        bds.setDiaChi(request.diaChi());
        bds.setViTri(request.viTri());
        bds.setHuong(request.huong());
        bds.setDienTich(request.dienTich());
        bds.setGia(request.gia());
        bds.setPhapLy(request.phapLy());
        bds.setTinhTrang(TinhTrangBds.CO_SAN);
        bds.setNgayDang(LocalDate.now());
        bds.setGhiChu(request.ghiChu());

        if (request.danhSachHinhAnh() != null && !request.danhSachHinhAnh().isEmpty()) {
            int viTriAnh = 1;
            for (HinhAnhBdsDto dto : request.danhSachHinhAnh()) {
                if (dto == null) {
                    continue;
                }
                HinhAnhBds hinhAnh = new HinhAnhBds();
                hinhAnh.setBatDongSan(bds);
                hinhAnh.setDuongDan(dto.duongDan());
                hinhAnh.setThuTu(dto.thuTu() > 0 ? dto.thuTu() : viTriAnh);
                bds.getHinhAnh().add(hinhAnh);
                viTriAnh++;
            }
        }

        BatDongSan saved = batDongSanRepository.saveAndFlush(bds);
        saved.setMaBds(MaHienThi.format(MaHienThi.BAT_DONG_SAN, saved.getId()));

        eventPublisher.publishEvent(new BatDongSanDangTinEvent(saved.getMaBds(), saved.getDiaChi(), saved.getGia()));

        return toResponse(saved, khachHang);
    }

    @Transactional(readOnly = true)
    public PageResponse<BatDongSanResponse> getDanhSachBatDongSanMoiDang(Pageable pageable) {
        Page<BatDongSan> page = batDongSanRepository.findAllByOrderByNgayDangDescIdDesc(pageable);
        return PageResponse.of(page.map(bds -> toResponse(bds, bds.getKhachHang())));
    }

    private static boolean isKhachHang(JwtPrincipal principal) {
        return principal.roles() != null && principal.roles().contains(ROLE_KHACH_HANG);
    }

    private KhachHang findKhachHangCuaTaiKhoan(JwtPrincipal principal) {
        TaiKhoan taiKhoan = taiKhoanRepository.findWithChuSoHuuById(principal.userId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        KhachHang kh = taiKhoan.getKhachHang();
        if (kh == null || kh.getDeletedAt() != null) {
            throw new NotFoundException("Không tìm thấy thông tin khách hàng cho tài khoản này");
        }
        return kh;
    }

    private static void checkQuyenDangTinBan(KhachHang khachHang) {
        if (!khachHang.isLaKhBan()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, MSG_CHUA_DANG_KY_BAN);
        }
        if (khachHang.getTrangThaiDuyet() != TrangThaiDuyet.DA_DUYET) {
            throw new BusinessException(HttpStatus.FORBIDDEN, MSG_CHO_DUYET);
        }
    }

    private KhachHang findKhachHangDuocDangThay(ThemBatDongSanRequest request) {
        if (request.khachHangId() == null) {
            throw new BadRequestException("Vui lòng chọn khách hàng sở hữu bất động sản");
        }
        return khachHangRepository.findById(request.khachHangId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng"));
    }

    private BatDongSanResponse toResponse(BatDongSan bds, KhachHang khachHang) {
        BigDecimal giaM2 = bds.getGia().divide(bds.getDienTich(), 0, RoundingMode.HALF_UP);
        List<HinhAnhBdsDto> hinhAnhDtos = new ArrayList<>();
        if (bds.getHinhAnh() != null) {
            for (HinhAnhBds h : bds.getHinhAnh()) {
                hinhAnhDtos.add(new HinhAnhBdsDto(h.getDuongDan(), h.getThuTu()));
            }
        }

        return new BatDongSanResponse(
                bds.getId(),
                bds.getMaBds(),
                khachHang != null ? khachHang.getId() : null,
                khachHang != null ? khachHang.getHoTen() : null,
                bds.getLoaiBds(),
                bds.getDiaChi(),
                bds.getViTri(),
                bds.getHuong(),
                bds.getDienTich(),
                bds.getGia(),
                giaM2,
                bds.getPhapLy(),
                bds.getTinhTrang(),
                bds.getNgayDang(),
                bds.getGhiChu(),
                hinhAnhDtos
        );
    }
}

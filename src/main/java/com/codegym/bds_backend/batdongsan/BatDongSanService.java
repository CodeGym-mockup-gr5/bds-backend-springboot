package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.auth.CurrentUser;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanDetailResponse;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanListItemResponse;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanSearchRequest;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatDongSanService {

    static final String MSG_KHOANG_DIEN_TICH = "Khoảng diện tích không hợp lệ";
    static final String MSG_KHOANG_GIA = "Khoảng giá không hợp lệ";
    static final String MSG_SAP_XEP = "Tiêu chí sắp xếp không hợp lệ";
    static final String MSG_KHONG_TIM_THAY = "Không tìm thấy bất động sản";

    private static final Set<String> SORTABLE_FIELDS = Set.of("ngayDang", "gia", "dienTich", "giaM2", "id");
    private static final String DEFAULT_SORT_FIELD = "ngayDang";
    private static final String ID_FIELD = "id";
    private static final int KEEP_TRAILING_EMPTY_PARTS = -1;

    private final BatDongSanRepository batDongSanRepository;
    private final HinhAnhBdsRepository hinhAnhBdsRepository;
    private final BatDongSanMapper batDongSanMapper;
    private final BatDongSanProperties batDongSanProperties;
    private final CurrentUser currentUser;

    public PageResponse<BatDongSanListItemResponse> searchBatDongSan(BatDongSanSearchRequest request) {
        validateKhoang(request.dienTichTu(), request.dienTichDen(), MSG_KHOANG_DIEN_TICH);
        validateKhoang(request.giaTu(), request.giaDen(), MSG_KHOANG_GIA);

        PageRequest pageRequest = PageRequest.of(resolvePage(request), resolveSize(request), buildSort(request.sort()));
        boolean laNoiBo = currentUser.isAdminOrNhanVien();
        Long khachHangDangNhapId = currentUser.findKhachHangId().orElse(null);
        TinhTrangBds tinhTrang = laNoiBo ? request.tinhTrang() : TinhTrangBds.CO_SAN;
        Long khachHangId = resolveKhachHangFilter(request.khachHangId(), laNoiBo, khachHangDangNhapId);

        Page<BatDongSan> page = batDongSanRepository.findAll(
                BatDongSanSpecifications.filterBatDongSan(request, tinhTrang, khachHangId), pageRequest);
        Map<Long, String> anhDaiDien = findAnhDaiDien(page.getContent());

        return PageResponse.of(page.map(bds -> batDongSanMapper.toListItemResponse(
                bds, anhDaiDien.get(bds.getId()), canViewChuSoHuu(bds, laNoiBo, khachHangDangNhapId))));
    }

    public BatDongSanDetailResponse getChiTietBatDongSan(Long id) {
        boolean laNoiBo = currentUser.isAdminOrNhanVien();
        Long khachHangDangNhapId = currentUser.findKhachHangId().orElse(null);
        BatDongSan bds = batDongSanRepository.findWithChiTietById(id)
                .filter(found -> laNoiBo
                        || found.getTinhTrang() == TinhTrangBds.CO_SAN
                        || isChuSoHuu(found, khachHangDangNhapId))
                .orElseThrow(() -> new NotFoundException(MSG_KHONG_TIM_THAY));
        return batDongSanMapper.toDetailResponse(bds, canViewChuSoHuu(bds, laNoiBo, khachHangDangNhapId));
    }

    private Long resolveKhachHangFilter(Long khachHangId, boolean laNoiBo, Long khachHangDangNhapId) {
        if (laNoiBo || (khachHangId != null && khachHangId.equals(khachHangDangNhapId))) {
            return khachHangId;
        }
        return null;
    }

    private boolean canViewChuSoHuu(BatDongSan bds, boolean laNoiBo, Long khachHangDangNhapId) {
        return laNoiBo || isChuSoHuu(bds, khachHangDangNhapId);
    }

    private boolean isChuSoHuu(BatDongSan bds, Long khachHangDangNhapId) {
        return khachHangDangNhapId != null
                && bds.getKhachHang() != null
                && khachHangDangNhapId.equals(bds.getKhachHang().getId());
    }

    private Map<Long, String> findAnhDaiDien(List<BatDongSan> danhSach) {
        if (danhSach.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = danhSach.stream().map(BatDongSan::getId).toList();
        Map<Long, String> result = new HashMap<>();
        for (HinhAnhBds hinhAnh : hinhAnhBdsRepository.findByBatDongSanIds(ids)) {
            result.putIfAbsent(hinhAnh.getBatDongSan().getId(), hinhAnh.getDuongDan());
        }
        return result;
    }

    private void validateKhoang(BigDecimal from, BigDecimal to, String message) {
        if (from != null && to != null && from.compareTo(to) > 0) {
            throw new BadRequestException(message);
        }
    }

    private int resolvePage(BatDongSanSearchRequest request) {
        return request.page() == null ? 0 : request.page();
    }

    private int resolveSize(BatDongSanSearchRequest request) {
        if (request.size() == null) {
            return batDongSanProperties.defaultPageSize();
        }
        if (request.size() > batDongSanProperties.pageMaxSize()) {
            throw new BadRequestException("Kích thước trang tối đa là " + batDongSanProperties.pageMaxSize());
        }
        return request.size();
    }

    private Sort buildSort(String sortParam) {
        Sort tieBreaker = Sort.by(Sort.Direction.DESC, ID_FIELD);
        if (!StringUtils.hasText(sortParam)) {
            return Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD).and(tieBreaker);
        }
        String[] parts = sortParam.split(",", KEEP_TRAILING_EMPTY_PARTS);
        String field = parts[0].trim();
        if (parts.length > 2 || !SORTABLE_FIELDS.contains(field)) {
            throw new BadRequestException(MSG_SAP_XEP);
        }
        Sort.Direction direction = parts.length == 2 ? parseDirection(parts[1]) : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, field);
        return ID_FIELD.equals(field) ? sort : sort.and(tieBreaker);
    }

    private Sort.Direction parseDirection(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new BadRequestException(MSG_SAP_XEP);
        };
    }
}

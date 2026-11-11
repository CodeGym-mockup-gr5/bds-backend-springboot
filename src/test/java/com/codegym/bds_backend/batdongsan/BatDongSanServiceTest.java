package com.codegym.bds_backend.batdongsan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.CurrentUser;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanDetailResponse;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanListItemResponse;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanSearchRequest;
import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.KhachHang;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BatDongSanServiceTest {

    @Mock
    private BatDongSanRepository batDongSanRepository;

    @Mock
    private HinhAnhBdsRepository hinhAnhBdsRepository;

    @Mock
    private CurrentUser currentUser;

    private BatDongSanService batDongSanService;

    @BeforeEach
    void setUp() {
        batDongSanService = new BatDongSanService(
                batDongSanRepository,
                hinhAnhBdsRepository,
                new BatDongSanMapper(),
                new BatDongSanProperties(10, 100),
                currentUser);
    }

    @Test
    void shouldUseLowestOrderImageAsThumbnailWhenSearching() {
        when(currentUser.isAdminOrNhanVien()).thenReturn(true);
        Page<BatDongSan> page = new PageImpl<>(
                List.of(batDongSan(301L, TinhTrangBds.DA_BAN),
                        batDongSan(304L, TinhTrangBds.CO_SAN)),
                PageRequest.of(0, 10), 2);
        when(batDongSanRepository.findAll(ArgumentMatchers.<Specification<BatDongSan>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(hinhAnhBdsRepository.findByBatDongSanIds(anyCollection())).thenReturn(List.of(
                hinhAnh(1L, 301L, 0, "/uploads/bds/301-1.jpg"),
                hinhAnh(2L, 301L, 1, "/uploads/bds/301-2.jpg")));

        PageResponse<BatDongSanListItemResponse> result = batDongSanService.searchBatDongSan(emptyRequest());

        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.content().get(0).anhDaiDien()).isEqualTo("/uploads/bds/301-1.jpg");
        assertThat(result.content().get(1).anhDaiDien()).isNull();
        assertThat(result.content().get(0).tenChuSoHuu()).isEqualTo("Chủ sở hữu 301");
    }

    @Test
    void shouldHideOwnerWhenUserIsNotStaff() {
        when(currentUser.isAdminOrNhanVien()).thenReturn(false);
        Page<BatDongSan> page = new PageImpl<>(
                List.of(batDongSan(304L, TinhTrangBds.CO_SAN)), PageRequest.of(0, 10), 1);
        when(batDongSanRepository.findAll(ArgumentMatchers.<Specification<BatDongSan>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(hinhAnhBdsRepository.findByBatDongSanIds(anyCollection())).thenReturn(List.of());

        BatDongSanListItemResponse item = batDongSanService.searchBatDongSan(emptyRequest()).content().get(0);

        assertThat(item.khachHangId()).isNull();
        assertThat(item.maKh()).isNull();
        assertThat(item.tenChuSoHuu()).isNull();
    }

    @Test
    void shouldSkipImageQueryWhenNoPropertyMatches() {
        when(currentUser.isAdminOrNhanVien()).thenReturn(false);
        when(batDongSanRepository.findAll(ArgumentMatchers.<Specification<BatDongSan>>any(), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        PageResponse<BatDongSanListItemResponse> result = batDongSanService.searchBatDongSan(emptyRequest());

        assertThat(result.content()).isEmpty();
        verify(hinhAnhBdsRepository, never()).findByBatDongSanIds(anyCollection());
    }

    @Test
    void shouldRejectWhenAreaRangeIsReversed() {
        BatDongSanSearchRequest request = new BatDongSanSearchRequest(null, null, null, null,
                new BigDecimal("200"), new BigDecimal("100"), null, null, null, null, null, null, null);

        assertThatThrownBy(() -> batDongSanService.searchBatDongSan(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Khoảng diện tích không hợp lệ");
    }

    @Test
    void shouldRejectWhenPriceRangeIsReversed() {
        BatDongSanSearchRequest request = new BatDongSanSearchRequest(null, null, null, null, null, null,
                new BigDecimal("5000000000"), new BigDecimal("1000000000"), null, null, null, null, null);

        assertThatThrownBy(() -> batDongSanService.searchBatDongSan(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Khoảng giá không hợp lệ");
    }

    @Test
    void shouldRejectWhenSortFieldIsNotAllowed() {
        assertThatThrownBy(() -> batDongSanService.searchBatDongSan(requestWithPaging(null, null, "hoTen,asc")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tiêu chí sắp xếp không hợp lệ");
    }

    @Test
    void shouldRejectWhenSortDirectionIsInvalid() {
        assertThatThrownBy(() -> batDongSanService.searchBatDongSan(requestWithPaging(null, null, "gia,up")))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> batDongSanService.searchBatDongSan(requestWithPaging(null, null, "gia,")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldRejectWhenPageSizeExceedsLimit() {
        assertThatThrownBy(() -> batDongSanService.searchBatDongSan(requestWithPaging(0, 101, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Kích thước trang tối đa là 100");
    }

    @Test
    void shouldEscapeLikeWildcardsWhenKeywordContainsThem() {
        assertThat(BatDongSanSpecifications.escapeLike("50%_a!")).isEqualTo("50!%!_a!!");
    }

    private static BatDongSanSearchRequest emptyRequest() {
        return requestWithPaging(null, null, null);
    }

    private static BatDongSanSearchRequest requestWithPaging(Integer page, Integer size, String sort) {
        return new BatDongSanSearchRequest(null, null, null, null, null, null, null, null, null, null,
                page, size, sort);
    }

    @Test
    void shouldReturnOwnerAndOrderedImagesWhenStaffViewsDetail() {
        when(currentUser.isAdminOrNhanVien()).thenReturn(true);
        BatDongSan bds = batDongSan(301L, TinhTrangBds.DA_BAN);
        bds.getKhachHang().setSoDienThoai("0987000201");
        bds.getHinhAnh().add(hinhAnh(1L, 301L, 0, "/uploads/bds/301-1.jpg"));
        bds.getHinhAnh().add(hinhAnh(2L, 301L, 1, "/uploads/bds/301-2.jpg"));
        when(batDongSanRepository.findWithChiTietById(301L)).thenReturn(Optional.of(bds));

        BatDongSanDetailResponse result = batDongSanService.getChiTietBatDongSan(301L);

        assertThat(result.maBds()).isEqualTo("BDS301");
        assertThat(result.hinhAnh()).extracting("thuTu").containsExactly(0, 1);
        assertThat(result.chuSoHuu().hoTen()).isEqualTo("Chủ sở hữu 301");
        assertThat(result.chuSoHuu().soDienThoai()).isEqualTo("0987000201");
    }

    @Test
    void shouldHideOwnerWhenGuestViewsAvailableDetail() {
        when(currentUser.isAdminOrNhanVien()).thenReturn(false);
        when(batDongSanRepository.findWithChiTietById(304L))
                .thenReturn(Optional.of(batDongSan(304L, TinhTrangBds.CO_SAN)));

        BatDongSanDetailResponse result = batDongSanService.getChiTietBatDongSan(304L);

        assertThat(result.chuSoHuu()).isNull();
        assertThat(result.hinhAnh()).isEmpty();
    }

    @Test
    void shouldThrowNotFoundWhenGuestViewsSoldProperty() {
        when(currentUser.isAdminOrNhanVien()).thenReturn(false);
        when(batDongSanRepository.findWithChiTietById(301L))
                .thenReturn(Optional.of(batDongSan(301L, TinhTrangBds.DA_BAN)));

        assertThatThrownBy(() -> batDongSanService.getChiTietBatDongSan(301L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Không tìm thấy bất động sản");
    }

    @Test
    void shouldThrowNotFoundWhenPropertyDoesNotExist() {
        when(currentUser.isAdminOrNhanVien()).thenReturn(true);
        when(batDongSanRepository.findWithChiTietById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> batDongSanService.getChiTietBatDongSan(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private static BatDongSan batDongSan(Long id, TinhTrangBds tinhTrang) {
        KhachHang chuSoHuu = new KhachHang();
        chuSoHuu.setId(id + 1000);
        chuSoHuu.setMaKh("KH" + (id + 1000));
        chuSoHuu.setHoTen("Chủ sở hữu " + id);

        BatDongSan bds = new BatDongSan();
        bds.setId(id);
        bds.setMaBds("BDS" + id);
        bds.setKhachHang(chuSoHuu);
        bds.setLoaiBds(LoaiBds.DAT);
        bds.setDiaChi("Địa chỉ " + id);
        bds.setViTri(ViTri.TRUNG_TAM);
        bds.setHuong(Huong.DONG);
        bds.setDienTich(new BigDecimal("80"));
        bds.setGia(new BigDecimal("2400000000"));
        bds.setGiaM2(new BigDecimal("30000000"));
        bds.setTinhTrang(tinhTrang);
        bds.setNgayDang(LocalDate.of(2026, 5, 20));
        return bds;
    }

    private static HinhAnhBds hinhAnh(Long id, Long batDongSanId, int thuTu, String duongDan) {
        BatDongSan bds = new BatDongSan();
        bds.setId(batDongSanId);
        HinhAnhBds hinhAnh = new HinhAnhBds();
        hinhAnh.setId(id);
        hinhAnh.setBatDongSan(bds);
        hinhAnh.setThuTu(thuTu);
        hinhAnh.setDuongDan(duongDan);
        return hinhAnh;
    }
}

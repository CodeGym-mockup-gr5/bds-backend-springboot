package com.codegym.bds_backend.batdongsan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanResponse;
import com.codegym.bds_backend.batdongsan.dto.HinhAnhBdsDto;
import com.codegym.bds_backend.batdongsan.dto.ThemBatDongSanRequest;
import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import com.codegym.bds_backend.common.exception.BadRequestException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BatDongSanDangTinServiceTest {

    @Mock
    private BatDongSanRepository batDongSanRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BatDongSanDangTinService batDongSanDangTinService;

    private KhachHang khachHang;
    private ThemBatDongSanRequest request;

    @BeforeEach
    void setUp() {
        khachHang = new KhachHang();
        khachHang.setId(10L);
        khachHang.setMaKh("KH010");
        khachHang.setHoTen("Nguyễn Văn An");
        khachHang.setLaKhMua(false);
        khachHang.setLaKhBan(false);

        List<HinhAnhBdsDto> hinhAnh = List.of(
                new HinhAnhBdsDto("https://res.cloudinary.com/demo/image/upload/sample.jpg", 1));

        request = new ThemBatDongSanRequest(
                10L,
                LoaiBds.NHA_O,
                "Số 25 ngõ 120 phố Thái Thịnh, Đống Đa, Hà Nội",
                ViTri.TRUNG_TAM,
                Huong.DONG_NAM,
                new BigDecimal("75.00"),
                new BigDecimal("17800000000"),
                "Sổ đỏ chính chủ",
                "Nhà mới xây đẹp",
                hinhAnh);
    }

    @Test
    void shouldCreateBatDongSanAsKhachHangSuccessfully() {
        JwtPrincipal principal = new JwtPrincipal(1L, "khachhang1", List.of("ROLE_KHACH_HANG"));
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(1L);
        taiKhoan.setKhachHang(khachHang);

        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));
        when(batDongSanRepository.saveAndFlush(any(BatDongSan.class))).thenAnswer(invocation -> {
            BatDongSan bds = invocation.getArgument(0);
            bds.setId(100L);
            return bds;
        });

        BatDongSanResponse response = batDongSanDangTinService.createBatDongSan(principal, request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("BDS100", response.maBds());
        assertEquals(TinhTrangBds.CO_SAN, response.tinhTrang());
        assertEquals(new BigDecimal("237333333"), response.giaM2());
        assertTrue(khachHang.isLaKhBan());
        assertEquals(1, response.danhSachHinhAnh().size());
        verify(eventPublisher).publishEvent(any(BatDongSanDangTinEvent.class));
    }

    @Test
    void shouldPreserveImageOrderWhenThuTuIsZeroOrNegative() {
        JwtPrincipal principal = new JwtPrincipal(1L, "khachhang1", List.of("ROLE_KHACH_HANG"));
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(1L);
        taiKhoan.setKhachHang(khachHang);

        List<HinhAnhBdsDto> hinhAnhKhongThuTu = new ArrayList<>();
        hinhAnhKhongThuTu.add(new HinhAnhBdsDto("https://res.cloudinary.com/demo/image/upload/img1.jpg", 0));
        hinhAnhKhongThuTu.add(null);
        hinhAnhKhongThuTu.add(new HinhAnhBdsDto("https://res.cloudinary.com/demo/image/upload/img2.jpg", 0));

        ThemBatDongSanRequest req = new ThemBatDongSanRequest(
                null,
                LoaiBds.NHA_O,
                "Địa chỉ",
                ViTri.TRUNG_TAM,
                Huong.DONG,
                new BigDecimal("50.00"),
                new BigDecimal("1000000000"),
                null,
                null,
                hinhAnhKhongThuTu);

        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));
        when(batDongSanRepository.saveAndFlush(any(BatDongSan.class))).thenAnswer(invocation -> {
            BatDongSan bds = invocation.getArgument(0);
            bds.setId(105L);
            return bds;
        });

        BatDongSanResponse response = batDongSanDangTinService.createBatDongSan(principal, req);

        assertEquals(2, response.danhSachHinhAnh().size());
        assertEquals(1, response.danhSachHinhAnh().get(0).thuTu());
        assertEquals(2, response.danhSachHinhAnh().get(1).thuTu());
    }

    @Test
    void shouldCreateBatDongSanAsNhanVienSuccessfully() {
        JwtPrincipal principal = new JwtPrincipal(2L, "nhanvien1", List.of("ROLE_NHAN_VIEN"));

        when(khachHangRepository.findById(10L)).thenReturn(Optional.of(khachHang));
        when(batDongSanRepository.saveAndFlush(any(BatDongSan.class))).thenAnswer(invocation -> {
            BatDongSan bds = invocation.getArgument(0);
            bds.setId(101L);
            return bds;
        });

        BatDongSanResponse response = batDongSanDangTinService.createBatDongSan(principal, request);

        assertNotNull(response);
        assertEquals(101L, response.id());
        assertEquals("BDS101", response.maBds());
        assertTrue(khachHang.isLaKhBan());
        verify(eventPublisher).publishEvent(any(BatDongSanDangTinEvent.class));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenStaffDoesNotProvideKhachHangId() {
        JwtPrincipal principal = new JwtPrincipal(2L, "nhanvien1", List.of("ROLE_NHAN_VIEN"));
        ThemBatDongSanRequest requestKhongKhachHang = new ThemBatDongSanRequest(
                null,
                LoaiBds.NHA_O,
                "Địa chỉ",
                ViTri.TRUNG_TAM,
                Huong.DONG,
                new BigDecimal("50.00"),
                new BigDecimal("1000000000"),
                null,
                null,
                List.of(new HinhAnhBdsDto("https://res.cloudinary.com/demo/image/upload/sample.jpg", 1)));

        assertThrows(BadRequestException.class,
                () -> batDongSanDangTinService.createBatDongSan(principal, requestKhongKhachHang));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenKhachHangNotFoundForStaff() {
        JwtPrincipal principal = new JwtPrincipal(2L, "nhanvien1", List.of("ROLE_NHAN_VIEN"));
        when(khachHangRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> batDongSanDangTinService.createBatDongSan(principal, request));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenKhachHangNotFoundForCustomerAccount() {
        JwtPrincipal principal = new JwtPrincipal(1L, "khachhang1", List.of("ROLE_KHACH_HANG"));
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(1L);
        taiKhoan.setKhachHang(null);

        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoan));

        assertThrows(NotFoundException.class,
                () -> batDongSanDangTinService.createBatDongSan(principal, request));
    }

    @Test
    void shouldGetDanhSachBatDongSanMoiDangSuccessfully() {
        BatDongSan bds = new BatDongSan();
        bds.setId(301L);
        bds.setMaBds("BDS301");
        bds.setKhachHang(khachHang);
        bds.setLoaiBds(LoaiBds.NHA_O);
        bds.setDiaChi("Địa chỉ");
        bds.setViTri(ViTri.TRUNG_TAM);
        bds.setHuong(Huong.DONG);
        bds.setDienTich(new BigDecimal("100.00"));
        bds.setGia(new BigDecimal("2000000000"));
        bds.setTinhTrang(TinhTrangBds.CO_SAN);

        Pageable pageable = PageRequest.of(0, 10);
        Page<BatDongSan> page = new PageImpl<>(List.of(bds), pageable, 1);

        when(batDongSanRepository.findAllByOrderByNgayDangDescIdDesc(pageable)).thenReturn(page);

        PageResponse<BatDongSanResponse> response = batDongSanDangTinService.getDanhSachBatDongSanMoiDang(pageable);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals("BDS301", response.content().get(0).maBds());
    }
}

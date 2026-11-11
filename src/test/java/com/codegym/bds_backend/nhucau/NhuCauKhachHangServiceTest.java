package com.codegym.bds_backend.nhucau;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.KhachHang;
import com.codegym.bds_backend.khachhang.KhachHangRepository;
import com.codegym.bds_backend.nhanvien.NhanVien;
import com.codegym.bds_backend.nhucau.dto.NhuCauKhachHangFilter;
import com.codegym.bds_backend.nhucau.dto.NhuCauKhachHangResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class NhuCauKhachHangServiceTest {
    private static final NhuCauKhachHangFilter KHONG_LOC = new NhuCauKhachHangFilter(null, null, null);

    @Mock
    private NhuCauRepository nhuCauRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @InjectMocks
    private NhuCauKhachHangService nhuCauKhachHangService;

    private KhachHang khachHang;
    private NhuCau nhuCau;

    @BeforeEach
    void setUp() {
        khachHang = new KhachHang();
        khachHang.setId(206L);
        khachHang.setMaKh("KH206");

        nhuCau = new NhuCau();
        nhuCau.setId(401L);
        nhuCau.setMaNc("NC401");
        nhuCau.setKhachHang(khachHang);
        nhuCau.setLoaiBds(LoaiBds.DAT);
        nhuCau.setViTri(ViTri.TRUNG_TAM);
        nhuCau.setDienTichMin(new BigDecimal("70"));
        nhuCau.setDienTichMax(new BigDecimal("100"));
        nhuCau.setGhiChu("Gần trường học");
    }

    private TaiKhoan taiKhoanNhanVien() {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(1L);
        taiKhoan.setNhanVien(new NhanVien());
        return taiKhoan;
    }

    private TaiKhoan taiKhoanKhachHang(KhachHang chuSoHuu) {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(2L);
        taiKhoan.setKhachHang(chuSoHuu);
        return taiKhoan;
    }

    private void giaLapKetQuaTimKiem() {
        Page<NhuCau> ketQua = new PageImpl<>(List.of(nhuCau));
        when(nhuCauRepository.findAll(ArgumentMatchers.<Specification<NhuCau>>any(), any(Pageable.class)))
                .thenReturn(ketQua);
    }

    @Test
    void shouldFindNhuCauCuaKhachHangWhenNhanVienXem() {
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoanNhanVien()));
        when(khachHangRepository.findById(206L)).thenReturn(Optional.of(khachHang));
        giaLapKetQuaTimKiem();

        PageResponse<NhuCauKhachHangResponse> ketQua =
                nhuCauKhachHangService.findNhuCauCuaKhachHang(1L, 206L, KHONG_LOC, 0, 10);

        assertEquals(1, ketQua.totalElements());
        NhuCauKhachHangResponse dong = ketQua.content().get(0);
        assertEquals("NC401", dong.maNc());
        assertEquals("KH206", dong.maKh());
        assertEquals(206L, dong.khachHangId());
        assertEquals(LoaiBds.DAT, dong.loaiBds());
        assertEquals(ViTri.TRUNG_TAM, dong.viTri());
        assertEquals(new BigDecimal("70"), dong.dienTichMin());
        assertEquals(new BigDecimal("100"), dong.dienTichMax());
    }

    @Test
    void shouldFindNhuCauCuaKhachHangWhenKhachXemCuaMinh() {
        when(taiKhoanRepository.findWithChuSoHuuById(2L)).thenReturn(Optional.of(taiKhoanKhachHang(khachHang)));
        when(khachHangRepository.findById(206L)).thenReturn(Optional.of(khachHang));
        giaLapKetQuaTimKiem();

        PageResponse<NhuCauKhachHangResponse> ketQua =
                nhuCauKhachHangService.findNhuCauCuaKhachHang(2L, 206L, KHONG_LOC, 0, 10);

        assertEquals(1, ketQua.content().size());
    }

    @Test
    void shouldThrowForbiddenWhenKhachXemNhuCauCuaNguoiKhac() {
        KhachHang nguoiKhac = new KhachHang();
        nguoiKhac.setId(207L);
        when(taiKhoanRepository.findWithChuSoHuuById(2L)).thenReturn(Optional.of(taiKhoanKhachHang(nguoiKhac)));

        BusinessException loi = assertThrows(BusinessException.class,
                () -> nhuCauKhachHangService.findNhuCauCuaKhachHang(2L, 206L, KHONG_LOC, 0, 10));

        assertEquals(HttpStatus.FORBIDDEN, loi.getStatus());
        verify(nhuCauRepository, never()).findAll(
                ArgumentMatchers.<Specification<NhuCau>>any(), any(Pageable.class));
    }

    @Test
    void shouldThrowNotFoundWhenKhachHangKhongTonTai() {
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoanNhanVien()));
        when(khachHangRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> nhuCauKhachHangService.findNhuCauCuaKhachHang(1L, 999L, KHONG_LOC, 0, 10));
    }

    @Test
    void shouldThrowNotFoundWhenTaiKhoanKhongTonTai() {
        when(taiKhoanRepository.findWithChuSoHuuById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> nhuCauKhachHangService.findNhuCauCuaKhachHang(99L, 206L, KHONG_LOC, 0, 10));
    }

    @Test
    void shouldLimitSizeAndSortMoiNhatTruoc() {
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoanNhanVien()));
        when(khachHangRepository.findById(206L)).thenReturn(Optional.of(khachHang));
        giaLapKetQuaTimKiem();

        nhuCauKhachHangService.findNhuCauCuaKhachHang(1L, 206L, KHONG_LOC, -3, 100000);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(nhuCauRepository).findAll(ArgumentMatchers.<Specification<NhuCau>>any(), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(50, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    void shouldUseSizeMacDinhWhenSizeKhongHopLe() {
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoanNhanVien()));
        when(khachHangRepository.findById(206L)).thenReturn(Optional.of(khachHang));
        giaLapKetQuaTimKiem();

        nhuCauKhachHangService.findNhuCauCuaKhachHang(1L, 206L, KHONG_LOC, 0, 0);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(nhuCauRepository).findAll(ArgumentMatchers.<Specification<NhuCau>>any(), pageable.capture());
        assertEquals(10, pageable.getValue().getPageSize());
    }

    @Test
    void shouldFindNhuCauCuaToiWhenLaKhachHang() {
        when(taiKhoanRepository.findWithChuSoHuuById(2L)).thenReturn(Optional.of(taiKhoanKhachHang(khachHang)));
        when(khachHangRepository.findById(206L)).thenReturn(Optional.of(khachHang));
        giaLapKetQuaTimKiem();

        PageResponse<NhuCauKhachHangResponse> ketQua = nhuCauKhachHangService.findNhuCauCuaToi(
                2L, new NhuCauKhachHangFilter(LoaiBds.DAT, ViTri.TRUNG_TAM, new BigDecimal("80")), 0, 10);

        assertEquals("NC401", ketQua.content().get(0).maNc());
    }

    @Test
    void shouldThrowForbiddenWhenNhanVienGoiNhuCauCuaToi() {
        when(taiKhoanRepository.findWithChuSoHuuById(1L)).thenReturn(Optional.of(taiKhoanNhanVien()));

        BusinessException loi = assertThrows(BusinessException.class,
                () -> nhuCauKhachHangService.findNhuCauCuaToi(1L, KHONG_LOC, 0, 10));

        assertEquals(HttpStatus.FORBIDDEN, loi.getStatus());
    }
}

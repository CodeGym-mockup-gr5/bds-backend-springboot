package com.codegym.bds_backend.khachhang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.auth.TaiKhoan;
import com.codegym.bds_backend.auth.TaiKhoanRepository;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.khachhang.dto.YeuCauDangTinResponse;
import com.codegym.bds_backend.nhanvien.NhanVien;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class YeuCauDangTinServiceTest {
    private static final Long TAI_KHOAN_ID = 1L;

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @InjectMocks
    private YeuCauDangTinService yeuCauDangTinService;

    @Test
    void shouldSetChoDuyetWhenKhachHangChuaDangKyBan() {
        KhachHang khachHang = stubKhachHang(false, TrangThaiDuyet.DA_DUYET);

        YeuCauDangTinResponse response = yeuCauDangTinService.createYeuCauDangTin(TAI_KHOAN_ID);

        assertTrue(khachHang.isLaKhBan());
        assertEquals(TrangThaiDuyet.CHO_DUYET, khachHang.getTrangThaiDuyet());
        assertEquals(Boolean.TRUE, response.laKhBan());
        assertEquals("CHO_DUYET", response.trangThaiDuyet());
    }

    @Test
    void shouldThrowBusinessExceptionWithoutChangingDataWhenKhachHangDangChoDuyet() {
        KhachHang khachHang = stubKhachHang(true, TrangThaiDuyet.CHO_DUYET);

        BusinessException e = assertThrows(BusinessException.class,
                () -> yeuCauDangTinService.createYeuCauDangTin(TAI_KHOAN_ID));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("Yêu cầu đang chờ Admin duyệt", e.getMessage());
        assertTrue(khachHang.isLaKhBan());
        assertEquals(TrangThaiDuyet.CHO_DUYET, khachHang.getTrangThaiDuyet());
    }

    @Test
    void shouldThrowBusinessExceptionWithoutChangingDataWhenKhachHangDaDuyet() {
        KhachHang khachHang = stubKhachHang(true, TrangThaiDuyet.DA_DUYET);

        BusinessException e = assertThrows(BusinessException.class,
                () -> yeuCauDangTinService.createYeuCauDangTin(TAI_KHOAN_ID));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("Tài khoản đã được duyệt đăng tin bán", e.getMessage());
        assertTrue(khachHang.isLaKhBan());
        assertEquals(TrangThaiDuyet.DA_DUYET, khachHang.getTrangThaiDuyet());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTaiKhoanIsNotKhachHang() {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(TAI_KHOAN_ID);
        taiKhoan.setNhanVien(new NhanVien());
        when(taiKhoanRepository.findWithChuSoHuuById(TAI_KHOAN_ID)).thenReturn(Optional.of(taiKhoan));

        assertThrows(NotFoundException.class, () -> yeuCauDangTinService.createYeuCauDangTin(TAI_KHOAN_ID));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTaiKhoanDoesNotExist() {
        when(taiKhoanRepository.findWithChuSoHuuById(TAI_KHOAN_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> yeuCauDangTinService.createYeuCauDangTin(TAI_KHOAN_ID));
    }

    private KhachHang stubKhachHang(boolean laKhBan, TrangThaiDuyet trangThaiDuyet) {
        KhachHang khachHang = new KhachHang();
        khachHang.setId(10L);
        khachHang.setLaKhBan(laKhBan);
        khachHang.setTrangThaiDuyet(trangThaiDuyet);
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(TAI_KHOAN_ID);
        taiKhoan.setKhachHang(khachHang);
        when(taiKhoanRepository.findWithChuSoHuuById(TAI_KHOAN_ID)).thenReturn(Optional.of(taiKhoan));
        return khachHang;
    }
}

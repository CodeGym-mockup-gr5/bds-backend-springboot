package com.codegym.bds_backend.thongbao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ThongBaoServiceTest {
    @Mock
    private ThongBaoRepository thongBaoRepository;

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @InjectMocks
    private ThongBaoService thongBaoService;

    private NhanVien nhanVien;
    private ThongBao thongBao;

    @BeforeEach
    void setUp() {
        nhanVien = new NhanVien();
        nhanVien.setId(101L);
        nhanVien.setHoTen("Nguyễn Thành Long");

        thongBao = new ThongBao();
        thongBao.setId(1L);
        thongBao.setTenThongBao("Thông báo lễ Quốc khánh");
        thongBao.setNoiDung("Nghỉ lễ theo lịch nhà nước");
        thongBao.setNgay(LocalDate.of(2026, 9, 2));
        thongBao.setNguoiTao(nhanVien);
    }

    @Test
    void findThongBao_coKetQua_traVePageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ThongBao> page = new PageImpl<>(List.of(thongBao), pageable, 1);
        when(thongBaoRepository.findThongBaoByTuKhoa("lễ", pageable)).thenReturn(page);

        PageResponse<ThongBaoResponse> response = thongBaoService.findThongBao("lễ", pageable);

        assertEquals(1, response.content().size());
        assertEquals("Thông báo lễ Quốc khánh", response.content().get(0).tenThongBao());
        assertEquals("Nguyễn Thành Long", response.content().get(0).nguoiTaoTen());
    }

    @Test
    void findTopThongBaoMoiNhat_traVeDanhSachTop() {
        when(thongBaoRepository.findTopMoiNhat(any(Pageable.class))).thenReturn(List.of(thongBao));

        List<ThongBaoResponse> list = thongBaoService.findTopThongBaoMoiNhat(5);

        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).id());
    }

    @Test
    void findThongBaoById_tonTai_traVeChiTiet() {
        when(thongBaoRepository.findWithNguoiTaoById(1L)).thenReturn(Optional.of(thongBao));

        ThongBaoResponse response = thongBaoService.findThongBaoById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Thông báo lễ Quốc khánh", response.tenThongBao());
    }

    @Test
    void findThongBaoById_khongTonTai_nemNotFoundException() {
        when(thongBaoRepository.findWithNguoiTaoById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> thongBaoService.findThongBaoById(999L));
    }

    @Test
    void createThongBao_hopLe_taoThanhCong() {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(10L);
        taiKhoan.setNhanVien(nhanVien);

        CreateThongBaoRequest request = new CreateThongBaoRequest(
                LocalDate.of(2026, 9, 2), "Thông báo mới", "Nội dung chi tiết");

        when(taiKhoanRepository.findWithChuSoHuuById(10L)).thenReturn(Optional.of(taiKhoan));
        when(thongBaoRepository.save(any(ThongBao.class))).thenAnswer(invocation -> {
            ThongBao tb = invocation.getArgument(0);
            tb.setId(2L);
            return tb;
        });

        ThongBaoResponse response = thongBaoService.createThongBao(request, 10L);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("Thông báo mới", response.tenThongBao());
        assertEquals(101L, response.nguoiTaoId());
        verify(thongBaoRepository).save(any(ThongBao.class));
    }

    @Test
    void createThongBao_khongPhaiNhanVien_nemBusinessException() {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(10L);
        taiKhoan.setNhanVien(null);

        CreateThongBaoRequest request = new CreateThongBaoRequest(
                LocalDate.now(), "Tiêu đề", "Nội dung");

        when(taiKhoanRepository.findWithChuSoHuuById(10L)).thenReturn(Optional.of(taiKhoan));

        assertThrows(BusinessException.class, () -> thongBaoService.createThongBao(request, 10L));
    }

    @Test
    void updateThongBao_tonTai_capNhatThanhCong() {
        UpdateThongBaoRequest request = new UpdateThongBaoRequest(
                LocalDate.of(2026, 9, 3), "Tiêu đề cập nhật", "Nội dung mới");

        when(thongBaoRepository.findWithNguoiTaoById(1L)).thenReturn(Optional.of(thongBao));
        when(thongBaoRepository.save(any(ThongBao.class))).thenReturn(thongBao);

        ThongBaoResponse response = thongBaoService.updateThongBao(1L, request);

        assertEquals("Tiêu đề cập nhật", response.tenThongBao());
        assertEquals("Nội dung mới", response.noiDung());
        verify(thongBaoRepository).save(thongBao);
    }

    @Test
    void deleteThongBao_tonTai_xoaThanhCong() {
        when(thongBaoRepository.findById(1L)).thenReturn(Optional.of(thongBao));

        thongBaoService.deleteThongBao(1L);

        verify(thongBaoRepository).delete(thongBao);
    }
}

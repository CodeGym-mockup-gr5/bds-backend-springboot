package com.codegym.bds_backend.thongbao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.thongbao.dto.ThongBaoResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @InjectMocks
    private ThongBaoService thongBaoService;

    private ThongBao thongBao;

    @BeforeEach
    void setUp() {
        thongBao = new ThongBao();
        thongBao.setId(1L);
        thongBao.setTenThongBao("Thông báo lễ Quốc khánh");
        thongBao.setNoiDung("Nghỉ lễ theo lịch nhà nước");
        thongBao.setNgay(LocalDate.of(2026, 9, 2));
    }

    @Test
    @DisplayName("Lấy danh sách thông báo phân trang thành công")
    void findThongBao_coKetQua_traVePageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ThongBao> page = new PageImpl<>(List.of(thongBao), pageable, 1);
        when(thongBaoRepository.findThongBaoByTuKhoa("lễ", pageable)).thenReturn(page);

        PageResponse<ThongBaoResponse> response = thongBaoService.findThongBao("lễ", pageable);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals("Thông báo lễ Quốc khánh", response.content().get(0).tenThongBao());
    }

    @Test
    @DisplayName("Lấy danh sách thông báo mới nhất cho widget trang chủ")
    void findTopThongBaoMoiNhat_traVeDanhSachTop() {
        when(thongBaoRepository.findTopMoiNhat(any(Pageable.class))).thenReturn(List.of(thongBao));

        List<ThongBaoResponse> list = thongBaoService.findTopThongBaoMoiNhat(5);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).id());
    }

    @Test
    @DisplayName("Xem chi tiết thông báo tồn tại theo ID")
    void findThongBaoById_tonTai_traVeChiTiet() {
        when(thongBaoRepository.findById(1L)).thenReturn(Optional.of(thongBao));

        ThongBaoResponse response = thongBaoService.findThongBaoById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Thông báo lễ Quốc khánh", response.tenThongBao());
    }

    @Test
    @DisplayName("Xem chi tiết thông báo không tồn tại ném NotFoundException")
    void findThongBaoById_khongTonTai_nemNotFoundException() {
        when(thongBaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> thongBaoService.findThongBaoById(999L));
    }
}

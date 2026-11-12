package com.codegym.bds_backend.thongbao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.common.exception.GlobalExceptionHandler;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.thongbao.dto.ThongBaoResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ThongBaoControllerTest {
    private MockMvc mockMvc;

    @Mock
    private ThongBaoService thongBaoService;

    @InjectMocks
    private ThongBaoController thongBaoController;

    private ThongBaoResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new ThongBaoResponse(
                1L,
                LocalDate.of(2026, 10, 1),
                "Lịch nghỉ lễ cuối năm",
                "Trung tâm thông báo lịch nghỉ lễ..."
        );

        mockMvc = MockMvcBuilders.standaloneSetup(thongBaoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/thong-bao: Lấy danh sách phân trang thành công trả về 200")
    void findThongBao_traVe200VaTrangThongBao() throws Exception {
        PageResponse<ThongBaoResponse> pageResponse = new PageResponse<>(
                List.of(sampleResponse), 0, 10, 1, 1
        );
        when(thongBaoService.findThongBao(any(), any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/thong-bao")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].tenThongBao").value("Lịch nghỉ lễ cuối năm"))
                .andExpect(jsonPath("$.data.content[0].noiDung").value("Trung tâm thông báo lịch nghỉ lễ..."))
                .andExpect(jsonPath("$.data.content[0].ngay").value("2026-10-01"))
                .andExpect(jsonPath("$.data.content[0].nguoiTaoTen").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].nguoiTaoId").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/thong-bao/moi-nhat: Lấy danh sách widget trang chủ trả về 200")
    void findTopThongBaoMoiNhat_traVe200VaDanhSach() throws Exception {
        when(thongBaoService.findTopThongBaoMoiNhat(5)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/thong-bao/moi-nhat").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].tenThongBao").value("Lịch nghỉ lễ cuối năm"))
                .andExpect(jsonPath("$.data[0].nguoiTaoTen").doesNotExist())
                .andExpect(jsonPath("$.data[0].nguoiTaoId").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/thong-bao/{id}: Xem chi tiết thông báo tồn tại trả về 200")
    void findThongBaoById_tonTai_traVe200() throws Exception {
        when(thongBaoService.findThongBaoById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/thong-bao/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.tenThongBao").value("Lịch nghỉ lễ cuối năm"))
                .andExpect(jsonPath("$.data.noiDung").value("Trung tâm thông báo lịch nghỉ lễ..."))
                .andExpect(jsonPath("$.data.nguoiTaoTen").doesNotExist())
                .andExpect(jsonPath("$.data.nguoiTaoId").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/thong-bao/{id}: Thông báo không tồn tại trả về 404")
    void findThongBaoById_khongTonTai_traVe404() throws Exception {
        when(thongBaoService.findThongBaoById(999L))
                .thenThrow(new NotFoundException("Không tìm thấy thông báo với mã: 999"));

        mockMvc.perform(get("/api/v1/thong-bao/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy thông báo với mã: 999"));
    }
}

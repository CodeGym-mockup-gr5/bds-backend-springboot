package com.codegym.bds_backend.thongbao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.exception.GlobalExceptionHandler;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.thongbao.dto.CreateThongBaoRequest;
import com.codegym.bds_backend.thongbao.dto.ThongBaoResponse;
import com.codegym.bds_backend.thongbao.dto.UpdateThongBaoRequest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return new JwtPrincipal(101L, "admin", List.of("ROLE_ADMIN"));
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(thongBaoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
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
                .andExpect(jsonPath("$.data.content[0].nguoiTaoTen").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].nguoiTaoId").doesNotExist());
    }

    @Test
    void findTopThongBaoMoiNhat_traVe200VaDanhSach() throws Exception {
        when(thongBaoService.findTopThongBaoMoiNhat(5)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/thong-bao/moi-nhat").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].nguoiTaoTen").doesNotExist())
                .andExpect(jsonPath("$.data[0].nguoiTaoId").doesNotExist());
    }

    @Test
    void findThongBaoById_tonTai_traVe200() throws Exception {
        when(thongBaoService.findThongBaoById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/thong-bao/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.tenThongBao").value("Lịch nghỉ lễ cuối năm"))
                .andExpect(jsonPath("$.data.nguoiTaoTen").doesNotExist())
                .andExpect(jsonPath("$.data.nguoiTaoId").doesNotExist());
    }

    @Test
    void findThongBaoById_khongTonTai_traVe404() throws Exception {
        when(thongBaoService.findThongBaoById(999L))
                .thenThrow(new NotFoundException("Không tìm thấy thông báo với mã 999"));

        mockMvc.perform(get("/api/v1/thong-bao/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy thông báo với mã 999"));
    }

    @Test
    void createThongBao_hopLe_traVe201() throws Exception {
        when(thongBaoService.createThongBao(any(CreateThongBaoRequest.class), eq(101L)))
                .thenReturn(sampleResponse);

        String json = """
                {
                    "tenThongBao": "Lịch nghỉ lễ cuối năm",
                    "noiDung": "Trung tâm thông báo lịch nghỉ lễ...",
                    "ngay": "2026-10-01"
                }
                """;

        mockMvc.perform(post("/api/v1/thong-bao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void createThongBao_trongDuLieu_traVe400() throws Exception {
        String json = """
                {
                    "tenThongBao": "",
                    "noiDung": ""
                }
                """;

        mockMvc.perform(post("/api/v1/thong-bao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void updateThongBao_hopLe_traVe200() throws Exception {
        when(thongBaoService.updateThongBao(eq(1L), any(UpdateThongBaoRequest.class)))
                .thenReturn(sampleResponse);

        String json = """
                {
                    "tenThongBao": "Lịch nghỉ lễ cập nhật",
                    "noiDung": "Nội dung cập nhật",
                    "ngay": "2026-10-02"
                }
                """;

        mockMvc.perform(put("/api/v1/thong-bao/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteThongBao_hopLe_traVe200() throws Exception {
        doNothing().when(thongBaoService).deleteThongBao(1L);

        mockMvc.perform(delete("/api/v1/thong-bao/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

package com.codegym.bds_backend.hotro;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.common.exception.GlobalExceptionHandler;
import com.codegym.bds_backend.hotro.dto.YeuCauHoTroRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class HoTroControllerTest {
    private MockMvc mockMvc;

    @Mock
    private HoTroService hoTroService;

    @InjectMocks
    private HoTroController hoTroController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hoTroController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void guiYeuCauHoTro_hopLe_traVe200() throws Exception {
        doNothing().when(hoTroService).guiYeuCauHoTro(any(YeuCauHoTroRequest.class));

        String json = """
                {
                    "hoTen": "Trần Văn Hùng",
                    "soDienThoai": "0912345678",
                    "email": "hung.tv@example.com",
                    "noiDung": "Tôi cần tư vấn mua đất nền"
                }
                """;

        mockMvc.perform(post("/api/v1/ho-tro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Gửi yêu cầu hỗ trợ thành công. Chúng tôi sẽ liên hệ lại với bạn sớm nhất!"));
    }

    @Test
    void guiYeuCauHoTro_sdtSaiDinhDang_traVe400() throws Exception {
        String json = """
                {
                    "hoTen": "Trần Văn Hùng",
                    "soDienThoai": "012345",
                    "email": "hung.tv@example.com",
                    "noiDung": "Tôi cần tư vấn mua đất nền"
                }
                """;

        mockMvc.perform(post("/api/v1/ho-tro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("soDienThoai"));
    }

    @Test
    void guiYeuCauHoTro_emailSaiDinhDang_traVe400() throws Exception {
        String json = """
                {
                    "hoTen": "Trần Văn Hùng",
                    "soDienThoai": "0912345678",
                    "email": "hung-khong-dung-dinh-dang",
                    "noiDung": "Tôi cần tư vấn mua đất nền"
                }
                """;

        mockMvc.perform(post("/api/v1/ho-tro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void guiYeuCauHoTro_deTrong_traVe400() throws Exception {
        String json = """
                {
                    "hoTen": "",
                    "soDienThoai": "",
                    "email": "",
                    "noiDung": ""
                }
                """;

        mockMvc.perform(post("/api/v1/ho-tro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }
}

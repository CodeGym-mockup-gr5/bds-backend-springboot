package com.codegym.bds_backend.hotro;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.common.exception.GlobalExceptionHandler;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.hotro.dto.HuongDanResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class HoTroControllerTest {
    private MockMvc mockMvc;

    @Mock
    private HoTroService hoTroService;

    @InjectMocks
    private HoTroController hoTroController;

    private HuongDanResponse sampleHuongDan;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hoTroController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleHuongDan = new HuongDanResponse(
                "tai-khoan",
                "Hướng dẫn sử dụng tài khoản",
                "Cho phép người dùng click vào để đọc hướng dẫn sử dụng tài khoản",
                "Nội dung hướng dẫn chi tiết",
                "/huong-dan/tai-khoan"
        );
    }

    @Test
    void findDanhSachHuongDan_traVe200VaDanhSach() throws Exception {
        when(hoTroService.findDanhSachHuongDan()).thenReturn(List.of(sampleHuongDan));

        mockMvc.perform(get("/api/v1/ho-tro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].maHuongDan").value("tai-khoan"))
                .andExpect(jsonPath("$.data[0].tieuDe").value("Hướng dẫn sử dụng tài khoản"))
                .andExpect(jsonPath("$.data[0].duongDan").value("/huong-dan/tai-khoan"));
    }

    @Test
    void findChiTietHuongDan_tonTai_traVe200() throws Exception {
        when(hoTroService.findChiTietHuongDan("tai-khoan")).thenReturn(sampleHuongDan);

        mockMvc.perform(get("/api/v1/ho-tro/tai-khoan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maHuongDan").value("tai-khoan"))
                .andExpect(jsonPath("$.data.tieuDe").value("Hướng dẫn sử dụng tài khoản"));
    }

    @Test
    void findChiTietHuongDan_khongTonTai_traVe404() throws Exception {
        when(hoTroService.findChiTietHuongDan("khong-ton-tai"))
                .thenThrow(new NotFoundException("Không tìm thấy tài liệu hướng dẫn"));

        mockMvc.perform(get("/api/v1/ho-tro/khong-ton-tai"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy tài liệu hướng dẫn"));
    }
}

package com.codegym.bds_backend.footer;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.common.exception.GlobalExceptionHandler;
import com.codegym.bds_backend.footer.dto.FooterContactResponse;
import com.codegym.bds_backend.footer.dto.FooterLinkResponse;
import com.codegym.bds_backend.footer.dto.FooterResponse;
import com.codegym.bds_backend.footer.dto.FooterSectionResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class FooterControllerTest {
    private MockMvc mockMvc;

    @Mock
    private FooterService footerService;

    @InjectMocks
    private FooterController footerController;

    private FooterResponse sampleFooterResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(footerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleFooterResponse = new FooterResponse(
                "REALIA",
                "Đồng hành cùng bạn trên hành trình an cư",
                "© 2026 REALIA.",
                new FooterContactResponse(
                        "Trung tâm Môi giới Bất động sản REALIA",
                        "123 Đường Nguyễn Văn Linh",
                        "1900 6868",
                        "lienhe@realia.com.vn",
                        "08:00 - 17:30"
                ),
                List.of(
                        new FooterSectionResponse(
                                "Về REALIA",
                                List.of(new FooterLinkResponse("Giới thiệu", "/gioi-thieu"))
                        )
                ),
                List.of(new FooterLinkResponse("Facebook", "https://facebook.com/realia"))
        );
    }

    @Test
    @DisplayName("GET /api/v1/footer: trả về 200 OK và cấu trúc dữ liệu footer đầy đủ")
    void findThongTinFooter_traVe200VaCauTrucDuLieu() throws Exception {
        when(footerService.findThongTinFooter()).thenReturn(sampleFooterResponse);

        mockMvc.perform(get("/api/v1/footer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tenThuongHieu").value("REALIA"))
                .andExpect(jsonPath("$.data.lienHe.soDienThoai").value("1900 6868"))
                .andExpect(jsonPath("$.data.danhSachChuyenMuc[0].tieuDe").value("Về REALIA"))
                .andExpect(jsonPath("$.data.mangXaHoi[0].nhan").value("Facebook"));
    }
}

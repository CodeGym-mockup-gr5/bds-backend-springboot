package com.codegym.bds_backend.header;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.exception.GlobalExceptionHandler;
import com.codegym.bds_backend.header.dto.ActionButtonResponse;
import com.codegym.bds_backend.header.dto.HeaderResponse;
import com.codegym.bds_backend.header.dto.HeaderUserResponse;
import com.codegym.bds_backend.header.dto.MenuItemResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class HeaderControllerTest {

    private MockMvc mockMvcGuest;
    private MockMvc mockMvcAuth;

    @Mock
    private HeaderService headerService;

    @InjectMocks
    private HeaderController headerController;

    @BeforeEach
    void setUp() {
        mockMvcGuest = MockMvcBuilders.standaloneSetup(headerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

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
                return new JwtPrincipal(101L, "minhanh", List.of("ROLE_NHAN_VIEN"));
            }
        };

        mockMvcAuth = MockMvcBuilders.standaloneSetup(headerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/header: Khi chưa đăng nhập trả về 200, không có nút đăng tin, có đăng ký đăng nhập")
    void findThongTinHeader_khiChuaDangNhap_traVe200ChuaDangNhap() throws Exception {
        HeaderResponse guestResponse = new HeaderResponse(
                "/assets/images/logo.png",
                "REALIA",
                false,
                false,
                false,
                List.of(new MenuItemResponse("Mua bán", "/bat-dong-san")),
                List.of(
                        new ActionButtonResponse("DANG_KY", "Đăng ký", "BUTTON", "/dang-ky"),
                        new ActionButtonResponse("DANG_NHAP", "Đăng nhập", "BUTTON", "/dang-nhap")
                ),
                null
        );

        when(headerService.findThongTinHeader(null)).thenReturn(guestResponse);

        mockMvcGuest.perform(get("/api/v1/header"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.daDangNhap").value(false))
                .andExpect(jsonPath("$.data.coNutDangTin").value(false))
                .andExpect(jsonPath("$.data.danhSachHanhDong[0].maHanhDong").value("DANG_KY"))
                .andExpect(jsonPath("$.data.danhSachHanhDong[1].maHanhDong").value("DANG_NHAP"));
    }

    @Test
    @DisplayName("GET /api/v1/header: Khi đã đăng nhập trả về 200, có nút đăng tin, có thông báo và info user")
    void findThongTinHeader_khiDaDangNhap_traVe200DaDangNhap() throws Exception {
        HeaderUserResponse user = new HeaderUserResponse(
                101L, "minhanh", "Minh Anh", "MA", List.of("ROLE_NHAN_VIEN")
        );

        HeaderResponse authResponse = new HeaderResponse(
                "/assets/images/logo.png",
                "REALIA",
                true,
                true,
                true,
                List.of(new MenuItemResponse("Mua bán", "/bat-dong-san")),
                List.of(
                        new ActionButtonResponse("THONG_BAO", "Thông báo", "ICON", "/thong-bao"),
                        new ActionButtonResponse("DANG_TIN", "+ Đăng tin", "BUTTON", "/dang-tin")
                ),
                user
        );

        when(headerService.findThongTinHeader(101L)).thenReturn(authResponse);

        mockMvcAuth.perform(get("/api/v1/header"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.daDangNhap").value(true))
                .andExpect(jsonPath("$.data.coNutDangTin").value(true))
                .andExpect(jsonPath("$.data.coThongBao").value(true))
                .andExpect(jsonPath("$.data.nguoiDung.hoTen").value("Minh Anh"))
                .andExpect(jsonPath("$.data.nguoiDung.kyTuDaiDien").value("MA"))
                .andExpect(jsonPath("$.data.danhSachHanhDong[1].maHanhDong").value("DANG_TIN"));
    }
}

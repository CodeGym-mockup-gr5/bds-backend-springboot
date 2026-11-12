package com.codegym.bds_backend.khachhang;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.khachhang.dto.YeuCauDangTinResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class YeuCauDangTinControllerTest {
    private static final String URL = "/api/v1/khach-hang/me/yeu-cau-dang-tin";

    @MockitoBean
    private YeuCauDangTinService yeuCauDangTinService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void shouldReturnOkWithChoDuyetWhenKhachHangSendsYeuCau() throws Exception {
        when(yeuCauDangTinService.createYeuCauDangTin(5L))
                .thenReturn(new YeuCauDangTinResponse(true, "CHO_DUYET"));

        mockMvc.perform(post(URL).with(user(5L, "ROLE_KHACH_HANG")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Đã gửi yêu cầu đăng tin bán, vui lòng chờ Admin duyệt")))
                .andExpect(jsonPath("$.data.laKhBan", is(true)))
                .andExpect(jsonPath("$.data.trangThaiDuyet", is("CHO_DUYET")));
    }

    @Test
    void shouldReturnConflictWhenYeuCauDangChoDuyet() throws Exception {
        when(yeuCauDangTinService.createYeuCauDangTin(5L))
                .thenThrow(new BusinessException("Yêu cầu đang chờ Admin duyệt"));

        mockMvc.perform(post(URL).with(user(5L, "ROLE_KHACH_HANG")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Yêu cầu đang chờ Admin duyệt")));
    }

    @Test
    void shouldReturnForbiddenWhenNhanVienSendsYeuCau() throws Exception {
        mockMvc.perform(post(URL).with(user(2L, "ROLE_NHAN_VIEN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));

        verify(yeuCauDangTinService, never()).createYeuCauDangTin(any());
    }

    @Test
    void shouldReturnForbiddenWhenAdminSendsYeuCau() throws Exception {
        mockMvc.perform(post(URL).with(user(3L, "ROLE_ADMIN")))
                .andExpect(status().isForbidden());

        verify(yeuCauDangTinService, never()).createYeuCauDangTin(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(post(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));

        verify(yeuCauDangTinService, never()).createYeuCauDangTin(any());
    }

    private static RequestPostProcessor user(Long taiKhoanId, String role) {
        JwtPrincipal principal = new JwtPrincipal(taiKhoanId, "user" + taiKhoanId, List.of(role));
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role))));
    }
}

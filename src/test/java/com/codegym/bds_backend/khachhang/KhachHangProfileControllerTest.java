package com.codegym.bds_backend.khachhang;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.khachhang.dto.KhachHangProfileResponse;
import com.codegym.bds_backend.khachhang.dto.UpdateKhachHangProfileRequest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class KhachHangProfileControllerTest {
    private static final String URL = "/api/v1/khach-hang/profile";

    @MockitoBean
    private KhachHangProfileService khachHangProfileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void shouldGetProfileWhenLoggedInAsKhachHang() throws Exception {
        KhachHangProfileResponse response = new KhachHangProfileResponse(
                20L,
                "MKH-0020",
                "Trần Thị Hoa",
                LocalDate.of(1995, 8, 15),
                GioiTinh.NU,
                "10 Lê Lợi, Đà Nẵng",
                "0905123456",
                "hoa.tran@example.com",
                "048195000123",
                true,
                false,
                TrangThaiDuyet.DA_DUYET);
        when(khachHangProfileService.getProfileKhachHang(5L)).thenReturn(response);

        mockMvc.perform(get(URL).with(user(5L, "ROLE_KHACH_HANG")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Lấy thông tin cá nhân thành công")))
                .andExpect(jsonPath("$.data.maKh", is("MKH-0020")))
                .andExpect(jsonPath("$.data.hoTen", is("Trần Thị Hoa")))
                .andExpect(jsonPath("$.data.trangThaiDuyet", is("DA_DUYET")));
    }

    @Test
    void shouldReturnForbiddenWhenNonKhachHangGetsProfile() throws Exception {
        mockMvc.perform(get(URL).with(user(2L, "ROLE_NHAN_VIEN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));

        mockMvc.perform(get(URL).with(user(3L, "ROLE_ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));

        verify(khachHangProfileService, never()).getProfileKhachHang(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenAnonymousGetsProfile() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));

        verify(khachHangProfileService, never()).getProfileKhachHang(any());
    }

    @Test
    void shouldUpdateProfileAndNormalizeBlankCccdWhenLoggedInAsKhachHang() throws Exception {
        KhachHangProfileResponse response = new KhachHangProfileResponse(
                20L,
                "MKH-0020",
                "Trần Thị Hoa",
                LocalDate.of(1995, 8, 15),
                GioiTinh.NU,
                "10 Lê Lợi, Đà Nẵng",
                "0905123456",
                "hoa.tran@example.com",
                null,
                true,
                false,
                TrangThaiDuyet.DA_DUYET);
        when(khachHangProfileService.updateProfileKhachHang(eq(5L), any(UpdateKhachHangProfileRequest.class)))
                .thenReturn(response);

        String body = """
                {
                  "hoTen": "Trần Thị Hoa",
                  "ngaySinh": "1995-08-15",
                  "gioiTinh": "NU",
                  "diaChi": "10 Lê Lợi, Đà Nẵng",
                  "soDienThoai": "0905123456",
                  "email": "hoa.tran@example.com",
                  "soCccd": "   "
                }
                """;

        mockMvc.perform(put(URL)
                        .with(user(5L, "ROLE_KHACH_HANG"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Cập nhật thông tin cá nhân thành công")))
                .andExpect(jsonPath("$.data.soCccd", is(nullValue())));

        verify(khachHangProfileService).updateProfileKhachHang(
                5L,
                new UpdateKhachHangProfileRequest(
                        "Trần Thị Hoa",
                        LocalDate.of(1995, 8, 15),
                        GioiTinh.NU,
                        "10 Lê Lợi, Đà Nẵng",
                        "0905123456",
                        "hoa.tran@example.com",
                        null));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadInvalid() throws Exception {
        String body = """
                {
                  "hoTen": "",
                  "soDienThoai": "123",
                  "email": "invalid-email",
                  "soCccd": "12345"
                }
                """;

        mockMvc.perform(put(URL)
                        .with(user(5L, "ROLE_KHACH_HANG"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors").isArray());

        verify(khachHangProfileService, never()).updateProfileKhachHang(any(), any());
    }

    @Test
    void shouldReturnConflictWithFieldErrorWhenEmailDuplicates() throws Exception {
        when(khachHangProfileService.updateProfileKhachHang(eq(5L), any(UpdateKhachHangProfileRequest.class)))
                .thenThrow(BusinessException.fieldConflict("email", "Email đã được sử dụng"));

        String body = """
                {
                  "hoTen": "Trần Thị Hoa",
                  "soDienThoai": "0905123456",
                  "email": "dup@example.com",
                  "soCccd": "048195000123"
                }
                """;

        mockMvc.perform(put(URL)
                        .with(user(5L, "ROLE_KHACH_HANG"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Email đã được sử dụng")))
                .andExpect(jsonPath("$.errors[0].field", is("email")))
                .andExpect(jsonPath("$.errors[0].message", is("Email đã được sử dụng")));
    }

    @Test
    void shouldReturnForbiddenWhenNonKhachHangUpdatesProfile() throws Exception {
        String body = """
                {
                  "hoTen": "Trần Thị Hoa",
                  "soDienThoai": "0905123456",
                  "email": "hoa@example.com"
                }
                """;

        mockMvc.perform(put(URL)
                        .with(user(2L, "ROLE_NHAN_VIEN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(put(URL)
                        .with(user(3L, "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(khachHangProfileService, never()).updateProfileKhachHang(any(), any());
    }

    @Test
    void shouldReturnUnauthorizedWhenAnonymousUpdatesProfile() throws Exception {
        String body = """
                {
                  "hoTen": "Trần Thị Hoa",
                  "soDienThoai": "0905123456",
                  "email": "hoa@example.com"
                }
                """;

        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        verify(khachHangProfileService, never()).updateProfileKhachHang(any(), any());
    }

    private static RequestPostProcessor user(Long taiKhoanId, String role) {
        JwtPrincipal principal = new JwtPrincipal(taiKhoanId, "user" + taiKhoanId, List.of(role));
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role))));
    }
}

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangListItemResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangResponse;
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
class KhachHangControllerTest {
    private static final String BASE_URL = "/api/v1/khach-hang";

    @MockitoBean
    private KhachHangService khachHangService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void shouldReturnListWhenNhanVienRequests() throws Exception {
        KhachHangListItemResponse item = new KhachHangListItemResponse(
                12L, "KH012", "Nguyễn Văn An", true, false, TrangThaiDuyet.DA_DUYET, null,
                "an@example.com", "001234567890");
        when(khachHangService.findDanhSachKhachHang(any(), any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(item), 0, 20, 1, 1));

        mockMvc.perform(get(BASE_URL).with(user("ROLE_NHAN_VIEN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content[0].maKh", is("KH012")))
                .andExpect(jsonPath("$.data.content[0].soDienThoai", nullValue()));
    }

    @Test
    void shouldReturnForbiddenWhenKhachHangRequestsList() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user("ROLE_KHACH_HANG")))
                .andExpect(status().isForbidden());

        verify(khachHangService, never()).findDanhSachKhachHang(any(), any(), any(), any());
    }

    @Test
    void shouldCreateKhachHangWhenNhanVienSubmitsValidPayload() throws Exception {
        KhachHangResponse res = new KhachHangResponse(
                15L, "KH015", "Nguyễn Văn An", true, false, TrangThaiDuyet.DA_DUYET,
                LocalDate.of(1995, 5, 20), GioiTinh.NAM, "Hà Nội", null,
                "an@example.com", "001234567890", null, null, null);
        when(khachHangService.createKhachHang(any(), any())).thenReturn(res);

        String body = """
                {
                  "hoTen": "Nguyễn Văn An",
                  "laKhMua": true,
                  "laKhBan": false,
                  "ngaySinh": "1995-05-20",
                  "gioiTinh": "NAM",
                  "diaChi": "Hà Nội",
                  "soDienThoai": "0912345678",
                  "email": "an@example.com",
                  "soCccd": "001234567890"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .with(user("ROLE_NHAN_VIEN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.maKh", is("KH015")));
    }

    @Test
    void shouldReturnBadRequestWhenPhoneInvalidOnCreate() throws Exception {
        String body = """
                {
                  "hoTen": "Nguyễn Văn An",
                  "laKhMua": true,
                  "laKhBan": false,
                  "soDienThoai": "12345"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .with(user("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));

        verify(khachHangService, never()).createKhachHang(any(), any());
    }

    @Test
    void shouldAllowAdminToUpdateAndDeleteKhachHang() throws Exception {
        KhachHangResponse res = new KhachHangResponse(
                12L, "KH012", "Nguyễn Văn Bình", true, true, TrangThaiDuyet.DA_DUYET,
                LocalDate.of(1990, 1, 1), GioiTinh.NAM, "Đà Nẵng", "0988777666",
                "binh@example.com", "001234567890", null, null, null);
        when(khachHangService.updateKhachHang(eq(12L), any(), any())).thenReturn(res);

        String body = """
                {
                  "hoTen": "Nguyễn Văn Bình",
                  "laKhMua": true,
                  "laKhBan": true,
                  "soDienThoai": "0988777666",
                  "email": "binh@example.com"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/{id}", 12L)
                        .with(user("ROLE_NHAN_VIEN", "ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.soDienThoai", is("0988777666")));

        mockMvc.perform(delete(BASE_URL + "/{id}", 12L)
                        .with(user("ROLE_NHAN_VIEN", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Xóa khách hàng thành công")));
    }

    @Test
    void shouldReturnForbiddenWhenNhanVienUpdatesOrDeletesKhachHang() throws Exception {
        String body = """
                {
                  "hoTen": "Nguyễn Văn Bình",
                  "laKhMua": true,
                  "laKhBan": false,
                  "soDienThoai": "0988777666"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/{id}", 12L)
                        .with(user("ROLE_NHAN_VIEN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete(BASE_URL + "/{id}", 12L)
                        .with(user("ROLE_NHAN_VIEN")))
                .andExpect(status().isForbidden());

        verify(khachHangService, never()).updateKhachHang(any(), any(), any());
        verify(khachHangService, never()).deleteKhachHang(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenAnonymousRequests() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete(BASE_URL + "/{id}", 12L))
                .andExpect(status().isUnauthorized());
    }

    private static RequestPostProcessor user(String... roles) {
        List<String> roleList = List.of(roles);
        JwtPrincipal principal = new JwtPrincipal(1L, "user1", roleList);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, roleList.stream().map(SimpleGrantedAuthority::new).toList()));
    }
}

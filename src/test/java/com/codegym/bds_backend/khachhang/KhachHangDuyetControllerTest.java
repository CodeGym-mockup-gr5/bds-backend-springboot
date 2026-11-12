package com.codegym.bds_backend.khachhang;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.dto.DuyetKhachHangResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangChoDuyetResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class KhachHangDuyetControllerTest {
    private static final String URL_CHO_DUYET = "/api/v1/khach-hang/cho-duyet";
    private static final String URL_DUYET = "/api/v1/khach-hang/{id}/duyet";

    @MockitoBean
    private KhachHangDuyetService khachHangDuyetService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void shouldReturnDanhSachChoDuyetWhenAdminRequests() throws Exception {
        KhachHangChoDuyetResponse dong =
                new KhachHangChoDuyetResponse(12L, "KH012", "Nguyễn Văn An", "0912345678", "an@example.com");
        when(khachHangDuyetService.getDanhSachKhachHangChoDuyet(any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(dong), 0, 20, 1, 1));

        mockMvc.perform(get(URL_CHO_DUYET).with(user("ROLE_NHAN_VIEN", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.content[0].id", is(12)))
                .andExpect(jsonPath("$.data.content[0].maKh", is("KH012")))
                .andExpect(jsonPath("$.data.content[0].hoTen", is("Nguyễn Văn An")))
                .andExpect(jsonPath("$.data.content[0].soDienThoai", is("0912345678")))
                .andExpect(jsonPath("$.data.content[0].email", is("an@example.com")));
    }

    @Test
    void shouldApproveKhachHangWhenAdminRequests() throws Exception {
        when(khachHangDuyetService.approveKhachHang(12L))
                .thenReturn(new DuyetKhachHangResponse(12L, "KH012", true, "DA_DUYET"));

        mockMvc.perform(put(URL_DUYET, 12L).with(user("ROLE_NHAN_VIEN", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Duyệt khách hàng thành công")))
                .andExpect(jsonPath("$.data.trangThaiDuyet", is("DA_DUYET")));
    }

    @Test
    void shouldReturnNotFoundWhenKhachHangDoesNotExist() throws Exception {
        when(khachHangDuyetService.approveKhachHang(99L))
                .thenThrow(new NotFoundException("Không tìm thấy khách hàng"));

        mockMvc.perform(put(URL_DUYET, 99L).with(user("ROLE_NHAN_VIEN", "ROLE_ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Không tìm thấy khách hàng")));
    }

    @Test
    void shouldReturnConflictWhenKhachHangDaDuyet() throws Exception {
        when(khachHangDuyetService.approveKhachHang(12L))
                .thenThrow(new BusinessException("Khách hàng không ở trạng thái chờ duyệt"));

        mockMvc.perform(put(URL_DUYET, 12L).with(user("ROLE_NHAN_VIEN", "ROLE_ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Khách hàng không ở trạng thái chờ duyệt")));
    }

    @Test
    void shouldReturnForbiddenWhenNhanVienApproves() throws Exception {
        mockMvc.perform(put(URL_DUYET, 12L).with(user("ROLE_NHAN_VIEN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));

        verify(khachHangDuyetService, never()).approveKhachHang(any());
    }

    @Test
    void shouldReturnForbiddenWhenNhanVienViewsDanhSachChoDuyet() throws Exception {
        mockMvc.perform(get(URL_CHO_DUYET).with(user("ROLE_NHAN_VIEN")))
                .andExpect(status().isForbidden());

        verify(khachHangDuyetService, never()).getDanhSachKhachHangChoDuyet(any());
    }

    @Test
    void shouldReturnForbiddenWhenKhachHangApproves() throws Exception {
        mockMvc.perform(put(URL_DUYET, 12L).with(user("ROLE_KHACH_HANG")))
                .andExpect(status().isForbidden());

        verify(khachHangDuyetService, never()).approveKhachHang(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(put(URL_DUYET, 12L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
        mockMvc.perform(get(URL_CHO_DUYET))
                .andExpect(status().isUnauthorized());

        verify(khachHangDuyetService, never()).approveKhachHang(any());
        verify(khachHangDuyetService, never()).getDanhSachKhachHangChoDuyet(any());
    }

    private static RequestPostProcessor user(String... roles) {
        List<String> danhSachVaiTro = List.of(roles);
        JwtPrincipal principal = new JwtPrincipal(1L, "user1", danhSachVaiTro);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, danhSachVaiTro.stream().map(SimpleGrantedAuthority::new).toList()));
    }
}

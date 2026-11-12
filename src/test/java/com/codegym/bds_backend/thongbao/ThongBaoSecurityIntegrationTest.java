package com.codegym.bds_backend.thongbao;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ThongBaoSecurityIntegrationTest {

    private static final String URL = "/api/v1/thong-bao";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnListWhenAdminViewsList() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldReturnListWhenStaffViewsList() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @WithMockUser(roles = "KHACH_HANG")
    void shouldReturnForbiddenWhenCustomerViewsList() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void shouldReturnUnauthorizedWhenGuestViewsList() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldReturnNotFoundWhenStaffViewsMissingDetail() throws Exception {
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(roles = "KHACH_HANG")
    void shouldReturnForbiddenWhenCustomerViewsDetail() throws Exception {
        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void shouldReturnUnauthorizedWhenGuestViewsDetail() throws Exception {
        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldReturnLatestWhenStaffViewsLatest() throws Exception {
        mockMvc.perform(get(URL + "/moi-nhat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    void shouldReturnUnauthorizedWhenGuestViewsLatest() throws Exception {
        mockMvc.perform(get(URL + "/moi-nhat"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldReturnForbiddenWhenStaffDeletes() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)));
    }
}

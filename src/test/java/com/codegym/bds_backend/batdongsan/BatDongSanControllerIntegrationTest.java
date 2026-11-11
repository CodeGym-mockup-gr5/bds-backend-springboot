package com.codegym.bds_backend.batdongsan;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
class BatDongSanControllerIntegrationTest {

    private static final String URL = "/api/v1/bat-dong-san";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldReturnAllPropertiesNewestFirstWhenStaffSearchesWithoutFilter() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalElements", is(12)))
                .andExpect(jsonPath("$.data.content[0].maBds", is("BDS312")));
    }

    @Test
    void shouldReturnOnlyAvailablePropertiesWhenGuestSearches() throws Exception {
        mockMvc.perform(get(URL).param("tinhTrang", "DA_BAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(8)))
                .andExpect(jsonPath("$.data.content[*].tinhTrang", everyItem(is("CO_SAN"))));
    }

    @Test
    void shouldHideOwnerAndIgnoreOwnerFilterWhenGuestSearches() throws Exception {
        mockMvc.perform(get(URL).param("khachHangId", "210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(8)))
                .andExpect(jsonPath("$.data.content[*].khachHangId", everyItem(nullValue())))
                .andExpect(jsonPath("$.data.content[*].maKh", everyItem(nullValue())))
                .andExpect(jsonPath("$.data.content[*].tenChuSoHuu", everyItem(nullValue())));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldFilterByTypeAndLocationWhenStaffSearches() throws Exception {
        mockMvc.perform(get(URL).param("loaiBds", "DAT").param("viTri", "TRUNG_TAM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", containsInAnyOrder("BDS301", "BDS305", "BDS309")));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldFilterByStatusWhenStaffSearchesAvailableLand() throws Exception {
        mockMvc.perform(get(URL).param("loaiBds", "DAT").param("viTri", "TRUNG_TAM").param("tinhTrang", "CO_SAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", containsInAnyOrder("BDS309")));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldFilterByAreaRangeWhenRangeIsGiven() throws Exception {
        mockMvc.perform(get(URL).param("dienTichTu", "100").param("dienTichDen", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds",
                        containsInAnyOrder("BDS302", "BDS303", "BDS305", "BDS310", "BDS311")));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldFilterByOwnerWhenKhachHangIdIsGiven() throws Exception {
        mockMvc.perform(get(URL).param("khachHangId", "210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", containsInAnyOrder("BDS309", "BDS310")));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldMatchAddressWhenKeywordIsGiven() throws Exception {
        mockMvc.perform(get(URL).param("tuKhoa", "thủ đức"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", containsInAnyOrder("BDS302", "BDS310")));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldUseLowestOrderImageAsThumbnailWhenSearching() throws Exception {
        mockMvc.perform(get(URL).param("khachHangId", "201").param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].maBds", is("BDS301")))
                .andExpect(jsonPath("$.data.content[0].anhDaiDien", is("/uploads/bds/301-1.jpg")));

        mockMvc.perform(get(URL).param("khachHangId", "204").param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].maBds", is("BDS304")))
                .andExpect(jsonPath("$.data.content[0].anhDaiDien", nullValue()));
    }

    @Test
    void shouldReturnBadRequestWhenAreaRangeIsReversed() throws Exception {
        mockMvc.perform(get(URL).param("dienTichTu", "200").param("dienTichDen", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Khoảng diện tích không hợp lệ")));
    }

    @Test
    void shouldReturnBadRequestWithFieldWhenEnumValueIsInvalid() throws Exception {
        mockMvc.perform(get(URL).param("viTri", "ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Giá trị không hợp lệ")))
                .andExpect(jsonPath("$.errors[0].field", is("viTri")))
                .andExpect(jsonPath("$.errors[0].message", is("Giá trị không hợp lệ")));
    }

    @Test
    void shouldReturnBadRequestWhenSortFieldIsNotAllowed() throws Exception {
        mockMvc.perform(get(URL).param("sort", "hoTen,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Tiêu chí sắp xếp không hợp lệ")));
    }
}

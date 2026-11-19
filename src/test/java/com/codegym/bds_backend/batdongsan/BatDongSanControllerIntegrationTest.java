package com.codegym.bds_backend.batdongsan;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codegym.bds_backend.auth.JwtPrincipal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class BatDongSanControllerIntegrationTest {

    private static final String URL = "/api/v1/bat-dong-san";
    private static final String ROLE_KHACH_HANG = "ROLE_KHACH_HANG";

    private MockMvc mockMvc;

    @Autowired
    private BatDongSanRepository batDongSanRepository;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldReturnAllPropertiesNewestFirstWhenStaffSearchesWithoutFilter() throws Exception {
        int totalCount = (int) batDongSanRepository.count();
        String newestMaBds = batDongSanRepository
                .findAllByOrderByNgayDangDescIdDesc(PageRequest.of(0, 1))
                .getContent()
                .get(0)
                .getMaBds();

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalElements", is(totalCount)))
                .andExpect(jsonPath("$.data.content[0].maBds", is(newestMaBds)));
    }

    @Test
    void shouldReturnOnlyAvailablePropertiesWhenGuestSearches() throws Exception {
        mockMvc.perform(get(URL).param("tinhTrang", "DA_BAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(countAvailableProperties())))
                .andExpect(jsonPath("$.data.content[*].tinhTrang", everyItem(is("CO_SAN"))));
    }

    @Test
    void shouldHideOwnerAndIgnoreOwnerFilterWhenGuestSearches() throws Exception {
        mockMvc.perform(get(URL).param("khachHangId", "210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(countAvailableProperties())))
                .andExpect(jsonPath("$.data.content[*].khachHangId", everyItem(nullValue())))
                .andExpect(jsonPath("$.data.content[*].maKh", everyItem(nullValue())))
                .andExpect(jsonPath("$.data.content[*].tenChuSoHuu", everyItem(nullValue())));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldFilterByTypeAndLocationWhenStaffSearches() throws Exception {
        mockMvc.perform(get(URL).param("loaiBds", "DAT").param("viTri", "TRUNG_TAM").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", hasItems("BDS301", "BDS305", "BDS309")))
                .andExpect(jsonPath("$.data.content[*].loaiBds", everyItem(is("DAT"))))
                .andExpect(jsonPath("$.data.content[*].viTri", everyItem(is("TRUNG_TAM"))));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldFilterByStatusWhenStaffSearchesAvailableLand() throws Exception {
        mockMvc.perform(get(URL)
                        .param("loaiBds", "DAT")
                        .param("viTri", "TRUNG_TAM")
                        .param("tinhTrang", "CO_SAN")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", hasItems("BDS309")))
                .andExpect(jsonPath("$.data.content[*].loaiBds", everyItem(is("DAT"))))
                .andExpect(jsonPath("$.data.content[*].viTri", everyItem(is("TRUNG_TAM"))))
                .andExpect(jsonPath("$.data.content[*].tinhTrang", everyItem(is("CO_SAN"))));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldFilterByAreaRangeWhenRangeIsGiven() throws Exception {
        mockMvc.perform(get(URL).param("dienTichTu", "100").param("dienTichDen", "150").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds",
                        hasItems("BDS302", "BDS303", "BDS305", "BDS310", "BDS311")))
                .andExpect(jsonPath("$.data.content[*].dienTich",
                        everyItem(allOf(greaterThanOrEqualTo(100.0), lessThanOrEqualTo(150.0)))));
    }

    @Test
    @WithMockUser(roles = {"NHAN_VIEN", "ADMIN"})
    void shouldFilterByOwnerWhenAdminGivesKhachHangId() throws Exception {
        mockMvc.perform(get(URL).param("khachHangId", "210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", containsInAnyOrder("BDS309", "BDS310")));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldMatchAddressWhenKeywordIsGiven() throws Exception {
        mockMvc.perform(get(URL).param("tuKhoa", "thủ đức").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", hasItems("BDS302", "BDS310")))
                .andExpect(jsonPath("$.data.content[*].diaChi", everyItem(containsStringIgnoringCase("thủ đức"))));
    }

    @Test
    @WithMockUser(roles = {"NHAN_VIEN", "ADMIN"})
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

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldHideOwnerPhoneWhenStaffViewsDetail() throws Exception {
        mockMvc.perform(get(URL + "/301"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maBds", is("BDS301")))
                .andExpect(jsonPath("$.data.tinhTrang", is("DA_BAN")))
                .andExpect(jsonPath("$.data.hinhAnh", hasSize(2)))
                .andExpect(jsonPath("$.data.hinhAnh[0].duongDan", is("/uploads/bds/301-1.jpg")))
                .andExpect(jsonPath("$.data.hinhAnh[1].thuTu", is(1)))
                .andExpect(jsonPath("$.data.chuSoHuu.maKh", is("KH201")))
                .andExpect(jsonPath("$.data.chuSoHuu.soDienThoai", nullValue()));
    }

    @Test
    void shouldHideOwnerWhenGuestViewsDetail() throws Exception {
        mockMvc.perform(get(URL + "/303"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maBds", is("BDS303")))
                .andExpect(jsonPath("$.data.hinhAnh", hasSize(1)))
                .andExpect(jsonPath("$.data.chuSoHuu", nullValue()));
    }

    @Test
    void shouldReturnEmptyImagesWhenPropertyHasNoImage() throws Exception {
        mockMvc.perform(get(URL + "/304"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hinhAnh", hasSize(0)));
    }

    @Test
    void shouldReturnNotFoundWhenGuestViewsSoldOrMissingProperty() throws Exception {
        mockMvc.perform(get(URL + "/301"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Không tìm thấy bất động sản")));

        mockMvc.perform(get(URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWithFieldWhenIdIsNotNumber() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Giá trị không hợp lệ")))
                .andExpect(jsonPath("$.errors[0].field", is("id")));
    }

    @Test
    @WithMockUser(roles = "NHAN_VIEN")
    void shouldShowOwnerAndFilterByOwnerWhenStaffSearches() throws Exception {
        mockMvc.perform(get(URL).param("khachHangId", "210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", containsInAnyOrder("BDS309", "BDS310")))
                .andExpect(jsonPath("$.data.content[*].maKh", everyItem(is("KH210"))))
                .andExpect(jsonPath("$.data.content[*].tenChuSoHuu", everyItem(is("Đỗ Minh Khoa"))));
    }

    @Test
    void shouldShowOwnerOnlyForOwnPropertyWhenCustomerViewsDetail() throws Exception {
        mockMvc.perform(get(URL + "/301").with(khachHang201()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tinhTrang", is("DA_BAN")))
                .andExpect(jsonPath("$.data.chuSoHuu.maKh", is("KH201")));

        mockMvc.perform(get(URL + "/303").with(khachHang201()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chuSoHuu", nullValue()));
    }

    @Test
    void shouldFilterOnlyOwnPropertiesWhenCustomerGivesKhachHangId() throws Exception {
        mockMvc.perform(get(URL).param("khachHangId", "201").param("size", "100").with(khachHang201()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].maBds", hasItems("BDS306")))
                .andExpect(jsonPath("$.data.content[*].maKh", everyItem(is("KH201"))));

        mockMvc.perform(get(URL).param("khachHangId", "203").with(khachHang201()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(countAvailableProperties())));
    }

    private int countAvailableProperties() {
        return (int) batDongSanRepository.count(
                (root, query, cb) -> cb.equal(root.get("tinhTrang"), TinhTrangBds.CO_SAN));
    }

    private static RequestPostProcessor khachHang201() {
        JwtPrincipal principal = new JwtPrincipal(201L, "kh201", List.of(ROLE_KHACH_HANG));
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(ROLE_KHACH_HANG))));
    }
}

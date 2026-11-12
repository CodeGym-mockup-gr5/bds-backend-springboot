package com.codegym.bds_backend.khachhang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codegym.bds_backend.common.enums.TrangThaiDuyet;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.exception.NotFoundException;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.dto.DuyetKhachHangResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangChoDuyetResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class KhachHangDuyetServiceTest {

    @Mock
    private KhachHangRepository khachHangRepository;

    @InjectMocks
    private KhachHangDuyetService khachHangDuyetService;

    @Test
    void shouldApproveKhachHangWhenKhachHangChoDuyet() {
        KhachHang khachHang = khachHang(12L, true, TrangThaiDuyet.CHO_DUYET);
        when(khachHangRepository.findById(12L)).thenReturn(Optional.of(khachHang));

        DuyetKhachHangResponse response = khachHangDuyetService.approveKhachHang(12L);

        assertEquals(TrangThaiDuyet.DA_DUYET, khachHang.getTrangThaiDuyet());
        assertEquals(12L, response.id());
        assertEquals("KH012", response.maKh());
        assertEquals(Boolean.TRUE, response.laKhBan());
        assertEquals("DA_DUYET", response.trangThaiDuyet());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenKhachHangDoesNotExist() {
        when(khachHangRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> khachHangDuyetService.approveKhachHang(99L));
    }

    @Test
    void shouldThrowBusinessExceptionWhenKhachHangDaDuyet() {
        KhachHang khachHang = khachHang(12L, true, TrangThaiDuyet.DA_DUYET);
        when(khachHangRepository.findById(12L)).thenReturn(Optional.of(khachHang));

        BusinessException e = assertThrows(BusinessException.class,
                () -> khachHangDuyetService.approveKhachHang(12L));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("Khách hàng không ở trạng thái chờ duyệt", e.getMessage());
        assertEquals(TrangThaiDuyet.DA_DUYET, khachHang.getTrangThaiDuyet());
    }

    @Test
    void shouldThrowBusinessExceptionWhenKhachHangChuaDangKyBan() {
        KhachHang khachHang = khachHang(12L, false, TrangThaiDuyet.CHO_DUYET);
        when(khachHangRepository.findById(12L)).thenReturn(Optional.of(khachHang));

        BusinessException e = assertThrows(BusinessException.class,
                () -> khachHangDuyetService.approveKhachHang(12L));

        assertEquals("Khách hàng không ở trạng thái chờ duyệt", e.getMessage());
        assertEquals(TrangThaiDuyet.CHO_DUYET, khachHang.getTrangThaiDuyet());
    }

    @Test
    void shouldQueryOnlyChoDuyetSortedByIdAscWhenGetDanhSachChoDuyet() {
        Pageable yeuCau = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "hoTen"));
        KhachHang khachHang = khachHang(12L, true, TrangThaiDuyet.CHO_DUYET);
        khachHang.setHoTen("Nguyễn Văn An");
        khachHang.setSoDienThoai("0912345678");
        khachHang.setEmail("an@example.com");
        when(khachHangRepository.findByLaKhBanTrueAndTrangThaiDuyet(eq(TrangThaiDuyet.CHO_DUYET), any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(khachHang), call.getArgument(1), 6));

        PageResponse<KhachHangChoDuyetResponse> response = khachHangDuyetService.getDanhSachKhachHangChoDuyet(yeuCau);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(khachHangRepository).findByLaKhBanTrueAndTrangThaiDuyet(eq(TrangThaiDuyet.CHO_DUYET), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(5, captor.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "id"), captor.getValue().getSort());
        assertEquals(1, response.content().size());
        assertEquals(new KhachHangChoDuyetResponse(12L, "KH012", "Nguyễn Văn An", "0912345678", "an@example.com"),
                response.content().get(0));
        assertEquals(6, response.totalElements());
    }

    private static KhachHang khachHang(Long id, boolean laKhBan, TrangThaiDuyet trangThaiDuyet) {
        KhachHang khachHang = new KhachHang();
        khachHang.setId(id);
        khachHang.setMaKh("KH012");
        khachHang.setLaKhBan(laKhBan);
        khachHang.setTrangThaiDuyet(trangThaiDuyet);
        return khachHang;
    }
}

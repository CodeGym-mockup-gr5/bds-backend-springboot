package com.codegym.bds_backend.nhucau;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.nhucau.dto.NhuCauKhachHangFilter;
import com.codegym.bds_backend.nhucau.dto.NhuCauKhachHangResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
@Tag(name = "Nhu cầu của khách hàng")
public class NhuCauKhachHangController {
    private final NhuCauKhachHangService nhuCauKhachHangService;

    @GetMapping("/{id}/nhu-cau")
    @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN', 'KHACH_HANG')")
    @Operation(summary = "Xem danh sách nhu cầu của một khách hàng, mới nhất trước. "
            + "Nhân viên và Admin xem mọi khách, khách hàng chỉ xem của chính mình. "
            + "dienTich: chỉ lấy nhu cầu có khoảng diện tích chứa giá trị này")
    public ApiResponse<PageResponse<NhuCauKhachHangResponse>> findNhuCauCuaKhachHang(
            @PathVariable Long id,
            @RequestParam(required = false) LoaiBds loaiBds,
            @RequestParam(required = false) ViTri viTri,
            @RequestParam(required = false) BigDecimal dienTich,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal JwtPrincipal principal) {
        NhuCauKhachHangFilter filter = new NhuCauKhachHangFilter(loaiBds, viTri, dienTich);
        return ApiResponse.ok(
                nhuCauKhachHangService.findNhuCauCuaKhachHang(principal.userId(), id, filter, page, size));
    }

    @GetMapping("/me/nhu-cau")
    @PreAuthorize("hasRole('KHACH_HANG')")
    @Operation(summary = "Khách hàng xem danh sách nhu cầu của chính mình (lịch sử đăng bài), mới nhất trước")
    public ApiResponse<PageResponse<NhuCauKhachHangResponse>> findNhuCauCuaToi(
            @RequestParam(required = false) LoaiBds loaiBds,
            @RequestParam(required = false) ViTri viTri,
            @RequestParam(required = false) BigDecimal dienTich,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal JwtPrincipal principal) {
        NhuCauKhachHangFilter filter = new NhuCauKhachHangFilter(loaiBds, viTri, dienTich);
        return ApiResponse.ok(nhuCauKhachHangService.findNhuCauCuaToi(principal.userId(), filter, page, size));
    }
}

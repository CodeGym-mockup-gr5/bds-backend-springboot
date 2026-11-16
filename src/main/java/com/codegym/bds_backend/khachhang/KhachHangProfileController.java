package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangProfileResponse;
import com.codegym.bds_backend.khachhang.dto.UpdateKhachHangProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/khach-hang/profile")
@RequiredArgsConstructor
@Tag(name = "Thông tin cá nhân khách hàng")
public class KhachHangProfileController {

    private final KhachHangProfileService khachHangProfileService;

    @GetMapping
    @PreAuthorize("hasRole('KHACH_HANG')")
    @Operation(summary = "Xem thông tin cá nhân của khách hàng đang đăng nhập")
    public ApiResponse<KhachHangProfileResponse> getProfileKhachHang(
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(
                khachHangProfileService.getProfileKhachHang(principal.userId()),
                "Lấy thông tin cá nhân thành công");
    }

    @PutMapping
    @PreAuthorize("hasRole('KHACH_HANG')")
    @Operation(summary = "Cập nhật thông tin cá nhân của khách hàng đang đăng nhập")
    public ApiResponse<KhachHangProfileResponse> updateProfileKhachHang(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody UpdateKhachHangProfileRequest request) {
        return ApiResponse.ok(
                khachHangProfileService.updateProfileKhachHang(principal.userId(), request),
                "Cập nhật thông tin cá nhân thành công");
    }
}

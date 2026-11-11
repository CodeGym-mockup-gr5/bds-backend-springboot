package com.codegym.bds_backend.nhanvien;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.nhanvien.dto.NhanVienProfileResponse;
import com.codegym.bds_backend.nhanvien.dto.UpdateNhanVienProfileRequest;
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
@RequestMapping("/api/v1/nhan-vien/profile")
@RequiredArgsConstructor
@Tag(name = "Thông tin cá nhân nhân viên")
public class NhanVienProfileController {

    private final NhanVienProfileService nhanVienProfileService;

    @GetMapping
    @PreAuthorize("hasAnyRole('NHAN_VIEN', 'ADMIN')")
    @Operation(summary = "Xem thông tin cá nhân của nhân viên đang đăng nhập")
    public ApiResponse<NhanVienProfileResponse> getProfileNhanVien(
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(
                nhanVienProfileService.getProfileNhanVien(principal.userId()),
                "Lấy thông tin cá nhân thành công");
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('NHAN_VIEN', 'ADMIN')")
    @Operation(summary = "Cập nhật thông tin cá nhân của nhân viên đang đăng nhập")
    public ApiResponse<NhanVienProfileResponse> updateProfileNhanVien(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody UpdateNhanVienProfileRequest request) {
        return ApiResponse.ok(
                nhanVienProfileService.updateProfileNhanVien(principal.userId(), request),
                "Cập nhật thông tin cá nhân thành công");
    }
}


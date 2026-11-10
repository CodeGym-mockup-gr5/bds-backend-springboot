package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tai-khoan")
@RequiredArgsConstructor
@Tag(name = "Tài khoản")
public class TaiKhoanController {
    private final TaiKhoanService taiKhoanService;

    @PostMapping("/{id}/loi-moi")
    @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
    @Operation(summary = "Gửi lại lời mời đặt mật khẩu cho tài khoản chưa kích hoạt (Admin gửi cho cả nhân viên)")
    public ApiResponse<Void> resendLoiMoi(@PathVariable Long id, @AuthenticationPrincipal JwtPrincipal principal) {
        taiKhoanService.resendLoiMoi(id, principal);
        return ApiResponse.ok(null, "Đã gửi lại lời mời");
    }
}

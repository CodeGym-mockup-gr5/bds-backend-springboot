package com.codegym.bds_backend.auth;

import com.codegym.bds_backend.auth.dto.ForgotPasswordRequest;
import com.codegym.bds_backend.auth.dto.LoginRequest;
import com.codegym.bds_backend.auth.dto.LoginResponse;
import com.codegym.bds_backend.auth.dto.RegisterRequest;
import com.codegym.bds_backend.auth.dto.SetPasswordRequest;
import com.codegym.bds_backend.auth.dto.ThongTinTaiKhoan;
import com.codegym.bds_backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Xác thực")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng tên đăng nhập và mật khẩu, trả về JWT")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.loginTaiKhoan(request), "Đăng nhập thành công");
    }

    @PostMapping("/register")
    @Operation(summary = "Khách hàng tự đăng ký tài khoản, đăng nhập được ngay, không cần Admin duyệt")
    public ResponseEntity<ApiResponse<ThongTinTaiKhoan>> register(@Valid @RequestBody RegisterRequest request) {
        ThongTinTaiKhoan thongTin = authService.registerKhachHang(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(thongTin, "Đăng ký thành công"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Yêu cầu đặt lại mật khẩu, hệ thống gửi liên kết qua email (hiệu lực 30 phút)")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotMatKhau(request);
        return ApiResponse.ok(null, "Nếu tài khoản tồn tại, hướng dẫn đặt lại mật khẩu đã được gửi tới email");
    }

    @PostMapping("/set-password")
    @Operation(summary = "Đặt mật khẩu mới bằng mã trong email (dùng cho lời mời và đặt lại mật khẩu)")
    public ApiResponse<Void> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        authService.setMatKhau(request);
        return ApiResponse.ok(null, "Đặt mật khẩu thành công");
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập")
    public ApiResponse<ThongTinTaiKhoan> me(@AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(authService.findThongTinTaiKhoan(principal.userId()));
    }
}

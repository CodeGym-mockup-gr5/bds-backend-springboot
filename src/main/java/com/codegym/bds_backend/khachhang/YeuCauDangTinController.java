package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.khachhang.dto.YeuCauDangTinResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/khach-hang/me/yeu-cau-dang-tin")
@RequiredArgsConstructor
@Tag(name = "Yêu cầu đăng tin bán của khách hàng")
public class YeuCauDangTinController {

    private final YeuCauDangTinService yeuCauDangTinService;

    @PostMapping
    @PreAuthorize("hasRole('KHACH_HANG')")
    @Operation(summary = "Khách hàng đang đăng nhập gửi yêu cầu đăng tin bán",
            description = "Chỉ khách hàng. Khách chưa đăng ký bán sẽ chuyển sang chờ Admin duyệt (CHO_DUYET). "
                    + "Trả 409 nếu yêu cầu đang chờ duyệt hoặc tài khoản đã được duyệt đăng tin bán.")
    public ApiResponse<YeuCauDangTinResponse> createYeuCauDangTin(@AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(
                yeuCauDangTinService.createYeuCauDangTin(principal.userId()),
                "Đã gửi yêu cầu đăng tin bán, vui lòng chờ Admin duyệt");
    }
}

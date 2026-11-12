package com.codegym.bds_backend.header;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.header.dto.HeaderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/header")
@RequiredArgsConstructor
@Tag(name = "Header", description = "Màn hình Header - Thông tin điều hướng theo SRS 5.1.4")
public class HeaderController {
    private final HeaderService headerService;

    @GetMapping
    @Operation(summary = "Lấy cấu hình thông tin điều hướng trên Header (SRS 5.1.4)")
    public ApiResponse<HeaderResponse> findThongTinHeader(
            @AuthenticationPrincipal JwtPrincipal principal) {
        Long userId = (principal != null) ? principal.userId() : null;
        return ApiResponse.ok(headerService.findThongTinHeader(userId));
    }
}

package com.codegym.bds_backend.footer;

import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.footer.dto.FooterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/footer")
@RequiredArgsConstructor
@Tag(name = "Footer", description = "Chân trang - Cấu hình thông tin trung tâm và điều hướng theo SRS 5.1.5")
public class FooterController {
    private final FooterService footerService;

    @GetMapping
    @Operation(summary = "Lấy thông tin cấu hình chân trang (Footer)")
    public ApiResponse<FooterResponse> findThongTinFooter() {
        return ApiResponse.ok(footerService.findThongTinFooter());
    }
}

package com.codegym.bds_backend.hotro;

import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.hotro.dto.HuongDanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ho-tro")
@RequiredArgsConstructor
@Tag(name = "Hỗ trợ", description = "Màn hình Hỗ trợ - Trợ giúp hướng dẫn người dùng theo SRS 5.1.3")
public class HoTroController {
    private final HoTroService hoTroService;

    @GetMapping
    @Operation(summary = "Lấy danh sách các tài liệu hướng dẫn trợ giúp người dùng (SRS 5.1.3)")
    public ApiResponse<List<HuongDanResponse>> findDanhSachHuongDan() {
        return ApiResponse.ok(hoTroService.findDanhSachHuongDan());
    }

    @GetMapping("/{maHuongDan}")
    @Operation(summary = "Xem chi tiết một tài liệu hướng dẫn trợ giúp theo mã (SRS 5.1.3)")
    public ApiResponse<HuongDanResponse> findChiTietHuongDan(@PathVariable String maHuongDan) {
        return ApiResponse.ok(hoTroService.findChiTietHuongDan(maHuongDan));
    }
}

package com.codegym.bds_backend.thongbao;

import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.thongbao.dto.ThongBaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/thong-bao")
@RequiredArgsConstructor
@Tag(name = "Thông báo", description = "Hiển thị thông báo trên trang chủ theo SRS 5.1.2")
public class ThongBaoController {
    private final ThongBaoService thongBaoService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo phân trang, hỗ trợ tìm kiếm theo từ khóa tiêu đề (SRS 5.1.2)")
    public ApiResponse<PageResponse<ThongBaoResponse>> findThongBao(
            @RequestParam(required = false) String tuKhoa,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(thongBaoService.findThongBao(tuKhoa, PageRequest.of(page, size)));
    }

    @GetMapping("/moi-nhat")
    @Operation(summary = "Lấy danh sách thông báo mới nhất phục vụ widget trang chủ (SRS 5.1.2)")
    public ApiResponse<List<ThongBaoResponse>> findTopThongBaoMoiNhat(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(thongBaoService.findTopThongBaoMoiNhat(limit));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một thông báo theo mã ID (SRS 5.1.2)")
    public ApiResponse<ThongBaoResponse> findThongBaoById(@PathVariable Long id) {
        return ApiResponse.ok(thongBaoService.findThongBaoById(id));
    }
}

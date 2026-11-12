package com.codegym.bds_backend.thongbao;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.exception.BusinessException;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.thongbao.dto.CreateThongBaoRequest;
import com.codegym.bds_backend.thongbao.dto.ThongBaoPublicResponse;
import com.codegym.bds_backend.thongbao.dto.ThongBaoResponse;
import com.codegym.bds_backend.thongbao.dto.UpdateThongBaoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/thong-bao")
@RequiredArgsConstructor
@Tag(name = "Thông báo", description = "Hiển thị thông báo trên trang chủ và quản lý thông báo")
public class ThongBaoController {
    private final ThongBaoService thongBaoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách thông báo phân trang")
    public ApiResponse<PageResponse<ThongBaoResponse>> findThongBao(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        validatePagination(page, size);
        return ApiResponse.ok(thongBaoService.findThongBao(PageRequest.of(page, size)));
    }

    @GetMapping("/moi-nhat")
    @Operation(summary = "Lấy danh sách thông báo mới nhất phục vụ hiển thị widget trang chủ")
    public ApiResponse<List<ThongBaoPublicResponse>> findTopThongBaoMoiNhat(
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok(thongBaoService.findTopThongBaoMoiNhat(limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem chi tiết một thông báo theo mã ID")
    public ApiResponse<ThongBaoResponse> findThongBaoById(@PathVariable Long id) {
        return ApiResponse.ok(thongBaoService.findThongBaoById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo mới thông báo hệ thống (Dành cho Admin)")
    public ResponseEntity<ApiResponse<ThongBaoResponse>> createThongBao(
            @Valid @RequestBody CreateThongBaoRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        ThongBaoResponse response = thongBaoService.createThongBao(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Thêm thông báo thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật nội dung thông báo (Dành cho Admin)")
    public ApiResponse<ThongBaoResponse> updateThongBao(
            @PathVariable Long id,
            @Valid @RequestBody UpdateThongBaoRequest request) {
        return ApiResponse.ok(thongBaoService.updateThongBao(id, request), "Sửa thông báo thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa mềm thông báo (Dành cho Admin)")
    public ApiResponse<Void> deleteThongBao(@PathVariable Long id) {
        thongBaoService.deleteThongBao(id);
        return ApiResponse.ok(null, "Xóa thành công");
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Giá trị phân trang không hợp lệ");
        }
    }
}
package com.codegym.bds_backend.nhanvien;

import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.nhanvien.dto.CreateNhanVienRequest;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nhan-vien")
@RequiredArgsConstructor
@Tag(name = "Nhân viên")
public class NhanVienController {
    private final NhanVienService nhanVienService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem thông tin chi tiết một nhân viên")
    public ApiResponse<NhanVienResponse> getChiTietNhanVien(@PathVariable Long id) {
        return ApiResponse.ok(nhanVienService.findNhanVienById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm nhân viên mới")
    public ResponseEntity<ApiResponse<NhanVienResponse>> postThemNhanVien(
            @Valid @RequestBody CreateNhanVienRequest request) {
        NhanVienResponse response = nhanVienService.createNhanVien(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Thêm nhân viên thành công"));
    }
}

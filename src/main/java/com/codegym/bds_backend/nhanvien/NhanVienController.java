package com.codegym.bds_backend.nhanvien;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.nhanvien.dto.CreateNhanVienRequest;
import com.codegym.bds_backend.nhanvien.dto.NhanVienResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nhan-vien")
@RequiredArgsConstructor
@Tag(name = "Nhân viên")
public class NhanVienController {
    private final NhanVienService nhanVienService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem danh sách nhân viên có phân trang và lọc theo tiêu chí")
    public ApiResponse<PageResponse<NhanVienResponse>> getDanhSachNhanVien(
            @RequestParam(required = false) String maNv,
            @RequestParam(required = false) String tenNv,
            @RequestParam(required = false) String hoTen,
            @RequestParam(required = false) ChucVu chucVu,
            @RequestParam(required = false) String email,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        String nameFilter = (tenNv != null && !tenNv.isBlank()) ? tenNv : hoTen;
        PageResponse<NhanVienResponse> response =
                nhanVienService.findDanhSachNhanVien(maNv, nameFilter, chucVu, email, pageable);
        return ApiResponse.ok(response);
    }

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa mềm nhân viên")
    public ApiResponse<Void> deleteNhanVien(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        nhanVienService.deleteNhanVien(id, principal);
        return ApiResponse.ok(null, "Xóa nhân viên thành công");
    }
}

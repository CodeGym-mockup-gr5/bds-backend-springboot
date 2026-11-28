package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.dto.CreateKhachHangRequest;
import com.codegym.bds_backend.khachhang.dto.KhachHangListItemResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangResponse;
import com.codegym.bds_backend.khachhang.dto.LoaiKhachHang;
import com.codegym.bds_backend.khachhang.dto.UpdateKhachHangRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
@Tag(name = "Khách hàng")
public class KhachHangController {

    private final KhachHangService khachHangService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
    @Operation(summary = "Xem danh sách khách hàng có tìm kiếm, lọc loại khách và phân trang",
            description = "Admin và Nhân viên truy cập được. Số điện thoại chỉ trả về cho Admin.")
    public ApiResponse<PageResponse<KhachHangListItemResponse>> getDanhSachKhachHang(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LoaiKhachHang loaiKh,
            @ParameterObject
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(
                khachHangService.findDanhSachKhachHang(keyword, loaiKh, pageable, principal),
                "Lấy danh sách khách hàng thành công");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
    @Operation(summary = "Xem thông tin chi tiết một khách hàng",
            description = "Admin và Nhân viên truy cập được. Số điện thoại chỉ trả về cho Admin.")
    public ApiResponse<KhachHangResponse> getChiTietKhachHang(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(
                khachHangService.findKhachHangById(id, principal),
                "Lấy thông tin chi tiết khách hàng thành công");
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
    @Operation(summary = "Thêm khách hàng mới",
            description = "Admin và Nhân viên tạo mới khách hàng (mặc định DA_DUYET). "
                    + "Nếu nhập tenDangNhap thì bắt buộc có email để tạo tài khoản và gửi lời mời đặt mật khẩu.")
    public ResponseEntity<ApiResponse<KhachHangResponse>> postThemKhachHang(
            @Valid @RequestBody CreateKhachHangRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        KhachHangResponse response = khachHangService.createKhachHang(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Thêm khách hàng thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin khách hàng",
            description = "Chỉ Admin. Cho phép sửa email và số điện thoại; không cho phép sửa số CCCD.")
    public ApiResponse<KhachHangResponse> putCapNhatKhachHang(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKhachHangRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ApiResponse.ok(
                khachHangService.updateKhachHang(id, request, principal),
                "Cập nhật thông tin khách hàng thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa mềm khách hàng",
            description = "Chỉ Admin. Xóa mềm bằng cách cập nhật deleted_at.")
    public ApiResponse<Void> deleteKhachHang(@PathVariable Long id) {
        khachHangService.deleteKhachHang(id);
        return ApiResponse.ok(null, "Xóa khách hàng thành công");
    }
}

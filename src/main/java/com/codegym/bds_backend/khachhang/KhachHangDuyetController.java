package com.codegym.bds_backend.khachhang;

import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.PageResponse;
import com.codegym.bds_backend.khachhang.dto.DuyetKhachHangResponse;
import com.codegym.bds_backend.khachhang.dto.KhachHangChoDuyetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
@Tag(name = "Duyệt khách hàng đăng tin bán")
public class KhachHangDuyetController {

    private final KhachHangDuyetService khachHangDuyetService;

    @GetMapping("/cho-duyet")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem danh sách khách hàng chờ duyệt đăng tin bán",
            description = "Chỉ Admin. Sắp xếp theo id tăng dần (chờ lâu nhất lên đầu), tham số sort bị bỏ qua.")
    public ApiResponse<PageResponse<KhachHangChoDuyetResponse>> getDanhSachKhachHangChoDuyet(
            @ParameterObject Pageable pageable) {
        return ApiResponse.ok(
                khachHangDuyetService.getDanhSachKhachHangChoDuyet(pageable),
                "Lấy danh sách khách hàng chờ duyệt thành công");
    }

    @PutMapping("/{id}/duyet")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt đăng tin bán cho khách hàng",
            description = "Chỉ Admin. Chuyển khách sang DA_DUYET. Trả 404 nếu không tìm thấy khách, "
                    + "409 nếu khách không ở trạng thái chờ duyệt.")
    public ApiResponse<DuyetKhachHangResponse> approveKhachHang(@PathVariable Long id) {
        return ApiResponse.ok(khachHangDuyetService.approveKhachHang(id), "Duyệt khách hàng thành công");
    }
}

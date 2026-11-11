package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.batdongsan.dto.BatDongSanListItemResponse;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanSearchRequest;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bất động sản", description = "Xem danh sách bất động sản (SRS 5.6.1)")
@RestController
@RequestMapping("/api/v1/bat-dong-san")
@RequiredArgsConstructor
public class BatDongSanController {

    private final BatDongSanService batDongSanService;

    @Operation(
            summary = "Xem danh sách bất động sản",
            description = "Lọc theo loại, vị trí, hướng, khoảng diện tích, khoảng giá (VND), chủ sở hữu, từ khóa; "
                    + "phân trang và sắp xếp. Khách và khách hàng chỉ thấy BĐS có sẵn (CO_SAN), "
                    + "không thấy thông tin chủ sở hữu và không lọc được theo khachHangId.")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ApiResponse<PageResponse<BatDongSanListItemResponse>> searchBatDongSan(
            @Valid @ParameterObject BatDongSanSearchRequest request) {
        return ApiResponse.ok(batDongSanService.searchBatDongSan(request));
    }
}

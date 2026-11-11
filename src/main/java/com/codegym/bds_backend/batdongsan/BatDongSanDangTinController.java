package com.codegym.bds_backend.batdongsan;

import com.codegym.bds_backend.auth.JwtPrincipal;
import com.codegym.bds_backend.batdongsan.dto.BatDongSanResponse;
import com.codegym.bds_backend.batdongsan.dto.ThemBatDongSanRequest;
import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/bat-dong-san")
@RequiredArgsConstructor
@Tag(name = "Đăng tin bất động sản")
public class BatDongSanDangTinController {

    private final BatDongSanDangTinService batDongSanDangTinService;
    private final BatDongSanUploadService batDongSanUploadService;

    @PostMapping
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'NHAN_VIEN', 'ADMIN')")
    @Operation(summary = "Đăng tin bất động sản mới")
    public ResponseEntity<ApiResponse<BatDongSanResponse>> createBatDongSan(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ThemBatDongSanRequest request) {
        BatDongSanResponse response = batDongSanDangTinService.createBatDongSan(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Đăng tin bất động sản thành công"));
    }

    @PostMapping(value = "/hinh-anh", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'NHAN_VIEN', 'ADMIN')")
    @Operation(summary = "Tải lên nhiều hình ảnh bất động sản qua Cloudinary")
    public ApiResponse<List<String>> uploadHinhAnh(
            @RequestParam("files") List<MultipartFile> files) {
        List<String> urls = batDongSanUploadService.uploadHinhAnh(files);
        return ApiResponse.ok(urls, "Tải ảnh lên thành công");
    }

    @GetMapping("/moi-dang")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem danh sách bất động sản mới đăng cho Quản trị viên")
    public ApiResponse<PageResponse<BatDongSanResponse>> getDanhSachBatDongSanMoiDang(
            @ParameterObject Pageable pageable) {
        PageResponse<BatDongSanResponse> response = batDongSanDangTinService.getDanhSachBatDongSanMoiDang(pageable);
        return ApiResponse.ok(response, "Lấy danh sách bất động sản mới đăng thành công");
    }
}

package com.codegym.bds_backend.hotro;

import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.hotro.dto.YeuCauHoTroRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ho-tro")
@RequiredArgsConstructor
@Tag(name = "Hỗ trợ", description = "Tiếp nhận yêu cầu hỗ trợ và liên hệ từ trang chủ")
public class HoTroController {
    private final HoTroService hoTroService;

    @PostMapping
    @Operation(summary = "Gửi form yêu cầu hỗ trợ từ khách hàng trên trang chủ")
    public ApiResponse<Void> guiYeuCauHoTro(@Valid @RequestBody YeuCauHoTroRequest request) {
        hoTroService.guiYeuCauHoTro(request);
        return ApiResponse.ok(null, "Gửi yêu cầu hỗ trợ thành công. Chúng tôi sẽ liên hệ lại với bạn sớm nhất!");
    }
}

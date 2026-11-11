package com.codegym.bds_backend.thongbao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateThongBaoRequest(
        LocalDate ngay,

        @NotBlank(message = "Tên thông báo không được để trống")
        @Size(max = 255, message = "Tên thông báo tối đa 255 ký tự")
        String tenThongBao,

        @NotBlank(message = "Nội dung thông báo không được để trống")
        String noiDung
) {
}

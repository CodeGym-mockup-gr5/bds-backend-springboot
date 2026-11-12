package com.codegym.bds_backend.thongbao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateThongBaoRequest(
        @NotNull(message = "Xin mời nhập đầy đủ thông tin")
        LocalDate ngay,

        @NotBlank(message = "Xin mời nhập đầy đủ thông tin")
        @Size(max = 255, message = "Tên thông báo tối đa 255 ký tự")
        String tenThongBao,

        @NotBlank(message = "Xin mời nhập đầy đủ thông tin")
        @Size(max = 5000, message = "Nội dung thông báo tối đa 5000 ký tự")
        String noiDung
) {
}

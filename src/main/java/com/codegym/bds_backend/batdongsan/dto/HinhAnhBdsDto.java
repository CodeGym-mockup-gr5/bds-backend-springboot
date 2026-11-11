package com.codegym.bds_backend.batdongsan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HinhAnhBdsDto(
        @NotBlank(message = "Đường dẫn ảnh không được để trống")
        @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự")
        String duongDan,
        int thuTu
) {}

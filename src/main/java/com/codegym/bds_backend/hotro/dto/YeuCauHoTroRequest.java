package com.codegym.bds_backend.hotro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record YeuCauHoTroRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
        String hoTen,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải gồm đúng 10 chữ số bắt đầu bằng số 0")
        String soDienThoai,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 254, message = "Email không được vượt quá 254 ký tự")
        String email,

        @NotBlank(message = "Nội dung yêu cầu không được để trống")
        @Size(max = 1000, message = "Nội dung yêu cầu không được vượt quá 1000 ký tự")
        String noiDung
) {
}

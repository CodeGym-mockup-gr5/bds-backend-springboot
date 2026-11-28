package com.codegym.bds_backend.nhanvien.dto;

import com.codegym.bds_backend.common.enums.GioiTinh;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateNhanVienProfileRequest(
        @NotBlank(message = "Xin mời nhập đầy đủ thông tin")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Họ và tên không được chứa ký tự đặc biệt")
        @Size(max = 45, message = "Họ và tên tối đa 45 ký tự")
        String hoTen,

        @NotNull(message = "Xin mời nhập đầy đủ thông tin")
        @Past(message = "Ngày sinh phải trong quá khứ")
        LocalDate ngaySinh,

        @NotNull(message = "Xin mời nhập đầy đủ thông tin")
        GioiTinh gioiTinh,

        @NotBlank(message = "Xin mời nhập đầy đủ thông tin")
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String diaChi) {
}

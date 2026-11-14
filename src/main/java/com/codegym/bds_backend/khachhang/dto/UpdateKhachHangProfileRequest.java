package com.codegym.bds_backend.khachhang.dto;

import com.codegym.bds_backend.common.enums.GioiTinh;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateKhachHangProfileRequest(
        @NotBlank(message = "Xin mời nhập đầy đủ thông tin")
        @Pattern(regexp = "^[\\p{L} ]+$", message = "Họ và tên không được chứa ký tự đặc biệt")
        @Size(max = 45, message = "Họ và tên tối đa 45 ký tự")
        String hoTen,

        @Past(message = "Ngày sinh phải trong quá khứ")
        LocalDate ngaySinh,

        GioiTinh gioiTinh,

        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String diaChi,

        @NotBlank(message = "Xin mời nhập đầy đủ thông tin")
        @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ")
        String soDienThoai,

        @NotBlank(message = "Xin mời nhập đầy đủ thông tin")
        @Email(message = "Email không hợp lệ!")
        @Size(max = 254, message = "Email tối đa 254 ký tự")
        String email,

        @Pattern(regexp = "^[0-9]{12}$", message = "Số CCCD gồm đúng 12 chữ số")
        String soCccd) {

    public UpdateKhachHangProfileRequest {
        soCccd = (soCccd == null || soCccd.isBlank()) ? null : soCccd.trim();
    }
}

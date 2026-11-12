package com.codegym.bds_backend.nhanvien.dto;

import com.codegym.bds_backend.common.enums.GioiTinh;
import com.codegym.bds_backend.nhanvien.ChucVu;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateNhanVienRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 45, message = "Họ tên tối đa 45 ký tự")
        String hoTen,

        @NotNull(message = "Vui lòng chọn ngày sinh")
        @Past(message = "Ngày sinh phải ở trong quá khứ")
        LocalDate ngaySinh,

        @NotNull(message = "Vui lòng chọn giới tính")
        GioiTinh gioiTinh,

        @NotBlank(message = "Vui lòng nhập địa chỉ")
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String diaChi,

        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại không hợp lệ")
        String soDienThoai,

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        @Size(max = 254, message = "Email tối đa 254 ký tự")
        String email,

        @NotNull(message = "Vui lòng chọn chức vụ")
        ChucVu chucVu,

        Boolean laAdmin) {
}


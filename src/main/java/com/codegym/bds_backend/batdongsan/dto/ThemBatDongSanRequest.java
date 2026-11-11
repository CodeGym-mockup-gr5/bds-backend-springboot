package com.codegym.bds_backend.batdongsan.dto;

import com.codegym.bds_backend.batdongsan.Huong;
import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record ThemBatDongSanRequest(
        Long khachHangId,

        @NotNull(message = "Xin mời chọn loại bất động sản")
        LoaiBds loaiBds,

        @NotBlank(message = "Xin mời nhập địa chỉ")
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String diaChi,

        @NotNull(message = "Xin mời chọn vị trí")
        ViTri viTri,

        @NotNull(message = "Xin mời chọn hướng")
        Huong huong,

        @NotNull(message = "Vui lòng nhập diện tích")
        @DecimalMin(value = "0.01", message = "Diện tích phải lớn hơn hoặc bằng 0.01 m2")
        @Digits(integer = 8, fraction = 2, message = "Diện tích không hợp lệ")
        BigDecimal dienTich,

        @NotNull(message = "Vui lòng nhập giá")
        @PositiveOrZero(message = "Giá tiền không hợp lệ")
        @Digits(integer = 15, fraction = 0, message = "Giá tiền phải là số nguyên")
        BigDecimal gia,

        @Size(max = 255, message = "Thông tin pháp lý tối đa 255 ký tự")
        String phapLy,

        @Size(max = 50, message = "Ghi chú tối đa 50 ký tự")
        String ghiChu,

        @NotNull(message = "Danh sách hình ảnh không được để trống")
        @Size(min = 1, max = 10, message = "Số lượng hình ảnh phải từ 1 đến 10 ảnh")
        List<@NotNull(message = "Thông tin hình ảnh không được để trống")
                @Valid HinhAnhBdsDto> danhSachHinhAnh
) {}

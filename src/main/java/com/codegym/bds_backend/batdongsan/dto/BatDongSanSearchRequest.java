package com.codegym.bds_backend.batdongsan.dto;

import com.codegym.bds_backend.common.enums.Huong;
import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.TinhTrangBds;
import com.codegym.bds_backend.common.enums.ViTri;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Tham số lọc của SRS 5.6.1. Mọi tham số đều tùy chọn; diện tích và giá tìm theo khoảng Từ–Đến. */
public record BatDongSanSearchRequest(
        @Schema(description = "Loại BĐS", example = "DAT")
        LoaiBds loaiBds,

        @Schema(description = "Vị trí", example = "TRUNG_TAM")
        ViTri viTri,

        @Schema(description = "Hướng", example = "DONG")
        Huong huong,

        @Schema(description = "Tình trạng. Chỉ Admin, Nhân viên lọc được; khách luôn chỉ thấy CO_SAN",
                example = "CO_SAN")
        TinhTrangBds tinhTrang,

        @Schema(description = "Diện tích từ (m²)", example = "100")
        @PositiveOrZero(message = "Diện tích không hợp lệ")
        BigDecimal dienTichTu,

        @Schema(description = "Diện tích đến (m²)", example = "150")
        @PositiveOrZero(message = "Diện tích không hợp lệ")
        BigDecimal dienTichDen,

        @Schema(description = "Giá từ (VND)", example = "1000000000")
        @PositiveOrZero(message = "Giá tiền không hợp lệ")
        BigDecimal giaTu,

        @Schema(description = "Giá đến (VND)", example = "5000000000")
        @PositiveOrZero(message = "Giá tiền không hợp lệ")
        BigDecimal giaDen,

        @Schema(description = "Id khách hàng chủ sở hữu (màn Xem danh sách BĐS của khách hàng, SRS 5.5.5)",
                example = "210")
        @Positive(message = "Mã khách hàng không hợp lệ")
        Long khachHangId,

        @Schema(description = "Từ khóa tìm trong mã BĐS và địa chỉ", example = "Thủ Đức")
        @Size(max = 100, message = "Từ khóa tối đa 100 ký tự")
        String tuKhoa,

        @Schema(description = "Số trang, bắt đầu từ 0", example = "0")
        @Min(value = 0, message = "Số trang không hợp lệ")
        Integer page,

        @Schema(description = "Số bản ghi mỗi trang (mặc định 10, tối đa 100)", example = "10")
        @Min(value = 1, message = "Kích thước trang không hợp lệ")
        Integer size,

        @Schema(description = "Sắp xếp: ngayDang | gia | dienTich | giaM2 | id, kèm asc hoặc desc",
                example = "ngayDang,desc")
        String sort) {
}

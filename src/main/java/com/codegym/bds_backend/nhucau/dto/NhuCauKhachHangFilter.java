package com.codegym.bds_backend.nhucau.dto;

import com.codegym.bds_backend.common.enums.LoaiBds;
import com.codegym.bds_backend.common.enums.ViTri;
import java.math.BigDecimal;

public record NhuCauKhachHangFilter(LoaiBds loaiBds, ViTri viTri, BigDecimal dienTich) {
}

package com.codegym.bds_backend.common.response;

import java.util.List;
import org.springframework.data.domain.Page;

/** Dữ liệu phân trang: page tính từ 0 (quy ước mục C.6). */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}

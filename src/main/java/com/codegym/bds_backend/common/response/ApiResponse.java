package com.codegym.bds_backend.common.response;

import java.util.List;

/** Khuôn response chung cho mọi API (quy ước mục C.4). */
public record ApiResponse<T>(boolean success, T data, String message, List<FieldErrorItem> errors) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    public static ApiResponse<Void> error(String message, List<FieldErrorItem> errors) {
        return new ApiResponse<>(false, null, message, errors);
    }
}

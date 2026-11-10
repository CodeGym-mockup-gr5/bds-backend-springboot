package com.codegym.bds_backend.common.response;

/** Lỗi gắn với một trường nhập, FE hiển thị ngay dưới ô nhập tương ứng. */
public record FieldErrorItem(String field, String message) {
}

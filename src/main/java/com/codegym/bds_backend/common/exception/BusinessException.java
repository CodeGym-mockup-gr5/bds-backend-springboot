package com.codegym.bds_backend.common.exception;

import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ. Mặc định trả 409 (trùng hoặc vi phạm quy tắc nghiệp vụ). */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message) {
        this(HttpStatus.CONFLICT, message);
    }

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

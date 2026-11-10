package com.codegym.bds_backend.common.exception;

import org.springframework.http.HttpStatus;

/** Không tìm thấy dữ liệu, trả 404. */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

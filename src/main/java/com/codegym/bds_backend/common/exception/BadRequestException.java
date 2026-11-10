package com.codegym.bds_backend.common.exception;

import org.springframework.http.HttpStatus;

/** Dữ liệu gửi lên sai, trả 400. */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

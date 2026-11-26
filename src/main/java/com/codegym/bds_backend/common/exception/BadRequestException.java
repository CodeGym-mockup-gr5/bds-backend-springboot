package com.codegym.bds_backend.common.exception;

import com.codegym.bds_backend.common.response.FieldErrorItem;
import java.util.List;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public BadRequestException(String message, List<FieldErrorItem> errors) {
        super(HttpStatus.BAD_REQUEST, message, errors);
    }

    public static BadRequestException fieldError(String field, String message) {
        return new BadRequestException(message, List.of(new FieldErrorItem(field, message)));
    }
}

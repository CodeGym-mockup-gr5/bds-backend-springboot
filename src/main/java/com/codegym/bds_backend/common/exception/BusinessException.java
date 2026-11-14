package com.codegym.bds_backend.common.exception;

import com.codegym.bds_backend.common.response.FieldErrorItem;
import org.springframework.http.HttpStatus;

import java.util.List;

public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final List<FieldErrorItem> errors;

    public BusinessException(String message) {
        this(HttpStatus.CONFLICT, message, null);
    }

    public BusinessException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public BusinessException(HttpStatus status, String message, List<FieldErrorItem> errors) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    public static BusinessException fieldConflict(String field, String message) {
        return new BusinessException(HttpStatus.CONFLICT, message, List.of(new FieldErrorItem(field, message)));
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<FieldErrorItem> getErrors() {
        return errors;
    }
}

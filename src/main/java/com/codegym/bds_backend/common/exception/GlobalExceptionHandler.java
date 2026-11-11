package com.codegym.bds_backend.common.exception;

import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.FieldErrorItem;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String MSG_INVALID = "Dữ liệu không hợp lệ";
    private static final String MSG_INVALID_VALUE = "Giá trị không hợp lệ";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return build(e.getStatus(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException e) {
        List<FieldErrorItem> errors = e.getBindingResult().getFieldErrors().stream()
                // Lỗi ép kiểu (vd viTri=ABC) có thông báo kỹ thuật tiếng Anh, thay bằng câu tiếng Việt
                .map(f -> new FieldErrorItem(
                        f.getField(), f.isBindingFailure() ? MSG_INVALID_VALUE : f.getDefaultMessage()))
                .toList();
        String message = errors.isEmpty() ? MSG_INVALID : errors.get(0).message();
        return build(HttpStatus.BAD_REQUEST, message, errors);
    }

    @ExceptionHandler({
        HandlerMethodValidationException.class,
        ConstraintViolationException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleParamValidation(Exception e) {
        return build(HttpStatus.BAD_REQUEST, MSG_INVALID, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không đúng định dạng", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleIntegrity(DataIntegrityViolationException e) {
        log.warn("Vi phạm ràng buộc dữ liệu: {}", e.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleDenied(AccessDeniedException e) {
        return build(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện chức năng này", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return build(HttpStatus.NOT_FOUND, "Không tìm thấy đường dẫn", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức không được hỗ trợ", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Lỗi hệ thống không mong đợi", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống gặp lỗi, vui lòng thử lại sau", null);
    }

    private static ResponseEntity<ApiResponse<Void>> build(
            HttpStatus status, String message, List<FieldErrorItem> errors) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, errors));
    }
}

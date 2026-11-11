package com.codegym.bds_backend.common.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.codegym.bds_backend.common.response.ApiResponse;
import com.codegym.bds_backend.common.response.FieldErrorItem;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnFieldAndVietnameseMessageWhenParamTypeMismatches() {
        MethodArgumentTypeMismatchException e = new MethodArgumentTypeMismatchException(
                "ABC", Long.class, "id", mock(MethodParameter.class), new IllegalArgumentException("bad"));

        ResponseEntity<ApiResponse<Void>> response = handler.handleTypeMismatch(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Giá trị không hợp lệ");
        assertThat(response.getBody().errors()).containsExactly(new FieldErrorItem("id", "Giá trị không hợp lệ"));
    }
}

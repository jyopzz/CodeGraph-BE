package com.CodeGraph.common.exception;

import com.CodeGraph.common.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(
            BusinessException exception) {

        BaseResponse<Void> response = new BaseResponse<>(
                null,
                "Business validation failed",
                exception.getBusinessValidation(),
                false,
                exception.getResponseCode()
        );

        return ResponseEntity
                .status(exception.getResponseCode())
                .body(response);
    }
}
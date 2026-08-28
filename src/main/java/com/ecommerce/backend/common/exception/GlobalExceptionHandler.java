package com.ecommerce.backend.common.exception;

import com.ecommerce.backend.common.response.ErrorResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
    return ResponseEntity.status(ex.getStatus())
        .body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    List<ErrorResponse.FieldError> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
            .toList();
    return ResponseEntity.badRequest().body(ErrorResponse.ofValidation("Invalid request", details));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    return ResponseEntity.internalServerError()
        .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, ex.getMessage()));
  }
}

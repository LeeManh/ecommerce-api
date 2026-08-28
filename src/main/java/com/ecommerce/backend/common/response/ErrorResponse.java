package com.ecommerce.backend.common.response;

import com.ecommerce.backend.common.exception.ErrorCode;
import java.time.Instant;
import java.util.List;

public record ErrorResponse(boolean success, ErrorDetail error, Instant timestamp) {
  public static record ErrorDetail(ErrorCode error, String message, List<FieldError> details) {}

  public static record FieldError(String field, String message) {}

  public static ErrorResponse of(ErrorCode code, String message) {
    return new ErrorResponse(false, new ErrorDetail(code, message, null), Instant.now());
  }

  public static ErrorResponse ofValidation(String message, List<FieldError> details) {
    return new ErrorResponse(
        false, new ErrorDetail(ErrorCode.VALIDATION_ERROR, message, details), Instant.now());
  }
}

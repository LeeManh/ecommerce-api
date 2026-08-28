package com.ecommerce.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  USER_NOT_FOUND(HttpStatus.NOT_FOUND),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);


  private final HttpStatus status;

  ErrorCode(HttpStatus status) {
    this.status = status;
  }
}

package com.ecommerce.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  USER_NOT_FOUND(HttpStatus.NOT_FOUND),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND),
  CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND),
  SKU_ALREADY_EXISTS(HttpStatus.CONFLICT),
  CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND),
  CART_EMPTY(HttpStatus.BAD_REQUEST),
  PRODUCT_NOT_AVAILABLE(HttpStatus.CONFLICT),
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND),
  ORDER_NOT_PENDING(HttpStatus.CONFLICT),
  PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED),
  DUPLICATE_REQUEST(HttpStatus.CONFLICT),
  INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus status;

  ErrorCode(HttpStatus status) {
    this.status = status;
  }
}

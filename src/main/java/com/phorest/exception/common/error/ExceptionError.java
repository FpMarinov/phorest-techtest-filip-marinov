package com.phorest.exception.common.error;

import org.springframework.http.HttpStatus;

/** Represents HTTP and internal error codes for API errors. */
public interface ExceptionError {
  HttpStatus getHttpStatus();

  String getErrorCode();
}

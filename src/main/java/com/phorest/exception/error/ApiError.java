package com.phorest.exception.error;

import com.phorest.exception.common.error.ExceptionError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Represents HTTP and internal error codes for API errors. */
public enum ApiError implements ExceptionError {
  // Specific API errors
  CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "001"),
  APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "002"),
  PURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "003"),
  SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "004"),
  INVALID_CSV_FILE(HttpStatus.CONFLICT, "005"),

  // General API errors
  PROPERTY_REFERENCE(HttpStatus.BAD_REQUEST, "100"),
  DATA_INTEGRITY(HttpStatus.CONFLICT, "101"),
  ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "102"),
  CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "103"),

  // Bad request error
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "400"),

  // Internal error
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500");

  @Getter private final HttpStatus httpStatus;
  @Getter private final String errorCode;

  ApiError(HttpStatus httpStatus, String errorCode) {
    this.httpStatus = httpStatus;
    this.errorCode = errorCode;
  }
}

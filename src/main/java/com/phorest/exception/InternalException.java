package com.phorest.exception;

import com.phorest.exception.common.exception.BackendTechnicalTestException;
import com.phorest.exception.error.ApiError;

public class InternalException extends BackendTechnicalTestException {
  public static final String MESSAGE = "An internal server error occurred.";

  public InternalException() {
    super(ApiError.INTERNAL_ERROR, MESSAGE);
  }

  public InternalException(Throwable cause) {
    super(ApiError.INTERNAL_ERROR, MESSAGE, cause);
  }

  public InternalException(String errorMessage) {
    super(ApiError.INTERNAL_ERROR, errorMessage);
  }

  public InternalException(String errorMessage, Throwable cause) {
    super(ApiError.INTERNAL_ERROR, errorMessage, cause);
  }
}

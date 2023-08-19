package com.phorest.exception.common.exception;

import com.phorest.exception.common.error.ExceptionError;
import lombok.Getter;

/** Base API Exception. */
public class BackendTechnicalTestException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  @Getter private ExceptionError error;

  public BackendTechnicalTestException(ExceptionError error, String errorMessage) {
    super(errorMessage);

    this.error = error;
  }

  public BackendTechnicalTestException(ExceptionError error, String errorMessage, Throwable cause) {
    super(errorMessage, cause);

    this.error = error;
  }
}

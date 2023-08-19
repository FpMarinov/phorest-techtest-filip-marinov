package com.phorest.exception;

import com.phorest.exception.common.exception.BackendTechnicalTestException;
import com.phorest.exception.error.ApiError;

public class InvalidCsvFileException extends BackendTechnicalTestException {
  public static final String MESSAGE = "The csv file you're trying to upload is invalid.";

  public InvalidCsvFileException() {
    super(ApiError.INVALID_CSV_FILE, MESSAGE);
  }

  public InvalidCsvFileException(Throwable cause) {
    super(ApiError.INVALID_CSV_FILE, MESSAGE, cause);
  }

  public InvalidCsvFileException(String errorMessage) {
    super(ApiError.INVALID_CSV_FILE, errorMessage);
  }

  public InvalidCsvFileException(String errorMessage, Throwable cause) {
    super(ApiError.INVALID_CSV_FILE, errorMessage, cause);
  }
}

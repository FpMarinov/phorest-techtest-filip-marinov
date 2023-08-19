package com.phorest.exception;

import com.phorest.exception.common.exception.BackendTechnicalTestException;
import com.phorest.exception.error.ApiError;
import java.util.UUID;

public class ClientNotFoundException extends BackendTechnicalTestException {
  public static final String MESSAGE = "The Client with id (%s) does not exist.";

  public ClientNotFoundException(UUID clientId) {
    super(ApiError.CLIENT_NOT_FOUND, MESSAGE.formatted(clientId));
  }

  public ClientNotFoundException(UUID clientId, Throwable cause) {
    super(ApiError.CLIENT_NOT_FOUND, MESSAGE.formatted(clientId), cause);
  }

  public ClientNotFoundException(String errorMessage) {
    super(ApiError.CLIENT_NOT_FOUND, errorMessage);
  }

  public ClientNotFoundException(String errorMessage, Throwable cause) {
    super(ApiError.CLIENT_NOT_FOUND, errorMessage, cause);
  }
}

package com.phorest.exception;

import com.phorest.exception.common.exception.BackendTechnicalTestException;
import com.phorest.exception.error.ApiError;
import java.util.UUID;

public class ServiceNotFoundException extends BackendTechnicalTestException {
  public static final String MESSAGE = "The Service with id (%s) does not exist.";

  public ServiceNotFoundException(UUID serviceId) {
    super(ApiError.SERVICE_NOT_FOUND, MESSAGE.formatted(serviceId));
  }

  public ServiceNotFoundException(UUID serviceId, Throwable cause) {
    super(ApiError.SERVICE_NOT_FOUND, MESSAGE.formatted(serviceId), cause);
  }

  public ServiceNotFoundException(String errorMessage) {
    super(ApiError.SERVICE_NOT_FOUND, errorMessage);
  }

  public ServiceNotFoundException(String errorMessage, Throwable cause) {
    super(ApiError.SERVICE_NOT_FOUND, errorMessage, cause);
  }
}

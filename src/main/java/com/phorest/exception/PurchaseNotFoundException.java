package com.phorest.exception;

import com.phorest.exception.common.exception.BackendTechnicalTestException;
import com.phorest.exception.error.ApiError;
import java.util.UUID;

public class PurchaseNotFoundException extends BackendTechnicalTestException {
  public static final String MESSAGE = "The Purchase with id (%s) does not exist.";

  public PurchaseNotFoundException(UUID purchaseId) {
    super(ApiError.PURCHASE_NOT_FOUND, MESSAGE.formatted(purchaseId));
  }

  public PurchaseNotFoundException(UUID purchaseId, Throwable cause) {
    super(ApiError.PURCHASE_NOT_FOUND, MESSAGE.formatted(purchaseId), cause);
  }

  public PurchaseNotFoundException(String errorMessage) {
    super(ApiError.PURCHASE_NOT_FOUND, errorMessage);
  }

  public PurchaseNotFoundException(String errorMessage, Throwable cause) {
    super(ApiError.PURCHASE_NOT_FOUND, errorMessage, cause);
  }
}

package com.phorest.exception;

import com.phorest.exception.common.exception.BackendTechnicalTestException;
import com.phorest.exception.error.ApiError;
import java.util.UUID;

public class AppointmentNotFoundException extends BackendTechnicalTestException {
  public static final String MESSAGE = "The Appointment with id (%s) does not exist.";

  public AppointmentNotFoundException(UUID appointmentId) {
    super(ApiError.APPOINTMENT_NOT_FOUND, MESSAGE.formatted(appointmentId));
  }

  public AppointmentNotFoundException(UUID appointmentId, Throwable cause) {
    super(ApiError.APPOINTMENT_NOT_FOUND, MESSAGE.formatted(appointmentId), cause);
  }

  public AppointmentNotFoundException(String errorMessage) {
    super(ApiError.APPOINTMENT_NOT_FOUND, errorMessage);
  }

  public AppointmentNotFoundException(String errorMessage, Throwable cause) {
    super(ApiError.APPOINTMENT_NOT_FOUND, errorMessage, cause);
  }
}

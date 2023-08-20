package com.phorest.controller.advice;

import static com.phorest.exception.error.ApiError.ARGUMENT_TYPE_MISMATCH;
import static com.phorest.exception.error.ApiError.BAD_REQUEST;
import static com.phorest.exception.error.ApiError.CONSTRAINT_VIOLATION;
import static com.phorest.exception.error.ApiError.DATA_INTEGRITY;
import static com.phorest.exception.error.ApiError.INTERNAL_ERROR;
import static com.phorest.exception.error.ApiError.PROPERTY_REFERENCE;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import com.phorest.exception.InternalException;
import com.phorest.exception.common.error.ExceptionError;
import com.phorest.exception.common.error.ExceptionErrorField;
import com.phorest.exception.common.error.ExceptionErrorMessage;
import com.phorest.exception.common.exception.BackendTechnicalTestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {
  public static final MediaType DEFAULT_RESPONSE_TYPE = MediaType.APPLICATION_JSON;
  public static final String CONSTRAINT_VIOLATION_MESSAGE = "Constraint violation";
  public static final String METHOD_ARGUMENT_NOT_VALID_MESSAGE = "Argument validation failed";

  private final Clock clock;

  // Base API exception
  @ExceptionHandler(BackendTechnicalTestException.class)
  public ResponseEntity<Object> handleBackendTechnicalTestException(
      BackendTechnicalTestException ex) {
    log.warn("[EXCEPTION] {}", ex.getMessage());

    return getErrorMessage(ex.getMessage(), ex.getError());
  }

  // JpaRepository method names not matching entity properties exception
  @ExceptionHandler(PropertyReferenceException.class)
  public ResponseEntity<Object> propertyReferenceException(PropertyReferenceException ex) {
    return getErrorMessage(ex.getMessage(), PROPERTY_REFERENCE);
  }

  // Duplicate key exception
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> dataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    int start = ex.getRootCause().getMessage().indexOf("Detail:");
    String message = ex.getRootCause().getMessage().substring(start + 7).trim();

    return getErrorMessage(message, DATA_INTEGRITY);
  }

  // Controller method argument conversion failed exception
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> methodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    return getErrorMessage(ex.getRootCause().getMessage(), ARGUMENT_TYPE_MISMATCH);
  }

  // Entity constraint violation exception
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> constraintViolationException(ConstraintViolationException ex) {
    Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();

    List<ExceptionErrorField> errorFields =
        constraintViolations.stream()
            .map(
                violation ->
                    ExceptionErrorField.builder()
                        .fieldName(violation.getPropertyPath().toString())
                        .fieldError(violation.getMessage())
                        .build())
            .sorted(Comparator.comparing(ExceptionErrorField::getFieldName))
            .collect(toList());

    return getErrorMessage(CONSTRAINT_VIOLATION_MESSAGE, CONSTRAINT_VIOLATION, errorFields);
  }

  // Internal API exception
  @ExceptionHandler(InternalException.class)
  public ResponseEntity<Object> internalException(InternalException ex) {
    log.error("[INTERNAL] {}", ex.getMessage());

    return getErrorMessage(ex.getMessage(), INTERNAL_ERROR);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    BindingResult result = ex.getBindingResult();

    List<ExceptionErrorField> errorFields =
        result.getFieldErrors().stream()
            .map(
                field ->
                    ExceptionErrorField.builder()
                        .fieldName(field.getField())
                        .fieldError(field.getDefaultMessage())
                        .build())
            .sorted(Comparator.comparing(ExceptionErrorField::getFieldName))
            .collect(toList());

    return getErrorMessage(METHOD_ARGUMENT_NOT_VALID_MESSAGE, errorFields);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ExceptionErrorField errorField =
        ExceptionErrorField.builder()
            .fieldName(ex.getParameterName())
            .fieldError(ex.getMessage())
            .build();

    return getErrorMessage(ex.getMessage(), singletonList(errorField));
  }

  private ResponseEntity<Object> getErrorMessage(String errorMessage, ExceptionError error) {
    return getErrorMessage(errorMessage, error, null);
  }

  private ResponseEntity<Object> getErrorMessage(
      String errorMessage, List<ExceptionErrorField> errorFields) {
    return getErrorMessage(errorMessage, BAD_REQUEST, errorFields);
  }

  private ResponseEntity<Object> getErrorMessage(
      String errorMessage, ExceptionError error, @Nullable List<ExceptionErrorField> errorFields) {
    ExceptionErrorMessage exceptionErrorMessage =
        ExceptionErrorMessage.builder()
            .errorCode(error.getErrorCode())
            .errorMessage(errorMessage)
            .errorFields(errorFields)
            .httpStatus(error.getHttpStatus())
            .timestamp(clock.millis())
            .build();

    return ResponseEntity.status(error.getHttpStatus())
        .contentType(DEFAULT_RESPONSE_TYPE)
        .body(exceptionErrorMessage);
  }
}

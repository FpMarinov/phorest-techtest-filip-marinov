package com.phorest.exception.common.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.http.HttpStatus;

/** Represents body of API response to an exception thrown due to a request. */
@Value
@Builder
public class ExceptionErrorMessage implements ExceptionError {
  @NonNull @JsonIgnore private HttpStatus httpStatus;

  @NonNull
  @JsonProperty("error_code")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String errorCode;

  @JsonProperty("message")
  private String errorMessage;

  @JsonProperty("error_fields")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<ExceptionErrorField> errorFields;

  @JsonProperty("timestamp")
  private long timestamp;

  @JsonProperty("status")
  public int getHttpStatusCode() {
    return httpStatus.value();
  }
}

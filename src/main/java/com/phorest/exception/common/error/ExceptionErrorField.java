package com.phorest.exception.common.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/** Represents field name and error of a request field causing an error. */
@Value
@Builder
public class ExceptionErrorField {
  @JsonProperty("field_name")
  private String fieldName;

  @JsonProperty("field_error")
  private String fieldError;
}

package com.phorest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ServiceRequest {
  @NotBlank
  @JsonProperty("name")
  private String name;

  @NotNull
  @Positive
  @JsonProperty("price")
  private BigDecimal price;

  @PositiveOrZero
  @JsonProperty("loyalty_points")
  private int loyaltyPoints;
}

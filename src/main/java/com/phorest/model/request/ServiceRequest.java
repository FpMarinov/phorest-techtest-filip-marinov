package com.phorest.model.request;

import static com.phorest.model.entity.Service.SERVICE_NAME_LENGTH_LIMIT;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {
  @NotBlank
  @Size(max = SERVICE_NAME_LENGTH_LIMIT)
  @JsonProperty("name")
  private String name;

  @NotNull
  @Positive
  @JsonProperty("price")
  private BigDecimal price;

  @NotNull
  @PositiveOrZero
  @JsonProperty("loyalty_points")
  private Integer loyaltyPoints;
}

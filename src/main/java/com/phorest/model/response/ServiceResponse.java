package com.phorest.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class ServiceResponse {
  @JsonProperty("id")
  private UUID id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("price")
  private BigDecimal price;

  @JsonProperty("loyalty_points")
  private int loyaltyPoints;
}

package com.phorest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
  @NotNull
  @JsonProperty("start_time")
  private Instant startTime;

  @NotNull
  @JsonProperty("end_time")
  private Instant endTime;
}

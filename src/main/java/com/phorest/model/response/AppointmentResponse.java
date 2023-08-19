package com.phorest.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class AppointmentResponse {
  @JsonProperty("id")
  private UUID id;

  @JsonProperty("start_time")
  private Instant startTime;

  @JsonProperty("end_time")
  private Instant endTime;
}

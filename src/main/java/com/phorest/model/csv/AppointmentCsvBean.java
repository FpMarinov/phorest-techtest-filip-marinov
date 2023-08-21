package com.phorest.model.csv;

import com.phorest.model.csv.common.CsvBean;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCsvBean extends CsvBean {
  @NotNull private UUID id;

  @NotNull private Instant startTime;

  @NotNull private Instant endTime;

  @NotNull private UUID clientId;
}

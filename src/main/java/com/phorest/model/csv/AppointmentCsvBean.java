package com.phorest.model.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.phorest.model.csv.common.CsvBean;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class AppointmentCsvBean extends CsvBean {
  public static final String APPOINTMENT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss xx";

  @NotNull
  @CsvBindByName(column = "id")
  private UUID id;

  @NotNull
  @CsvDate(APPOINTMENT_DATE_TIME_PATTERN)
  @CsvBindByName(column = "start_time")
  private Instant startTime;

  @NotNull
  @CsvDate(APPOINTMENT_DATE_TIME_PATTERN)
  @CsvBindByName(column = "end_time")
  private Instant endTime;

  @NotNull
  @CsvBindByName(column = "client_id")
  private UUID clientId;
}

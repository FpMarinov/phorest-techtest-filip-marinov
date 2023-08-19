package com.phorest.model.csv;

import com.opencsv.bean.CsvBindByName;
import com.phorest.model.csv.common.CsvBean;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class PurchaseCsvBean extends CsvBean {
  @NotNull
  @CsvBindByName(column = "id")
  private UUID id;

  @NotBlank
  @CsvBindByName(column = "name")
  private String name;

  @NotNull
  @Positive
  @CsvBindByName(column = "price")
  private BigDecimal price;

  @PositiveOrZero
  @CsvBindByName(column = "loyalty_points")
  private int loyaltyPoints;

  @NotNull
  @CsvBindByName(column = "appointment_id")
  private UUID appointmentId;
}

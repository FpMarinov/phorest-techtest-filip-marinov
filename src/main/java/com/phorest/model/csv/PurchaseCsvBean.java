package com.phorest.model.csv;

import com.phorest.model.csv.common.CsvBean;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseCsvBean extends CsvBean {
  @NotNull private UUID id;

  @NotBlank private String name;

  @NotNull @Positive private BigDecimal price;

  @PositiveOrZero private int loyaltyPoints;

  @NotNull private UUID appointmentId;
}

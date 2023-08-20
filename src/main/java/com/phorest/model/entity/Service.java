package com.phorest.model.entity;

import com.phorest.model.entity.common.AuditAt;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "service")
public class Service extends AuditAt {
  public static final int SERVICE_NAME_LENGTH_LIMIT = 50;

  @Id
  @Column(name = "id")
  private UUID id = UUID.randomUUID();

  @NotBlank
  @Size(max = SERVICE_NAME_LENGTH_LIMIT)
  @Column(name = "name")
  private String name;

  @NotNull
  @Positive
  @Column(name = "price")
  private BigDecimal price;

  @PositiveOrZero
  @Column(name = "loyalty_points")
  private int loyaltyPoints;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_id")
  private Appointment appointment;
}

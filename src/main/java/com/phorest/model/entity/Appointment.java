package com.phorest.model.entity;

import com.phorest.model.entity.common.AuditAt;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "appointment")
public class Appointment extends AuditAt {
  @Id
  @Column(name = "id")
  private UUID id = UUID.randomUUID();

  @NotNull
  @Column(name = "start_time")
  private Instant startTime;

  @NotNull
  @Column(name = "end_time")
  private Instant endTime;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id")
  private Client client;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "appointment",
      cascade = CascadeType.REMOVE,
      orphanRemoval = true)
  private Set<Service> services = new HashSet<>();

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "appointment",
      cascade = CascadeType.REMOVE,
      orphanRemoval = true)
  private Set<Purchase> purchases = new HashSet<>();

  public Appointment addService(Service service) {
    services.add(service);
    service.setAppointment(this);

    return this;
  }

  public Appointment addPurchase(Purchase purchase) {
    purchases.add(purchase);
    purchase.setAppointment(this);

    return this;
  }
}

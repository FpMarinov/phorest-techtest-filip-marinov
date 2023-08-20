package com.phorest.model.entity;

import com.phorest.model.entity.common.AuditAt;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "client")
public class Client extends AuditAt {
  public static final int CLIENT_NAME_LENGTH_LIMIT = 50;
  public static final int EMAIL_LENGTH_LIMIT = 254;
  public static final int PHONE_LENGTH_LIMIT = 15;

  @Id
  @Column(name = "id")
  private UUID id = UUID.randomUUID();

  @NotBlank
  @Size(max = CLIENT_NAME_LENGTH_LIMIT)
  @Column(name = "first_name")
  private String firstName;

  @NotBlank
  @Size(max = CLIENT_NAME_LENGTH_LIMIT)
  @Column(name = "last_name")
  private String lastName;

  @Email
  @NotBlank
  @Column(name = "email")
  private String email;

  @NotBlank
  @Size(max = PHONE_LENGTH_LIMIT)
  @Column(name = "phone")
  private String phone;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private Gender gender;

  @Column(name = "banned")
  private boolean banned;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(
      fetch = FetchType.LAZY,
      mappedBy = "client",
      cascade = CascadeType.REMOVE,
      orphanRemoval = true)
  private Set<Appointment> appointments = new HashSet<>();

  public Client addAppointment(Appointment appointment) {
    appointments.add(appointment);
    appointment.setClient(this);

    return this;
  }

  public enum Gender {
    MALE,
    FEMALE
  }
}

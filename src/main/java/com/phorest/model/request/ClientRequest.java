package com.phorest.model.request;

import static com.phorest.model.entity.Client.CLIENT_NAME_LENGTH_LIMIT;
import static com.phorest.model.entity.Client.PHONE_LENGTH_LIMIT;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.phorest.model.entity.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {
  @NotBlank
  @Size(max = CLIENT_NAME_LENGTH_LIMIT)
  @JsonProperty("first_name")
  private String firstName;

  @NotBlank
  @Size(max = CLIENT_NAME_LENGTH_LIMIT)
  @JsonProperty("last_name")
  private String lastName;

  @Email
  @NotBlank
  @JsonProperty("email")
  private String email;

  @NotBlank
  @Size(max = PHONE_LENGTH_LIMIT)
  @JsonProperty("phone")
  private String phone;

  @NotNull
  @JsonProperty("gender")
  private Client.Gender gender;

  @NotNull
  @JsonProperty("banned")
  private Boolean banned;
}

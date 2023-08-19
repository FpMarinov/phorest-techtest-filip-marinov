package com.phorest.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.phorest.model.entity.Client;
import java.util.UUID;
import lombok.Data;

@Data
public class ClientResponse {
  @JsonProperty("id")
  private UUID id;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  @JsonProperty("email")
  private String email;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("gender")
  private Client.Gender gender;

  @JsonProperty("banned")
  private boolean banned;
}

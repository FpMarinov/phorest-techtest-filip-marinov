package com.phorest.model.jdbc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRow {
  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String gender;
  private boolean banned;
}

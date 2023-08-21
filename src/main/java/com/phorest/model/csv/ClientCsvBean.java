package com.phorest.model.csv;

import static com.phorest.model.entity.Client.CLIENT_NAME_LENGTH_LIMIT;
import static com.phorest.model.entity.Client.PHONE_LENGTH_LIMIT;

import com.phorest.model.csv.common.CsvBean;
import com.phorest.model.entity.Client.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCsvBean extends CsvBean {
  @NotNull private UUID id;

  @NotBlank
  @Size(max = CLIENT_NAME_LENGTH_LIMIT)
  private String firstName;

  @NotBlank
  @Size(max = CLIENT_NAME_LENGTH_LIMIT)
  private String lastName;

  @Email @NotBlank private String email;

  @NotBlank
  @Size(max = PHONE_LENGTH_LIMIT)
  private String phone;

  @NotNull private Gender gender;

  private boolean banned;
}

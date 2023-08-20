package com.phorest.model.csv;

import static com.phorest.model.entity.Client.CLIENT_NAME_LENGTH_LIMIT;
import static com.phorest.model.entity.Client.EMAIL_LENGTH_LIMIT;
import static com.phorest.model.entity.Client.PHONE_LENGTH_LIMIT;

import com.opencsv.bean.CsvBindByName;
import com.phorest.model.csv.common.CsvBean;
import com.phorest.model.entity.Client.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

@Data
public class ClientCsvBean extends CsvBean {
  @NotNull
  @CsvBindByName(column = "id")
  private UUID id;

  @NotBlank
  @Size(max = CLIENT_NAME_LENGTH_LIMIT)
  @CsvBindByName(column = "first_name")
  private String firstName;

  @NotBlank
  @Size(max = CLIENT_NAME_LENGTH_LIMIT)
  @CsvBindByName(column = "last_name")
  private String lastName;

  @Email
  @NotBlank
  @Size(max = EMAIL_LENGTH_LIMIT)
  @CsvBindByName(column = "email")
  private String email;

  @NotBlank
  @Size(max = PHONE_LENGTH_LIMIT)
  @CsvBindByName(column = "phone")
  private String phone;

  @NotNull
  @CsvBindByName(column = "gender")
  private Gender gender;

  @CsvBindByName(column = "banned")
  private boolean banned;
}

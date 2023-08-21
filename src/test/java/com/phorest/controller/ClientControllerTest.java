package com.phorest.controller;

import static com.phorest.controller.advice.ApiControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE;
import static com.phorest.exception.error.ApiError.BAD_REQUEST;
import static com.phorest.exception.error.ApiError.CLIENT_NOT_FOUND;
import static com.phorest.exception.error.ApiError.INVALID_CSV_FILE;
import static com.phorest.helper.JsonTestHelper.fromJson;
import static com.phorest.helper.JsonTestHelper.toJson;
import static com.phorest.model.entity.Client.CLIENT_NAME_LENGTH_LIMIT;
import static com.phorest.model.entity.Client.EMAIL_LENGTH_LIMIT;
import static com.phorest.model.entity.Client.PHONE_LENGTH_LIMIT;
import static com.phorest.service.CsvServiceImpl.CSV_CONTENT_TYPE;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.phorest.exception.ClientNotFoundException;
import com.phorest.exception.InvalidCsvFileException;
import com.phorest.helper.ClientTestHelper;
import com.phorest.model.entity.Client;
import com.phorest.model.entity.Client.Gender;
import com.phorest.model.request.ClientRequest;
import com.phorest.model.response.ClientResponse;
import com.phorest.repository.ClientRepository;
import com.phorest.util.CsvUtils;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Sql({"classpath:dataset/truncate.sql", "classpath:dataset/controller/client.sql"})
public class ClientControllerTest {
  private static final UUID CLIENT_ID = UUID.fromString("e0b8ebfc-6e57-4661-9546-328c644a3764");
  private static final UUID NON_EXISTENT_CLIENT_ID =
      UUID.fromString("aca44e8b-e581-45b3-849f-956a26dbeef8");

  @Autowired private ClientRepository clientRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;

  @Value("classpath:files/clients.csv")
  private Resource clientsCsvFile;

  @Value("classpath:files/appointments.csv")
  private Resource invalidCsvFile;

  @Value("classpath:files/empty.csv")
  private Resource emptyCsvFile;

  @Value("classpath:files/text.txt")
  private Resource textFile;

  @Test
  public void getClient_AsAnonymousUserWithInvalidId_ExceptionNotFound() throws Exception {
    Optional<Client> clientOptional = clientRepository.findById(NON_EXISTENT_CLIENT_ID);
    assertFalse(clientOptional.isPresent());

    mockMvc
        .perform(
            get("/clients/{clientId}", NON_EXISTENT_CLIENT_ID).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(CLIENT_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(CLIENT_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(ClientNotFoundException.MESSAGE.formatted(NON_EXISTENT_CLIENT_ID)));
  }

  @Test
  public void getClient_AsAnonymousUserWithValidId_ReturnExpectedClient() throws Exception {
    Optional<Client> clientOptional = clientRepository.findById(CLIENT_ID);
    assertTrue(clientOptional.isPresent());

    Client clientFromDatabase = clientOptional.orElseThrow();

    mockMvc
        .perform(get("/clients/{clientId}", CLIENT_ID).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(7)))
        .andExpect(jsonPath("$.id").value(CLIENT_ID.toString()))
        .andExpect(jsonPath("$.first_name").value(clientFromDatabase.getFirstName()))
        .andExpect(jsonPath("$.last_name").value(clientFromDatabase.getLastName()))
        .andExpect(jsonPath("$.email").value(clientFromDatabase.getEmail()))
        .andExpect(jsonPath("$.phone").value(clientFromDatabase.getPhone()))
        .andExpect(jsonPath("$.gender").value(clientFromDatabase.getGender().toString()))
        .andExpect(jsonPath("$.banned").value(clientFromDatabase.isBanned()));
  }

  @Test
  public void deleteClient_AsAnonymousUserWithInvalidId_ExceptionNotFound() throws Exception {
    Optional<Client> clientOptional = clientRepository.findById(NON_EXISTENT_CLIENT_ID);
    assertFalse(clientOptional.isPresent());

    mockMvc
        .perform(
            delete("/clients/{clientId}", NON_EXISTENT_CLIENT_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(CLIENT_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(CLIENT_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(ClientNotFoundException.MESSAGE.formatted(NON_EXISTENT_CLIENT_ID)));
  }

  @Test
  public void deleteClient_AsAnonymousUserWithValidId_ClientDeleted() throws Exception {
    Optional<Client> clientOptional = clientRepository.findById(CLIENT_ID);
    assertTrue(clientOptional.isPresent());

    mockMvc
        .perform(delete("/clients/{clientId}", CLIENT_ID).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    clientOptional = clientRepository.findById(CLIENT_ID);
    assertFalse(clientOptional.isPresent());
  }

  @Test
  public void updateClient_AsAnonymousUserWithEmptyBody_ExceptionBadRequest() throws Exception {
    assertTrue(clientRepository.findById(CLIENT_ID).isPresent());

    mockMvc
        .perform(
            put("/clients/{clientId}", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateClient_AsAnonymousUserWithNullClientRequest_ExceptionBadRequest()
      throws Exception {
    assertTrue(clientRepository.findById(CLIENT_ID).isPresent());

    ClientRequest clientRequest = null;

    mockMvc
        .perform(
            put("/clients/{clientId}", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, clientRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateClient_AsAnonymousUserWithEmptyStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(clientRepository.findById(CLIENT_ID).isPresent());

    ClientRequest clientRequest =
        ClientRequest.builder()
            .banned(Boolean.FALSE)
            .gender(Gender.MALE)
            .firstName("")
            .lastName("")
            .phone("")
            .email("")
            .build();

    mockMvc
        .perform(
            put("/clients/{clientId}", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, clientRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(BAD_REQUEST.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(METHOD_ARGUMENT_NOT_VALID_MESSAGE))
        .andExpect(jsonPath("$.error_fields").isArray())
        .andExpect(jsonPath("$.error_fields", hasSize(4)))
        .andExpect(jsonPath("$.error_fields[0]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[0].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("email"))
        .andExpect(jsonPath("$.error_fields[1]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[1].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[1].field_name").value("firstName"))
        .andExpect(jsonPath("$.error_fields[2]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[2].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[2].field_name").value("lastName"))
        .andExpect(jsonPath("$.error_fields[3]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[3].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[3].field_name").value("phone"));
  }

  @Test
  public void updateClient_AsAnonymousUserWithWhitespaceStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(clientRepository.findById(CLIENT_ID).isPresent());

    ClientRequest clientRequest =
        ClientRequest.builder()
            .banned(Boolean.FALSE)
            .gender(Gender.MALE)
            .firstName("     ")
            .lastName("      ")
            .phone("         ")
            .email("         ")
            .build();

    mockMvc
        .perform(
            put("/clients/{clientId}", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, clientRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(BAD_REQUEST.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(METHOD_ARGUMENT_NOT_VALID_MESSAGE))
        .andExpect(jsonPath("$.error_fields").isArray())
        .andExpect(jsonPath("$.error_fields", hasSize(5)))
        .andExpect(jsonPath("$.error_fields[0]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("email"))
        .andExpect(jsonPath("$.error_fields[1]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[1].field_name").value("email"))
        .andExpect(jsonPath("$.error_fields[2]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[2].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[2].field_name").value("firstName"))
        .andExpect(jsonPath("$.error_fields[3]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[3].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[3].field_name").value("lastName"))
        .andExpect(jsonPath("$.error_fields[4]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[4].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[4].field_name").value("phone"));
  }

  @Test
  public void updateClient_AsAnonymousUserWithTooLongStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(clientRepository.findById(CLIENT_ID).isPresent());

    ClientRequest clientRequest =
        ClientRequest.builder()
            .banned(Boolean.FALSE)
            .gender(Gender.MALE)
            .firstName("a".repeat(CLIENT_NAME_LENGTH_LIMIT + 1))
            .lastName("a".repeat(CLIENT_NAME_LENGTH_LIMIT + 1))
            .phone("a".repeat(PHONE_LENGTH_LIMIT + 1))
            .email("%s@gmail.com".formatted("a".repeat(EMAIL_LENGTH_LIMIT - 9)))
            .build();

    mockMvc
        .perform(
            put("/clients/{clientId}", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, clientRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(BAD_REQUEST.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(METHOD_ARGUMENT_NOT_VALID_MESSAGE))
        .andExpect(jsonPath("$.error_fields").isArray())
        .andExpect(jsonPath("$.error_fields", hasSize(4)))
        .andExpect(jsonPath("$.error_fields[0]", aMapWithSize(2)))
        .andExpect(
            jsonPath("$.error_fields[0].field_error").value("must be a well-formed email address"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("email"))
        .andExpect(jsonPath("$.error_fields[1]", aMapWithSize(2)))
        .andExpect(
            jsonPath("$.error_fields[1].field_error")
                .value("size must be between 0 and %s".formatted(CLIENT_NAME_LENGTH_LIMIT)))
        .andExpect(jsonPath("$.error_fields[1].field_name").value("firstName"))
        .andExpect(jsonPath("$.error_fields[2]", aMapWithSize(2)))
        .andExpect(
            jsonPath("$.error_fields[2].field_error")
                .value("size must be between 0 and %s".formatted(CLIENT_NAME_LENGTH_LIMIT)))
        .andExpect(jsonPath("$.error_fields[2].field_name").value("lastName"))
        .andExpect(jsonPath("$.error_fields[3]", aMapWithSize(2)))
        .andExpect(
            jsonPath("$.error_fields[3].field_error")
                .value("size must be between 0 and %s".formatted(PHONE_LENGTH_LIMIT)))
        .andExpect(jsonPath("$.error_fields[3].field_name").value("phone"));
  }

  @Test
  public void updateClient_AsAnonymousUserWithNullValues_ExceptionBadRequest() throws Exception {
    assertTrue(clientRepository.findById(CLIENT_ID).isPresent());

    ClientRequest clientRequest = new ClientRequest();

    mockMvc
        .perform(
            put("/clients/{clientId}", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, clientRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(BAD_REQUEST.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(METHOD_ARGUMENT_NOT_VALID_MESSAGE))
        .andExpect(jsonPath("$.error_fields").isArray())
        .andExpect(jsonPath("$.error_fields", hasSize(6)))
        .andExpect(jsonPath("$.error_fields[0]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[0].field_error").value("must not be null"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("banned"))
        .andExpect(jsonPath("$.error_fields[1]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[1].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[1].field_name").value("email"))
        .andExpect(jsonPath("$.error_fields[2]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[2].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[2].field_name").value("firstName"))
        .andExpect(jsonPath("$.error_fields[3]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[3].field_error").value("must not be null"))
        .andExpect(jsonPath("$.error_fields[3].field_name").value("gender"))
        .andExpect(jsonPath("$.error_fields[4]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[4].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[4].field_name").value("lastName"))
        .andExpect(jsonPath("$.error_fields[5]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[5].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[5].field_name").value("phone"));
  }

  @Test
  public void updateClient_AsAnonymousUserWithInvalidEmail_ExceptionBadRequest() throws Exception {
    assertTrue(clientRepository.findById(CLIENT_ID).isPresent());

    ClientRequest clientRequest =
        ClientRequest.builder()
            .banned(Boolean.FALSE)
            .gender(Gender.MALE)
            .firstName("John")
            .lastName("Smith")
            .phone("000")
            .email("invalid-email")
            .build();

    mockMvc
        .perform(
            put("/clients/{clientId}", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, clientRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(BAD_REQUEST.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(METHOD_ARGUMENT_NOT_VALID_MESSAGE))
        .andExpect(jsonPath("$.error_fields").isArray())
        .andExpect(jsonPath("$.error_fields", hasSize(1)))
        .andExpect(jsonPath("$.error_fields[0]", aMapWithSize(2)))
        .andExpect(
            jsonPath("$.error_fields[0].field_error").value("must be a well-formed email address"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("email"));
  }

  @Test
  public void updateClient_AsAnonymousUserWithValidRequestBodyAndInvalidId_ExceptionNotFound()
      throws Exception {
    Optional<Client> clientOptional = clientRepository.findById(NON_EXISTENT_CLIENT_ID);
    assertFalse(clientOptional.isPresent());

    ClientRequest clientRequest =
        ClientRequest.builder()
            .firstName("Amanda")
            .lastName("Smith")
            .email("adam.smith@gmail.com")
            .phone("000")
            .gender(Gender.FEMALE)
            .banned(true)
            .build();

    mockMvc
        .perform(
            put("/clients/{clientId}", NON_EXISTENT_CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, clientRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(CLIENT_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(CLIENT_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(ClientNotFoundException.MESSAGE.formatted(NON_EXISTENT_CLIENT_ID)));
  }

  @Test
  public void updateClient_AsAnonymousUserWithValidRequest_ClientUpdated() throws Exception {
    Optional<Client> clientOptional = clientRepository.findById(CLIENT_ID);
    assertTrue(clientOptional.isPresent());

    ClientRequest clientRequest =
        ClientRequest.builder()
            .firstName("Amanda")
            .lastName("Smith")
            .email("adam.smith@gmail.com")
            .phone("000")
            .gender(Gender.FEMALE)
            .banned(true)
            .build();

    Client client = clientOptional.orElseThrow();
    assertNotEquals(clientRequest.getFirstName(), client.getFirstName());
    assertNotEquals(clientRequest.getLastName(), client.getLastName());
    assertNotEquals(clientRequest.getEmail(), client.getEmail());
    assertNotEquals(clientRequest.getPhone(), client.getPhone());
    assertNotEquals(clientRequest.getGender(), client.getGender());
    assertNotEquals(clientRequest.getBanned(), client.isBanned());

    mockMvc
        .perform(
            put("/clients/{clientId}", CLIENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, clientRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(7)))
        .andExpect(jsonPath("$.id").value(CLIENT_ID.toString()))
        .andExpect(jsonPath("$.first_name").value(clientRequest.getFirstName()))
        .andExpect(jsonPath("$.last_name").value(clientRequest.getLastName()))
        .andExpect(jsonPath("$.email").value(clientRequest.getEmail()))
        .andExpect(jsonPath("$.phone").value(clientRequest.getPhone()))
        .andExpect(jsonPath("$.gender").value(clientRequest.getGender().toString()))
        .andExpect(jsonPath("$.banned").value(clientRequest.getBanned()));

    clientOptional = clientRepository.findById(CLIENT_ID);
    assertTrue(clientOptional.isPresent());

    client = clientOptional.orElseThrow();
    assertEquals(clientRequest.getFirstName(), client.getFirstName());
    assertEquals(clientRequest.getLastName(), client.getLastName());
    assertEquals(clientRequest.getEmail(), client.getEmail());
    assertEquals(clientRequest.getPhone(), client.getPhone());
    assertEquals(clientRequest.getGender(), client.getGender());
    assertEquals(clientRequest.getBanned(), client.isBanned());
  }

  @Test
  public void createClientsFromFile_AsAnonymousUserWithInvalidCsvFile_ExceptionConflict()
      throws Exception {
    clientRepository.deleteAll();
    assertTrue(clientRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "appointments.csv", CSV_CONTENT_TYPE, invalidCsvFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/clients/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createClientsFromFile_AsAnonymousUserWithEmptyCsvFile_ExceptionConflict()
      throws Exception {
    clientRepository.deleteAll();
    assertTrue(clientRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "empty.csv", CSV_CONTENT_TYPE, emptyCsvFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/clients/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createClientsFromFile_AsAnonymousUserWithTextFile_ExceptionConflict()
      throws Exception {
    clientRepository.deleteAll();
    assertTrue(clientRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "text.txt", MediaType.TEXT_PLAIN_VALUE, textFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/clients/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createClientsFromFile_AsAnonymousUserWithValidFile_ClientsCreated() throws Exception {
    clientRepository.deleteAll();
    assertTrue(clientRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "clients.csv", CSV_CONTENT_TYPE, clientsCsvFile.getContentAsByteArray());

    mockMvc.perform(multipart("/clients/files").file(file)).andExpect(status().isCreated());

    List<Client> clients = clientRepository.findAll();
    assertFalse(clients.isEmpty());

    try (CSVReader csvReader = CsvUtils.buildCsvReader(file.getBytes())) {
      String[] line = csvReader.readNext();
      assertEquals(7, line.length);
      assertEquals("id", line[0]);
      assertEquals("first_name", line[1]);
      assertEquals("last_name", line[2]);
      assertEquals("email", line[3]);
      assertEquals("phone", line[4]);
      assertEquals("gender", line[5]);
      assertEquals("banned", line[6]);

      for (Client client : clients) {
        line = csvReader.readNext();
        assertEquals(7, line.length);
        assertEquals(line[0], client.getId().toString());
        assertEquals(line[1], client.getFirstName());
        assertEquals(line[2], client.getLastName());
        assertEquals(line[3], client.getEmail());
        assertEquals(line[4], client.getPhone());
        assertEquals(line[5].toUpperCase(), client.getGender().name());
        assertEquals(Boolean.parseBoolean(line[6]), client.isBanned());
      }
    }
  }

  @Transactional
  @ParameterizedTest
  @ValueSource(strings = {"2016-06-01", "2016-12-31", "2017-09-01", "2018-03-01", "2018-08-01"})
  @Sql({"classpath:dataset/truncate.sql", "classpath:dataset/controller/all_data.sql"})
  public void getTopClients_AsAnonymousUser_ReturnExpectedClients(String cutoffDateString)
      throws Exception {
    int clientNumber = 50;

    Instant cutoffInstant = Instant.parse("%sT00:00:00.00Z".formatted(cutoffDateString));

    String responseString =
        mockMvc
            .perform(
                get("/clients/top")
                    .queryParam("number", String.valueOf(clientNumber))
                    .queryParam("cutoff", cutoffDateString)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<ClientResponse> clientResponses =
        fromJson(mapper, responseString, new TypeReference<>() {});

    assertTrue(clientResponses.size() <= clientNumber);

    List<Client> topClientsFromMemory =
        ClientTestHelper.getTopClientsWithFilteringAndSortingInMemory(
            clientNumber, cutoffInstant, clientRepository);

    assertEquals(topClientsFromMemory.size(), clientResponses.size());

    for (int i = 0; i < topClientsFromMemory.size(); i++) {
      Client clientFromMemory = topClientsFromMemory.get(i);
      ClientResponse clientResponse = clientResponses.get(i);

      assertFalse(clientResponse.isBanned());

      assertEquals(clientFromMemory.getId(), clientResponse.getId());
      assertEquals(clientFromMemory.getFirstName(), clientResponse.getFirstName());
      assertEquals(clientFromMemory.getLastName(), clientResponse.getLastName());
      assertEquals(clientFromMemory.getEmail(), clientResponse.getEmail());
      assertEquals(clientFromMemory.getPhone(), clientResponse.getPhone());
      assertEquals(clientFromMemory.getGender(), clientResponse.getGender());
      assertEquals(clientFromMemory.isBanned(), clientResponse.isBanned());
    }
  }
}

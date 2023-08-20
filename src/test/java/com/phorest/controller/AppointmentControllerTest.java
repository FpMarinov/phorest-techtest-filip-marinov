package com.phorest.controller;

import static com.phorest.controller.advice.ApiControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE;
import static com.phorest.controller.advice.ApiControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE;
import static com.phorest.exception.error.ApiError.APPOINTMENT_NOT_FOUND;
import static com.phorest.exception.error.ApiError.BAD_REQUEST;
import static com.phorest.exception.error.ApiError.CONSTRAINT_VIOLATION;
import static com.phorest.exception.error.ApiError.INVALID_CSV_FILE;
import static com.phorest.helper.JsonHelper.toJson;
import static com.phorest.model.csv.AppointmentCsvBean.APPOINTMENT_DATE_TIME_PATTERN;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.phorest.exception.AppointmentNotFoundException;
import com.phorest.exception.InvalidCsvFileException;
import com.phorest.model.entity.Appointment;
import com.phorest.model.request.AppointmentRequest;
import com.phorest.repository.AppointmentRepository;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql({"classpath:dataset/truncate.sql", "classpath:dataset/controller/appointment.sql"})
public class AppointmentControllerTest {
  private static final UUID APPOINTMENT_ID =
      UUID.fromString("7416ebc3-12ce-4000-87fb-82973722ebf4");
  private static final UUID NON_EXISTENT_APPOINTMENT_ID =
      UUID.fromString("aca44e8b-e581-45b3-849f-956a26dbeef8");

  private static final DateTimeFormatter APPOINTMENT_FORMATTER =
      DateTimeFormatter.ofPattern(APPOINTMENT_DATE_TIME_PATTERN).withZone(ZoneOffset.UTC);

  @Autowired private AppointmentRepository appointmentRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;

  @Value("classpath:files/appointments.csv")
  private Resource appointmentsCsvFile;

  @Value("classpath:files/clients.csv")
  private Resource invalidCsvFile;

  @Value("classpath:files/empty.csv")
  private Resource emptyCsvFile;

  @Value("classpath:files/text.txt")
  private Resource textFile;

  @Test
  public void getAppointment_AsAnonymousUserWithInvalidId_ExceptionNotFound() throws Exception {
    Optional<Appointment> appointmentOptional =
        appointmentRepository.findById(NON_EXISTENT_APPOINTMENT_ID);
    assertFalse(appointmentOptional.isPresent());

    mockMvc
        .perform(
            get("/appointments/{appointmentId}", NON_EXISTENT_APPOINTMENT_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(APPOINTMENT_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(APPOINTMENT_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(
                    AppointmentNotFoundException.MESSAGE.formatted(NON_EXISTENT_APPOINTMENT_ID)));
  }

  @Test
  public void getAppointment_AsAnonymousUserWithValidId_ReturnExpectedAppointment()
      throws Exception {
    Optional<Appointment> appointmentOptional = appointmentRepository.findById(APPOINTMENT_ID);
    assertTrue(appointmentOptional.isPresent());

    Appointment appointmentFromDatabase = appointmentOptional.orElseThrow();

    mockMvc
        .perform(
            get("/appointments/{appointmentId}", APPOINTMENT_ID).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(3)))
        .andExpect(jsonPath("$.id").value(APPOINTMENT_ID.toString()))
        .andExpect(
            jsonPath("$.start_time").value(appointmentFromDatabase.getStartTime().toString()))
        .andExpect(jsonPath("$.end_time").value(appointmentFromDatabase.getEndTime().toString()));
  }

  @Test
  public void deleteAppointment_AsAnonymousUserWithInvalidId_ExceptionNotFound() throws Exception {
    Optional<Appointment> appointmentOptional =
        appointmentRepository.findById(NON_EXISTENT_APPOINTMENT_ID);
    assertFalse(appointmentOptional.isPresent());

    mockMvc
        .perform(
            delete("/appointments/{appointmentId}", NON_EXISTENT_APPOINTMENT_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(APPOINTMENT_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(APPOINTMENT_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(
                    AppointmentNotFoundException.MESSAGE.formatted(NON_EXISTENT_APPOINTMENT_ID)));
  }

  @Test
  public void deleteAppointment_AsAnonymousUserWithValidId_AppointmentDeleted() throws Exception {
    Optional<Appointment> appointmentOptional = appointmentRepository.findById(APPOINTMENT_ID);
    assertTrue(appointmentOptional.isPresent());

    mockMvc
        .perform(
            delete("/appointments/{appointmentId}", APPOINTMENT_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    appointmentOptional = appointmentRepository.findById(APPOINTMENT_ID);
    assertFalse(appointmentOptional.isPresent());
  }

  @Test
  public void updateAppointment_AsAnonymousUserWithEmptyBody_ExceptionBadRequest()
      throws Exception {
    assertTrue(appointmentRepository.findById(APPOINTMENT_ID).isPresent());

    mockMvc
        .perform(
            put("/appointments/{appointmentId}", APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateAppointment_AsAnonymousUserWithNullAppointmentRequest_ExceptionBadRequest()
      throws Exception {
    assertTrue(appointmentRepository.findById(APPOINTMENT_ID).isPresent());

    AppointmentRequest appointmentRequest = null;

    mockMvc
        .perform(
            put("/appointments/{appointmentId}", APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, appointmentRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateAppointment_AsAnonymousUserWithNullValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(appointmentRepository.findById(APPOINTMENT_ID).isPresent());

    AppointmentRequest appointmentRequest = new AppointmentRequest();

    mockMvc
        .perform(
            put("/appointments/{appointmentId}", APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, appointmentRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(BAD_REQUEST.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(METHOD_ARGUMENT_NOT_VALID_MESSAGE))
        .andExpect(jsonPath("$.error_fields").isArray())
        .andExpect(jsonPath("$.error_fields", hasSize(2)))
        .andExpect(jsonPath("$.error_fields[0]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[0].field_error").value("must not be null"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("endTime"))
        .andExpect(jsonPath("$.error_fields[1]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[1].field_error").value("must not be null"))
        .andExpect(jsonPath("$.error_fields[1].field_name").value("startTime"));
  }

  @Test
  public void updateAppointment_AsAnonymousUserWithValidRequestBodyAndInvalidId_ExceptionNotFound()
      throws Exception {
    Optional<Appointment> appointmentOptional =
        appointmentRepository.findById(NON_EXISTENT_APPOINTMENT_ID);
    assertFalse(appointmentOptional.isPresent());

    AppointmentRequest appointmentRequest =
        AppointmentRequest.builder()
            .startTime(Instant.parse("2023-08-21T00:05:00.00Z"))
            .endTime(Instant.parse("2023-08-21T01:05:00.00Z"))
            .build();

    mockMvc
        .perform(
            put("/appointments/{appointmentId}", NON_EXISTENT_APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, appointmentRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(APPOINTMENT_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(APPOINTMENT_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(
                    AppointmentNotFoundException.MESSAGE.formatted(NON_EXISTENT_APPOINTMENT_ID)));
  }

  @Test
  public void updateAppointment_AsAnonymousUserWithValidRequest_AppointmentUpdated()
      throws Exception {
    Optional<Appointment> appointmentOptional = appointmentRepository.findById(APPOINTMENT_ID);
    assertTrue(appointmentOptional.isPresent());

    AppointmentRequest appointmentRequest =
        AppointmentRequest.builder()
            .startTime(Instant.parse("2023-08-21T00:05:00.00Z"))
            .endTime(Instant.parse("2023-08-21T01:05:00.00Z"))
            .build();

    Appointment appointment = appointmentOptional.orElseThrow();
    assertNotEquals(appointmentRequest.getStartTime(), appointment.getStartTime());
    assertNotEquals(appointmentRequest.getEndTime(), appointment.getEndTime());

    mockMvc
        .perform(
            put("/appointments/{appointmentId}", APPOINTMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, appointmentRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(3)))
        .andExpect(jsonPath("$.id").value(APPOINTMENT_ID.toString()))
        .andExpect(jsonPath("$.start_time").value(appointmentRequest.getStartTime().toString()))
        .andExpect(jsonPath("$.end_time").value(appointmentRequest.getEndTime().toString()));

    appointmentOptional = appointmentRepository.findById(APPOINTMENT_ID);
    assertTrue(appointmentOptional.isPresent());

    appointment = appointmentOptional.orElseThrow();
    assertEquals(appointmentRequest.getStartTime(), appointment.getStartTime());
    assertEquals(appointmentRequest.getEndTime(), appointment.getEndTime());
  }

  @Test
  public void createAppointmentsFromFile_AsAnonymousUserWithInvalidCsvFile_ExceptionBadRequest()
      throws Exception {
    appointmentRepository.deleteAll();
    assertTrue(appointmentRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "clients.csv", CSV_CONTENT_TYPE, invalidCsvFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/appointments/files").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(CONSTRAINT_VIOLATION.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(CONSTRAINT_VIOLATION.getErrorCode()))
        .andExpect(jsonPath("$.message").value(CONSTRAINT_VIOLATION_MESSAGE));
  }

  @Test
  public void createAppointmentsFromFile_AsAnonymousUserWithEmptyCsvFile_ExceptionConflict()
      throws Exception {
    appointmentRepository.deleteAll();
    assertTrue(appointmentRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "empty.csv", CSV_CONTENT_TYPE, emptyCsvFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/appointments/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createAppointmentsFromFile_AsAnonymousUserWithTextFile_ExceptionConflict()
      throws Exception {
    appointmentRepository.deleteAll();
    assertTrue(appointmentRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "text.txt", MediaType.TEXT_PLAIN_VALUE, textFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/appointments/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createAppointmentsFromFile_AsAnonymousUserWithValidFile_AppointmentsCreated()
      throws Exception {
    appointmentRepository.deleteAll();
    assertTrue(appointmentRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "appointments.csv",
            CSV_CONTENT_TYPE,
            appointmentsCsvFile.getContentAsByteArray());

    mockMvc.perform(multipart("/appointments/files").file(file)).andExpect(status().isCreated());

    List<Appointment> appointments = appointmentRepository.findAll();
    assertFalse(appointments.isEmpty());

    try (CSVReader csvReader =
        new CSVReader(new InputStreamReader(new ByteArrayInputStream(file.getBytes())))) {

      String[] line = csvReader.readNext();
      assertEquals(4, line.length);
      assertEquals("id", line[0]);
      assertEquals("client_id", line[1]);
      assertEquals("start_time", line[2]);
      assertEquals("end_time", line[3]);

      for (Appointment appointment : appointments) {
        line = csvReader.readNext();
        assertEquals(4, line.length);
        assertEquals(line[0], appointment.getId().toString());
        assertEquals(line[1], appointment.getClient().getId().toString());
        assertEquals(
            Instant.from(APPOINTMENT_FORMATTER.parse(line[2])), appointment.getStartTime());
        assertEquals(Instant.from(APPOINTMENT_FORMATTER.parse(line[3])), appointment.getEndTime());
      }
    }
  }
}

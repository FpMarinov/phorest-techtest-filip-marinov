package com.phorest.controller;

import static com.phorest.controller.advice.ApiControllerAdvice.CONSTRAINT_VIOLATION_MESSAGE;
import static com.phorest.controller.advice.ApiControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE;
import static com.phorest.exception.error.ApiError.BAD_REQUEST;
import static com.phorest.exception.error.ApiError.CONSTRAINT_VIOLATION;
import static com.phorest.exception.error.ApiError.INVALID_CSV_FILE;
import static com.phorest.exception.error.ApiError.SERVICE_NOT_FOUND;
import static com.phorest.helper.JsonHelper.toJson;
import static com.phorest.model.entity.Service.SERVICE_NAME_LENGTH_LIMIT;
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
import com.phorest.exception.InvalidCsvFileException;
import com.phorest.exception.ServiceNotFoundException;
import com.phorest.model.entity.Service;
import com.phorest.model.request.ServiceRequest;
import com.phorest.repository.ServiceRepository;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
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
@Sql({"classpath:dataset/truncate.sql", "classpath:dataset/controller/service.sql"})
public class ServiceControllerTest {
  private static final UUID SERVICE_ID = UUID.fromString("f1fc7009-0c44-4f89-ac98-5de9ce58095c");
  private static final UUID NON_EXISTENT_SERVICE_ID =
      UUID.fromString("aca44e8b-e581-45b3-849f-956a26dbeef8");

  @Autowired private ServiceRepository serviceRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;

  @Value("classpath:files/services.csv")
  private Resource servicesCsvFile;

  @Value("classpath:files/clients.csv")
  private Resource invalidCsvFile;

  @Value("classpath:files/empty.csv")
  private Resource emptyCsvFile;

  @Value("classpath:files/text.txt")
  private Resource textFile;

  @Test
  public void getService_AsAnonymousUserWithInvalidId_ExceptionNotFound() throws Exception {
    Optional<Service> serviceOptional = serviceRepository.findById(NON_EXISTENT_SERVICE_ID);
    assertFalse(serviceOptional.isPresent());

    mockMvc
        .perform(
            get("/services/{serviceId}", NON_EXISTENT_SERVICE_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(SERVICE_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(SERVICE_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(ServiceNotFoundException.MESSAGE.formatted(NON_EXISTENT_SERVICE_ID)));
  }

  @Test
  public void getService_AsAnonymousUserWithValidId_ReturnExpectedService() throws Exception {
    Optional<Service> serviceOptional = serviceRepository.findById(SERVICE_ID);
    assertTrue(serviceOptional.isPresent());

    Service serviceFromDatabase = serviceOptional.orElseThrow();

    mockMvc
        .perform(get("/services/{serviceId}", SERVICE_ID).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.id").value(SERVICE_ID.toString()))
        .andExpect(jsonPath("$.name").value(serviceFromDatabase.getName()))
        .andExpect(jsonPath("$.price").value(serviceFromDatabase.getPrice().doubleValue()))
        .andExpect(jsonPath("$.loyalty_points").value(serviceFromDatabase.getLoyaltyPoints()));
  }

  @Test
  public void deleteService_AsAnonymousUserWithInvalidId_ExceptionNotFound() throws Exception {
    Optional<Service> serviceOptional = serviceRepository.findById(NON_EXISTENT_SERVICE_ID);
    assertFalse(serviceOptional.isPresent());

    mockMvc
        .perform(
            delete("/services/{serviceId}", NON_EXISTENT_SERVICE_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(SERVICE_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(SERVICE_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(ServiceNotFoundException.MESSAGE.formatted(NON_EXISTENT_SERVICE_ID)));
  }

  @Test
  public void deleteService_AsAnonymousUserWithValidId_ServiceDeleted() throws Exception {
    Optional<Service> serviceOptional = serviceRepository.findById(SERVICE_ID);
    assertTrue(serviceOptional.isPresent());

    mockMvc
        .perform(delete("/services/{serviceId}", SERVICE_ID).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    serviceOptional = serviceRepository.findById(SERVICE_ID);
    assertFalse(serviceOptional.isPresent());
  }

  @Test
  public void updateService_AsAnonymousUserWithEmptyBody_ExceptionBadRequest() throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateService_AsAnonymousUserWithNullServiceRequest_ExceptionBadRequest()
      throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    ServiceRequest serviceRequest = null;

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateService_AsAnonymousUserWithEmptyStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    ServiceRequest serviceRequest =
        ServiceRequest.builder().price(BigDecimal.TEN).loyaltyPoints(10).name("").build();

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
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
        .andExpect(jsonPath("$.error_fields[0].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("name"));
  }

  @Test
  public void updateService_AsAnonymousUserWithWhitespaceStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    ServiceRequest serviceRequest =
        ServiceRequest.builder().price(BigDecimal.TEN).loyaltyPoints(10).name("         ").build();

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
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
        .andExpect(jsonPath("$.error_fields[0].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("name"));
  }

  @Test
  public void updateService_AsAnonymousUserWithTooLongStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    ServiceRequest serviceRequest =
        ServiceRequest.builder()
            .price(BigDecimal.TEN)
            .loyaltyPoints(10)
            .name("a".repeat(SERVICE_NAME_LENGTH_LIMIT + 1))
            .build();

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
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
            jsonPath("$.error_fields[0].field_error")
                .value("size must be between 0 and %s".formatted(SERVICE_NAME_LENGTH_LIMIT)))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("name"));
  }

  @Test
  public void updateService_AsAnonymousUserWithNullValues_ExceptionBadRequest() throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    ServiceRequest serviceRequest = new ServiceRequest();

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(BAD_REQUEST.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(BAD_REQUEST.getErrorCode()))
        .andExpect(jsonPath("$.message").value(METHOD_ARGUMENT_NOT_VALID_MESSAGE))
        .andExpect(jsonPath("$.error_fields").isArray())
        .andExpect(jsonPath("$.error_fields", hasSize(3)))
        .andExpect(jsonPath("$.error_fields[0]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[0].field_error").value("must not be null"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("loyaltyPoints"))
        .andExpect(jsonPath("$.error_fields[1]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[1].field_error").value("must not be blank"))
        .andExpect(jsonPath("$.error_fields[1].field_name").value("name"))
        .andExpect(jsonPath("$.error_fields[2]", aMapWithSize(2)))
        .andExpect(jsonPath("$.error_fields[2].field_error").value("must not be null"))
        .andExpect(jsonPath("$.error_fields[2].field_name").value("price"));
  }

  @Test
  public void updateService_AsAnonymousUserWithNegativePrice_ExceptionBadRequest()
      throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    ServiceRequest serviceRequest =
        ServiceRequest.builder()
            .price(BigDecimal.valueOf(-2.5))
            .loyaltyPoints(10)
            .name("name")
            .build();

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
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
        .andExpect(jsonPath("$.error_fields[0].field_error").value("must be greater than 0"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("price"));
  }

  @Test
  public void updateService_AsAnonymousUserWithZeroPrice_ExceptionBadRequest() throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    ServiceRequest serviceRequest =
        ServiceRequest.builder()
            .price(BigDecimal.valueOf(0.0))
            .loyaltyPoints(10)
            .name("name")
            .build();

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
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
        .andExpect(jsonPath("$.error_fields[0].field_error").value("must be greater than 0"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("price"));
  }

  @Test
  public void updateService_AsAnonymousUserWithNegativeLoyaltyPoints_ExceptionBadRequest()
      throws Exception {
    assertTrue(serviceRepository.findById(SERVICE_ID).isPresent());

    ServiceRequest serviceRequest =
        ServiceRequest.builder().price(BigDecimal.TEN).loyaltyPoints(-10).name("name").build();

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
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
            jsonPath("$.error_fields[0].field_error").value("must be greater than or equal to 0"))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("loyaltyPoints"));
  }

  @Test
  public void updateService_AsAnonymousUserWithValidRequestBodyAndInvalidId_ExceptionNotFound()
      throws Exception {
    Optional<Service> serviceOptional = serviceRepository.findById(NON_EXISTENT_SERVICE_ID);
    assertFalse(serviceOptional.isPresent());

    ServiceRequest serviceRequest =
        ServiceRequest.builder().name("Hair Gel").price(BigDecimal.TEN).loyaltyPoints(10).build();

    mockMvc
        .perform(
            put("/services/{serviceId}", NON_EXISTENT_SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(SERVICE_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(SERVICE_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(ServiceNotFoundException.MESSAGE.formatted(NON_EXISTENT_SERVICE_ID)));
  }

  @Test
  public void updateService_AsAnonymousUserWithValidRequest_ServiceUpdated() throws Exception {
    Optional<Service> serviceOptional = serviceRepository.findById(SERVICE_ID);
    assertTrue(serviceOptional.isPresent());

    ServiceRequest serviceRequest =
        ServiceRequest.builder().name("Hair Gel").price(BigDecimal.TEN).loyaltyPoints(10).build();

    Service service = serviceOptional.orElseThrow();
    assertNotEquals(serviceRequest.getName(), service.getName());
    assertNotEquals(serviceRequest.getLoyaltyPoints(), service.getLoyaltyPoints());
    assertNotEquals(serviceRequest.getPrice().doubleValue(), service.getPrice().doubleValue());

    mockMvc
        .perform(
            put("/services/{serviceId}", SERVICE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, serviceRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.id").value(SERVICE_ID.toString()))
        .andExpect(jsonPath("$.name").value(serviceRequest.getName()))
        .andExpect(jsonPath("$.price").value(serviceRequest.getPrice()))
        .andExpect(jsonPath("$.loyalty_points").value(serviceRequest.getLoyaltyPoints()));

    serviceOptional = serviceRepository.findById(SERVICE_ID);
    assertTrue(serviceOptional.isPresent());

    service = serviceOptional.orElseThrow();
    assertEquals(serviceRequest.getName(), service.getName());
    assertEquals(serviceRequest.getLoyaltyPoints(), service.getLoyaltyPoints());
    assertEquals(serviceRequest.getPrice().doubleValue(), service.getPrice().doubleValue());
  }

  @Test
  public void createServicesFromFile_AsAnonymousUserWithInvalidCsvFile_ExceptionBadRequest()
      throws Exception {
    serviceRepository.deleteAll();
    assertTrue(serviceRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "clients.csv", CSV_CONTENT_TYPE, invalidCsvFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/services/files").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(5)))
        .andExpect(jsonPath("$.status").value(CONSTRAINT_VIOLATION.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(CONSTRAINT_VIOLATION.getErrorCode()))
        .andExpect(jsonPath("$.message").value(CONSTRAINT_VIOLATION_MESSAGE));
  }

  @Test
  public void createServicesFromFile_AsAnonymousUserWithEmptyCsvFile_ExceptionConflict()
      throws Exception {
    serviceRepository.deleteAll();
    assertTrue(serviceRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "empty.csv", CSV_CONTENT_TYPE, emptyCsvFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/services/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createServicesFromFile_AsAnonymousUserWithTextFile_ExceptionConflict()
      throws Exception {
    serviceRepository.deleteAll();
    assertTrue(serviceRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "text.txt", MediaType.TEXT_PLAIN_VALUE, textFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/services/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createServicesFromFile_AsAnonymousUserWithValidFile_ServicesCreated()
      throws Exception {
    serviceRepository.deleteAll();
    assertTrue(serviceRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "services.csv", CSV_CONTENT_TYPE, servicesCsvFile.getContentAsByteArray());

    mockMvc.perform(multipart("/services/files").file(file)).andExpect(status().isCreated());

    List<Service> services = serviceRepository.findAll();
    assertFalse(services.isEmpty());

    try (CSVReader csvReader =
        new CSVReader(new InputStreamReader(new ByteArrayInputStream(file.getBytes())))) {

      String[] line = csvReader.readNext();
      assertEquals(5, line.length);
      assertEquals("id", line[0]);
      assertEquals("appointment_id", line[1]);
      assertEquals("name", line[2]);
      assertEquals("price", line[3]);
      assertEquals("loyalty_points", line[4]);

      for (Service service : services) {
        line = csvReader.readNext();
        assertEquals(5, line.length);
        assertEquals(line[0], service.getId().toString());
        assertEquals(line[1], service.getAppointment().getId().toString());
        assertEquals(line[2], service.getName());
        assertEquals(Double.parseDouble(line[3]), service.getPrice().doubleValue());
        assertEquals(Integer.parseInt(line[4]), service.getLoyaltyPoints());
      }
    }
  }
}

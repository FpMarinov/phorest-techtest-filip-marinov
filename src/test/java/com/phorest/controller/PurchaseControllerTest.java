package com.phorest.controller;

import static com.phorest.controller.advice.ApiControllerAdvice.METHOD_ARGUMENT_NOT_VALID_MESSAGE;
import static com.phorest.exception.error.ApiError.BAD_REQUEST;
import static com.phorest.exception.error.ApiError.INVALID_CSV_FILE;
import static com.phorest.exception.error.ApiError.PURCHASE_NOT_FOUND;
import static com.phorest.helper.JsonTestHelper.toJson;
import static com.phorest.model.entity.Purchase.PURCHASE_NAME_LENGTH_LIMIT;
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
import com.phorest.exception.PurchaseNotFoundException;
import com.phorest.model.entity.Purchase;
import com.phorest.model.request.PurchaseRequest;
import com.phorest.repository.PurchaseRepository;
import com.phorest.util.CsvUtils;
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
@Sql({"classpath:dataset/truncate.sql", "classpath:dataset/controller/purchase.sql"})
public class PurchaseControllerTest {
  private static final UUID PURCHASE_ID = UUID.fromString("d2d3b92d-f9b5-48c5-bf31-88c28e3b73ac");
  private static final UUID NON_EXISTENT_PURCHASE_ID =
      UUID.fromString("aca44e8b-e581-45b3-849f-956a26dbeef8");

  @Autowired private PurchaseRepository purchaseRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;

  @Value("classpath:files/purchases.csv")
  private Resource purchasesCsvFile;

  @Value("classpath:files/clients.csv")
  private Resource invalidCsvFile;

  @Value("classpath:files/empty.csv")
  private Resource emptyCsvFile;

  @Value("classpath:files/text.txt")
  private Resource textFile;

  @Test
  public void getPurchase_AsAnonymousUserWithInvalidId_ExceptionNotFound() throws Exception {
    Optional<Purchase> purchaseOptional = purchaseRepository.findById(NON_EXISTENT_PURCHASE_ID);
    assertFalse(purchaseOptional.isPresent());

    mockMvc
        .perform(
            get("/purchases/{purchaseId}", NON_EXISTENT_PURCHASE_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(PURCHASE_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(PURCHASE_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(PurchaseNotFoundException.MESSAGE.formatted(NON_EXISTENT_PURCHASE_ID)));
  }

  @Test
  public void getPurchase_AsAnonymousUserWithValidId_ReturnExpectedPurchase() throws Exception {
    Optional<Purchase> purchaseOptional = purchaseRepository.findById(PURCHASE_ID);
    assertTrue(purchaseOptional.isPresent());

    Purchase purchaseFromDatabase = purchaseOptional.orElseThrow();

    mockMvc
        .perform(get("/purchases/{purchaseId}", PURCHASE_ID).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.id").value(PURCHASE_ID.toString()))
        .andExpect(jsonPath("$.name").value(purchaseFromDatabase.getName()))
        .andExpect(jsonPath("$.price").value(purchaseFromDatabase.getPrice().doubleValue()))
        .andExpect(jsonPath("$.loyalty_points").value(purchaseFromDatabase.getLoyaltyPoints()));
  }

  @Test
  public void deletePurchase_AsAnonymousUserWithInvalidId_ExceptionNotFound() throws Exception {
    Optional<Purchase> purchaseOptional = purchaseRepository.findById(NON_EXISTENT_PURCHASE_ID);
    assertFalse(purchaseOptional.isPresent());

    mockMvc
        .perform(
            delete("/purchases/{purchaseId}", NON_EXISTENT_PURCHASE_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(PURCHASE_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(PURCHASE_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(PurchaseNotFoundException.MESSAGE.formatted(NON_EXISTENT_PURCHASE_ID)));
  }

  @Test
  public void deletePurchase_AsAnonymousUserWithValidId_PurchaseDeleted() throws Exception {
    Optional<Purchase> purchaseOptional = purchaseRepository.findById(PURCHASE_ID);
    assertTrue(purchaseOptional.isPresent());

    mockMvc
        .perform(delete("/purchases/{purchaseId}", PURCHASE_ID).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    purchaseOptional = purchaseRepository.findById(PURCHASE_ID);
    assertFalse(purchaseOptional.isPresent());
  }

  @Test
  public void updatePurchase_AsAnonymousUserWithEmptyBody_ExceptionBadRequest() throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updatePurchase_AsAnonymousUserWithNullPurchaseRequest_ExceptionBadRequest()
      throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    PurchaseRequest purchaseRequest = null;

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updatePurchase_AsAnonymousUserWithEmptyStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    PurchaseRequest purchaseRequest =
        PurchaseRequest.builder().price(BigDecimal.TEN).loyaltyPoints(10).name("").build();

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
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
  public void updatePurchase_AsAnonymousUserWithWhitespaceStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    PurchaseRequest purchaseRequest =
        PurchaseRequest.builder().price(BigDecimal.TEN).loyaltyPoints(10).name("         ").build();

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
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
  public void updatePurchase_AsAnonymousUserWithTooLongStringValues_ExceptionBadRequest()
      throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    PurchaseRequest purchaseRequest =
        PurchaseRequest.builder()
            .price(BigDecimal.TEN)
            .loyaltyPoints(10)
            .name("a".repeat(PURCHASE_NAME_LENGTH_LIMIT + 1))
            .build();

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
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
                .value("size must be between 0 and %s".formatted(PURCHASE_NAME_LENGTH_LIMIT)))
        .andExpect(jsonPath("$.error_fields[0].field_name").value("name"));
  }

  @Test
  public void updatePurchase_AsAnonymousUserWithNullValues_ExceptionBadRequest() throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    PurchaseRequest purchaseRequest = new PurchaseRequest();

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
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
  public void updatePurchase_AsAnonymousUserWithNegativePrice_ExceptionBadRequest()
      throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    PurchaseRequest purchaseRequest =
        PurchaseRequest.builder()
            .price(BigDecimal.valueOf(-2.5))
            .loyaltyPoints(10)
            .name("name")
            .build();

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
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
  public void updatePurchase_AsAnonymousUserWithZeroPrice_ExceptionBadRequest() throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    PurchaseRequest purchaseRequest =
        PurchaseRequest.builder()
            .price(BigDecimal.valueOf(0.0))
            .loyaltyPoints(10)
            .name("name")
            .build();

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
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
  public void updatePurchase_AsAnonymousUserWithNegativeLoyaltyPoints_ExceptionBadRequest()
      throws Exception {
    assertTrue(purchaseRepository.findById(PURCHASE_ID).isPresent());

    PurchaseRequest purchaseRequest =
        PurchaseRequest.builder().price(BigDecimal.TEN).loyaltyPoints(-10).name("name").build();

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
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
  public void updatePurchase_AsAnonymousUserWithValidRequestBodyAndInvalidId_ExceptionNotFound()
      throws Exception {
    Optional<Purchase> purchaseOptional = purchaseRepository.findById(NON_EXISTENT_PURCHASE_ID);
    assertFalse(purchaseOptional.isPresent());

    PurchaseRequest purchaseRequest =
        PurchaseRequest.builder().name("Hair Gel").price(BigDecimal.TEN).loyaltyPoints(10).build();

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", NON_EXISTENT_PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(PURCHASE_NOT_FOUND.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(PURCHASE_NOT_FOUND.getErrorCode()))
        .andExpect(
            jsonPath("$.message")
                .value(PurchaseNotFoundException.MESSAGE.formatted(NON_EXISTENT_PURCHASE_ID)));
  }

  @Test
  public void updatePurchase_AsAnonymousUserWithValidRequest_PurchaseUpdated() throws Exception {
    Optional<Purchase> purchaseOptional = purchaseRepository.findById(PURCHASE_ID);
    assertTrue(purchaseOptional.isPresent());

    PurchaseRequest purchaseRequest =
        PurchaseRequest.builder().name("Hair Gel").price(BigDecimal.TEN).loyaltyPoints(10).build();

    Purchase purchase = purchaseOptional.orElseThrow();
    assertNotEquals(purchaseRequest.getName(), purchase.getName());
    assertNotEquals(purchaseRequest.getLoyaltyPoints(), purchase.getLoyaltyPoints());
    assertNotEquals(purchaseRequest.getPrice().doubleValue(), purchase.getPrice().doubleValue());

    mockMvc
        .perform(
            put("/purchases/{purchaseId}", PURCHASE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mapper, purchaseRequest))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.id").value(PURCHASE_ID.toString()))
        .andExpect(jsonPath("$.name").value(purchaseRequest.getName()))
        .andExpect(jsonPath("$.price").value(purchaseRequest.getPrice()))
        .andExpect(jsonPath("$.loyalty_points").value(purchaseRequest.getLoyaltyPoints()));

    purchaseOptional = purchaseRepository.findById(PURCHASE_ID);
    assertTrue(purchaseOptional.isPresent());

    purchase = purchaseOptional.orElseThrow();
    assertEquals(purchaseRequest.getName(), purchase.getName());
    assertEquals(purchaseRequest.getLoyaltyPoints(), purchase.getLoyaltyPoints());
    assertEquals(purchaseRequest.getPrice().doubleValue(), purchase.getPrice().doubleValue());
  }

  @Test
  public void createPurchasesFromFile_AsAnonymousUserWithInvalidCsvFile_ExceptionConflict()
      throws Exception {
    purchaseRepository.deleteAll();
    assertTrue(purchaseRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "clients.csv", CSV_CONTENT_TYPE, invalidCsvFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/purchases/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createPurchasesFromFile_AsAnonymousUserWithEmptyCsvFile_ExceptionConflict()
      throws Exception {
    purchaseRepository.deleteAll();
    assertTrue(purchaseRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "empty.csv", CSV_CONTENT_TYPE, emptyCsvFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/purchases/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createPurchasesFromFile_AsAnonymousUserWithTextFile_ExceptionConflict()
      throws Exception {
    purchaseRepository.deleteAll();
    assertTrue(purchaseRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "text.txt", MediaType.TEXT_PLAIN_VALUE, textFile.getContentAsByteArray());

    mockMvc
        .perform(multipart("/purchases/files").file(file))
        .andExpect(status().isConflict())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$").isMap())
        .andExpect(jsonPath("$", aMapWithSize(4)))
        .andExpect(jsonPath("$.status").value(INVALID_CSV_FILE.getHttpStatus().value()))
        .andExpect(jsonPath("$.error_code").value(INVALID_CSV_FILE.getErrorCode()))
        .andExpect(jsonPath("$.message").value(InvalidCsvFileException.MESSAGE));
  }

  @Test
  public void createPurchasesFromFile_AsAnonymousUserWithValidFile_PurchasesCreated()
      throws Exception {
    purchaseRepository.deleteAll();
    assertTrue(purchaseRepository.findAll().isEmpty());

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "purchases.csv", CSV_CONTENT_TYPE, purchasesCsvFile.getContentAsByteArray());

    mockMvc.perform(multipart("/purchases/files").file(file)).andExpect(status().isCreated());

    List<Purchase> purchases = purchaseRepository.findAll();
    assertFalse(purchases.isEmpty());

    try (CSVReader csvReader = CsvUtils.buildCsvReader(file.getBytes())) {
      String[] line = csvReader.readNext();
      assertEquals(5, line.length);
      assertEquals("id", line[0]);
      assertEquals("appointment_id", line[1]);
      assertEquals("name", line[2]);
      assertEquals("price", line[3]);
      assertEquals("loyalty_points", line[4]);

      for (Purchase purchase : purchases) {
        line = csvReader.readNext();
        assertEquals(5, line.length);
        assertEquals(line[0], purchase.getId().toString());
        assertEquals(line[1], purchase.getAppointment().getId().toString());
        assertEquals(line[2], purchase.getName());
        assertEquals(Double.parseDouble(line[3]), purchase.getPrice().doubleValue());
        assertEquals(Integer.parseInt(line[4]), purchase.getLoyaltyPoints());
      }
    }
  }
}

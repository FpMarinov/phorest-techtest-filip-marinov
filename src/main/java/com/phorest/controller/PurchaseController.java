package com.phorest.controller;

import com.phorest.model.request.PurchaseRequest;
import com.phorest.model.response.PurchaseResponse;
import com.phorest.service.PurchaseService;
import com.phorest.util.PrincipalUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Purchase Operations")
public class PurchaseController {
  private final PurchaseService purchaseService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(
      path = "/purchases/files",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create new Purchases by uploading a csv file")
  public void createPurchasesFromFile(
      @RequestParam("file") MultipartFile file, Principal principal) {
    log.info(
        "[PURCHASES] Request from {} to create new purchases from csv file",
        PrincipalUtils.getPrincipalName(principal));

    purchaseService.createPurchasesFromFile(file);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping(
      path = "/purchases/{purchaseId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update Purchase")
  public PurchaseResponse updatePurchase(
      @PathVariable UUID purchaseId,
      @Valid @RequestBody PurchaseRequest purchaseRequest,
      Principal principal) {
    log.info(
        "[PURCHASES] Request from {} to update purchase with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        purchaseId);

    return purchaseService.updatePurchase(purchaseId, purchaseRequest);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(path = "/purchases/{purchaseId}")
  @Operation(summary = "Delete Purchase")
  public void deletePurchase(@PathVariable UUID purchaseId, Principal principal) {
    log.info(
        "[PURCHASES] Request from {} to delete purchase with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        purchaseId);

    purchaseService.deletePurchase(purchaseId);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(path = "/purchases/{purchaseId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get Purchase")
  public PurchaseResponse getPurchase(@PathVariable UUID purchaseId, Principal principal) {
    log.info(
        "[PURCHASES] Request from {} to get purchase with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        purchaseId);

    return purchaseService.getPurchaseResponse(purchaseId);
  }
}

package com.phorest.controller;

import com.phorest.model.request.ServiceRequest;
import com.phorest.model.response.ServiceResponse;
import com.phorest.service.ServiceService;
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
@Tag(name = "Service Operations")
public class ServiceController {
  private final ServiceService serviceService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(
      path = "/services/files",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create new Services by uploading a csv file")
  public void createServicesFromFile(
      @RequestParam("file") MultipartFile file, Principal principal) {
    log.info(
        "[SERVICES] Request from {} to create new services from csv file",
        PrincipalUtils.getPrincipalName(principal));

    serviceService.createServicesFromFile(file);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping(
      path = "/services/{serviceId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update Service")
  public ServiceResponse updateService(
      @PathVariable UUID serviceId,
      @Valid @RequestBody ServiceRequest serviceRequest,
      Principal principal) {
    log.info(
        "[SERVICES] Request from {} to update service with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        serviceId);

    return serviceService.updateService(serviceId, serviceRequest);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(path = "/services/{serviceId}")
  @Operation(summary = "Delete Service")
  public void deleteService(@PathVariable UUID serviceId, Principal principal) {
    log.info(
        "[SERVICES] Request from {} to delete service with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        serviceId);

    serviceService.deleteService(serviceId);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(path = "/services/{serviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get Service")
  public ServiceResponse getService(@PathVariable UUID serviceId, Principal principal) {
    log.info(
        "[SERVICES] Request from {} to get service with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        serviceId);

    return serviceService.getServiceResponse(serviceId);
  }
}

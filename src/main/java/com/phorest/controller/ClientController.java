package com.phorest.controller;

import com.phorest.model.request.ClientRequest;
import com.phorest.model.response.ClientResponse;
import com.phorest.service.ClientService;
import com.phorest.util.PrincipalUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
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
@Tag(name = "Client Operations")
public class ClientController {
  private final ClientService clientService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(
      path = "/clients/files",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create new Clients by uploading a csv file")
  public void createClientsFromFile(@RequestParam("file") MultipartFile file, Principal principal) {
    log.info(
        "[CLIENTS] Request from {} to create new clients from csv file",
        PrincipalUtils.getPrincipalName(principal));

    clientService.createClientsFromFile(file);
  }

  @ResponseStatus(HttpStatus.OK)
  @PutMapping(
      path = "/clients/{clientId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update Client")
  public ClientResponse updateClient(
      @PathVariable UUID clientId,
      @Valid @RequestBody ClientRequest clientRequest,
      Principal principal) {
    log.info(
        "[CLIENTS] Request from {} to update client with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        clientId);

    return clientService.updateClient(clientId, clientRequest);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(path = "/clients/{clientId}")
  @Operation(summary = "Delete Client")
  public void deleteClient(@PathVariable UUID clientId, Principal principal) {
    log.info(
        "[CLIENTS] Request from {} to delete client with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        clientId);

    clientService.deleteClient(clientId);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(path = "/clients/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get Client")
  public ClientResponse getClient(@PathVariable UUID clientId, Principal principal) {
    log.info(
        "[CLIENTS] Request from {} to get client with id: {}",
        PrincipalUtils.getPrincipalName(principal),
        clientId);

    return clientService.getClientResponse(clientId);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(path = "/clients/top", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get top Clients")
  public List<ClientResponse> getTopClients(
      @RequestParam(value = "number") int numberOfClients,
      @RequestParam(value = "cutoff") LocalDate cutoffDate,
      Principal principal) {

    log.info(
        "[CLIENTS] Request from {} to get top {} clients with cutoffDate: {}",
        PrincipalUtils.getPrincipalName(principal),
        numberOfClients,
        cutoffDate);

    return clientService.getTopClientsByLoyaltyPoints(numberOfClients, cutoffDate);
  }
}

package com.phorest.service;

import com.phorest.model.request.ClientRequest;
import com.phorest.model.response.ClientResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ClientService {
  void createClientsFromFile(MultipartFile file);

  ClientResponse updateClient(UUID clientId, ClientRequest clientRequest);

  void deleteClient(UUID clientId);

  List<ClientResponse> getTopClientsByLoyaltyPoints(int numberOfClients, LocalDate cutoffDate);

  ClientResponse getClientResponse(UUID clientId);
}

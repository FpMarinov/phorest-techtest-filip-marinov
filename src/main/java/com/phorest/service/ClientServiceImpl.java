package com.phorest.service;

import com.phorest.exception.ClientNotFoundException;
import com.phorest.model.csv.ClientCsvBean;
import com.phorest.model.entity.Client;
import com.phorest.model.request.ClientRequest;
import com.phorest.model.response.ClientResponse;
import com.phorest.repository.ClientRepository;
import com.phorest.validator.CsvBeanValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
  private final CsvService csvService;

  private final ClientRepository clientRepository;
  private final CsvBeanValidator csvBeanValidator;

  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public void createClientsFromFile(@NonNull MultipartFile file) {
    List<Client> clients =
        csvService.getElementsFromCsvFile(file, ClientCsvBean.class).stream()
            .peek(csvBeanValidator::validateCsvBean)
            .map(csvBean -> modelMapper.map(csvBean, Client.class))
            .toList();

    clientRepository.saveAll(clients);
  }

  @Override
  @Transactional
  public ClientResponse updateClient(@NonNull UUID clientId, @NonNull ClientRequest clientRequest) {
    Client client = getClient(clientId);

    modelMapper.map(clientRequest, client);

    return modelMapper.map(client, ClientResponse.class);
  }

  @Override
  @Transactional
  public void deleteClient(@NonNull UUID clientId) {
    Client client = getClient(clientId);

    clientRepository.delete(client);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClientResponse> getTopClientsByLoyaltyPoints(
      int numberOfClients, LocalDate cutoffDate) {

    return clientRepository
        .findTopNonBannedClientsWithMostLoyaltyPointsSinceCutoffDate(
            numberOfClients, cutoffDate.toString())
        .stream()
        .map(client -> modelMapper.map(client, ClientResponse.class))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ClientResponse getClientResponse(@NonNull UUID clientId) {
    Client client = getClient(clientId);

    return modelMapper.map(client, ClientResponse.class);
  }

  private Client getClient(@NonNull UUID clientId) {
    return clientRepository
        .findById(clientId)
        .orElseThrow(() -> new ClientNotFoundException(clientId));
  }
}

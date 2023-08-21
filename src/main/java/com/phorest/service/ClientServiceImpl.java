package com.phorest.service;

import com.phorest.exception.ClientNotFoundException;
import com.phorest.model.csv.ClientCsvBean;
import com.phorest.model.entity.Client;
import com.phorest.model.request.ClientRequest;
import com.phorest.model.response.ClientResponse;
import com.phorest.repository.ClientDAO;
import com.phorest.repository.ClientRepository;
import com.phorest.validator.CsvBeanValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
  private static final int CLIENT_FILE_PAGE_SIZE = 200;

  private final CsvService csvService;

  private final ClientRepository clientRepository;
  private final ClientDAO clientDAO;
  private final CsvBeanValidator csvBeanValidator;

  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public void createClientsFromFile(@NonNull MultipartFile file) {
    int currentPageNumber = 0;
    Page<ClientCsvBean> currentClientPage;
    boolean isCurrentPageLast;

    do {
      currentClientPage =
          csvService.getElementsFromCsvFile(
              file, ClientCsvBean.class, currentPageNumber, CLIENT_FILE_PAGE_SIZE);

      createClientsInPage(currentClientPage);

      isCurrentPageLast = currentClientPage.isLast();

      currentPageNumber++;
    } while (!isCurrentPageLast);
  }

  private void createClientsInPage(Page<ClientCsvBean> clientPage) {
    List<Client> clients =
        clientPage.getContent().stream()
            .peek(csvBeanValidator::validateCsvBean)
            .map(csvBean -> modelMapper.map(csvBean, Client.class))
            .toList();

    clientRepository.saveAllAndFlush(clients);
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

    return clientDAO
        .findTopNonBannedClientsWithMostLoyaltyPointsSinceCutoffDate(cutoffDate, numberOfClients)
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

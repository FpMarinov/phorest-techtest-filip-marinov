package com.phorest.helper;

import com.phorest.model.entity.Client;
import com.phorest.model.entity.Purchase;
import com.phorest.model.entity.Service;
import com.phorest.repository.ClientRepository;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientTestHelper {
  public static List<Client> getTopClientsWithFilteringAndSortingInMemory(
      int clientNumber, Instant cutoffInstant, ClientRepository clientRepository) {

    List<Client> nonBannedClients = clientRepository.findByBannedFalse();

    nonBannedClients.sort((c1, c2) -> getClientLoyaltyPointComparison(c1, c2, cutoffInstant));

    return nonBannedClients.subList(0, clientNumber);
  }

  // compare by loyalty points since cutoff, secondarily compare by firstName (for matching loyalty
  // points)
  private static int getClientLoyaltyPointComparison(Client c1, Client c2, Instant cutoffInstant) {
    int loyaltyPointComparison =
        getClientLoyaltyPointsSinceCutoff(c2, cutoffInstant)
            - getClientLoyaltyPointsSinceCutoff(c1, cutoffInstant);

    if (loyaltyPointComparison != 0) {
      return loyaltyPointComparison;
    } else {
      return c2.getFirstName().compareTo(c1.getFirstName());
    }
  }

  private static int getClientLoyaltyPointsSinceCutoff(Client client, Instant cutoffInstant) {
    int purchaseLoyaltyPoints =
        client.getAppointments().stream()
            .filter(appointment -> appointment.getStartTime().isAfter(cutoffInstant))
            .flatMap(appointment -> appointment.getPurchases().stream())
            .mapToInt(Purchase::getLoyaltyPoints)
            .sum();

    int serviceLoyaltyPoints =
        client.getAppointments().stream()
            .filter(appointment -> appointment.getStartTime().isAfter(cutoffInstant))
            .flatMap(appointment -> appointment.getServices().stream())
            .mapToInt(Service::getLoyaltyPoints)
            .sum();

    return purchaseLoyaltyPoints + serviceLoyaltyPoints;
  }
}

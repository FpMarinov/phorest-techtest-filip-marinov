package com.phorest.repository;

import com.phorest.model.entity.Client;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, UUID> {
  String TOP_NON_BANNED_CLIENTS_WITH_MOST_LOYALTY_POINTS_SINCE_CUTOFF_DATE_QUERY =
      """
      WITH appointment_loyalty_points_cte AS (
      	SELECT a.id AS appointment_id, a.client_id,
      	SUM(p.loyalty_points) + SUM(s.loyalty_points) AS appointment_loyalty_points
      	FROM appointment a
      	LEFT JOIN purchase p ON a.id = p.appointment_id
      	LEFT JOIN service s ON a.id = s.appointment_id
      	WHERE a.start_time >= TO_DATE(:cutoffDate,'YYYY-MM-DD')
      	GROUP BY a.id
      ), client_loyalty_points_cte AS (
      	SELECT client_id, SUM(appointment_loyalty_points) AS client_loyalty_points
      	FROM appointment_loyalty_points_cte
      	GROUP BY client_id
      ), client_cte AS (
        SELECT c.*, clp_cte.client_loyalty_points
        FROM client_loyalty_points_cte clp_cte LEFT JOIN client c ON clp_cte.client_id = c.id
        WHERE c.banned = FALSE
        ORDER BY client_loyalty_points DESC
        LIMIT :clientNumber
      )
      SELECT id, first_name, last_name, email, phone, gender, banned, created_at, updated_at
      FROM client_cte;""";

  @Query(
      value = TOP_NON_BANNED_CLIENTS_WITH_MOST_LOYALTY_POINTS_SINCE_CUTOFF_DATE_QUERY,
      nativeQuery = true)
  List<Client> findTopNonBannedClientsWithMostLoyaltyPointsSinceCutoffDate(
      @Param("clientNumber") int numberOfClients, @Param("cutoffDate") String cutoffDate);
}

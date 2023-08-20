package com.phorest.repository;

import com.phorest.model.jdbc.ClientRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClientDAO {
  private final NamedParameterJdbcTemplate jdbcTemplate;

  private final BeanPropertyRowMapper<ClientRow> rowMapper =
      BeanPropertyRowMapper.newInstance(ClientRow.class);

  public List<ClientRow> findTopNonBannedClientsWithMostLoyaltyPointsSinceCutoffDate(
      String cutoffDate, int numberOfClients) {

    MapSqlParameterSource parameters = new MapSqlParameterSource();

    parameters.addValue("cutoff", cutoffDate);
    parameters.addValue("number", numberOfClients);

    String query =
        """
          WITH purchase_union_service_cte AS (
              SELECT a.client_id, p.*
              FROM appointment a
              JOIN purchase p ON a.id = p.appointment_id
              WHERE a.start_time >= TO_DATE(:cutoff,'YYYY-MM-DD')
          		UNION ALL
          		SELECT a.client_id, s.*
              FROM appointment a
              JOIN service s ON a.id = s.appointment_id
              WHERE a.start_time >= TO_DATE(:cutoff,'YYYY-MM-DD')
          ), appointment_loyalty_points_cte AS (
              SELECT appointment_id, STRING_AGG(distinct client_id::text, '')::uuid AS client_id, SUM(loyalty_points) AS appointment_loyalty_points
          	  FROM purchase_union_service_cte
          	  GROUP BY appointment_id
          ), client_loyalty_points_cte AS (
              SELECT client_id, SUM(appointment_loyalty_points) AS client_loyalty_points
              FROM appointment_loyalty_points_cte
              GROUP BY client_id
          ), client_cte AS (
              SELECT c.*, clp_cte.client_loyalty_points
              FROM client_loyalty_points_cte clp_cte LEFT JOIN client c ON clp_cte.client_id = c.id
              WHERE c.banned = FALSE
              ORDER BY client_loyalty_points DESC, first_name DESC
              LIMIT :number
          )
          SELECT id, first_name, last_name, email, phone, gender, banned
          FROM client_cte;""";

    return jdbcTemplate.query(query, parameters, rowMapper);
  }
}

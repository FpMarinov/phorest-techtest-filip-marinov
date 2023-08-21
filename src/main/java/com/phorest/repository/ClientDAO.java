package com.phorest.repository;

import com.phorest.model.jdbc.ClientRow;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClientDAO {
  private static final String
      TOP_NON_BANNED_CLIENTS_WITH_MOST_LOYALTY_POINTS_SINCE_CUTOFF_DATE_QUERY =
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
          SELECT appointment_id,
                 STRING_AGG(DISTINCT client_id::text, '')::uuid AS client_id,
                 SUM(loyalty_points) AS appointment_loyalty_points
      	  FROM purchase_union_service_cte
      	  GROUP BY appointment_id
      ), client_loyalty_points_cte AS (
          SELECT client_id,
                 SUM(appointment_loyalty_points) AS client_loyalty_points
          FROM appointment_loyalty_points_cte
          GROUP BY client_id
      )
      SELECT c.*, clp_cte.client_loyalty_points
      FROM client_loyalty_points_cte clp_cte
      JOIN client c ON clp_cte.client_id = c.id
      WHERE c.banned = FALSE
      ORDER BY client_loyalty_points DESC, first_name DESC
      LIMIT :number;"""; // secondarily order by first_name for matching loyalty points

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final BeanPropertyRowMapper<ClientRow> rowMapper =
      BeanPropertyRowMapper.newInstance(ClientRow.class);

  public List<ClientRow> findTopNonBannedClientsWithMostLoyaltyPointsSinceCutoffDate(
      LocalDate cutoffDate, int numberOfClients) {

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("cutoff", cutoffDate.toString());
    parameters.addValue("number", numberOfClients);

    return jdbcTemplate.query(
        TOP_NON_BANNED_CLIENTS_WITH_MOST_LOYALTY_POINTS_SINCE_CUTOFF_DATE_QUERY,
        parameters,
        rowMapper);
  }
}

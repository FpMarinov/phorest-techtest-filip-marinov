package com.phorest.factory;

import com.phorest.model.csv.AppointmentCsvBean;
import com.phorest.model.csv.ClientCsvBean;
import com.phorest.model.csv.PurchaseCsvBean;
import com.phorest.model.csv.ServiceCsvBean;
import com.phorest.model.csv.common.CsvBean;
import com.phorest.model.entity.Client.Gender;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CsvBeanFactory {
  public static final String APPOINTMENT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss xx";

  public static final DateTimeFormatter APPOINTMENT_FORMATTER =
      DateTimeFormatter.ofPattern(APPOINTMENT_DATE_TIME_PATTERN).withZone(ZoneOffset.UTC);

  public <T extends CsvBean> T buildCsvBean(String[] line, Class<T> returnElementType) {
    if (ClientCsvBean.class.equals(returnElementType)) {
      return (T) buildClientCsvBean(line);
    } else if (AppointmentCsvBean.class.equals(returnElementType)) {
      return (T) buildAppointmentCsvBean(line);
    } else if (PurchaseCsvBean.class.equals(returnElementType)) {
      return (T) buildPurchaseCsvBean(line);
    } else if (ServiceCsvBean.class.equals(returnElementType)) {
      return (T) buildServiceCsvBean(line);
    }

    throw new IllegalArgumentException(); // unreachable
  }

  private ClientCsvBean buildClientCsvBean(String[] line) {
    return ClientCsvBean.builder()
        .id(UUID.fromString(line[0]))
        .firstName(line[1])
        .lastName(line[2])
        .email(line[3])
        .phone(line[4])
        .gender(Gender.valueOf(line[5].toUpperCase()))
        .banned(Boolean.parseBoolean(line[6]))
        .build();
  }

  private AppointmentCsvBean buildAppointmentCsvBean(String[] line) {
    return AppointmentCsvBean.builder()
        .id(UUID.fromString(line[0]))
        .clientId(UUID.fromString(line[1]))
        .startTime(Instant.from(APPOINTMENT_FORMATTER.parse(line[2])))
        .endTime(Instant.from(APPOINTMENT_FORMATTER.parse(line[3])))
        .build();
  }

  private PurchaseCsvBean buildPurchaseCsvBean(String[] line) {
    return PurchaseCsvBean.builder()
        .id(UUID.fromString(line[0]))
        .appointmentId(UUID.fromString(line[1]))
        .name(line[2])
        .price(new BigDecimal(line[3]))
        .loyaltyPoints(Integer.parseInt(line[4]))
        .build();
  }

  private ServiceCsvBean buildServiceCsvBean(String[] line) {
    return ServiceCsvBean.builder()
        .id(UUID.fromString(line[0]))
        .appointmentId(UUID.fromString(line[1]))
        .name(line[2])
        .price(new BigDecimal(line[3]))
        .loyaltyPoints(Integer.parseInt(line[4]))
        .build();
  }
}

package com.phorest.validator;

import com.phorest.exception.InvalidCsvFileException;
import com.phorest.model.csv.AppointmentCsvBean;
import com.phorest.model.csv.ClientCsvBean;
import com.phorest.model.csv.PurchaseCsvBean;
import com.phorest.model.csv.ServiceCsvBean;
import com.phorest.model.csv.common.CsvBean;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CsvLineValidator {
  public <T extends CsvBean> void validateFirstLine(String[] line, Class<T> returnElementType) {
    List<Boolean> validationConditions = getValidFirstLineConditions(line, returnElementType);

    boolean isFirstLineInvalid = validationConditions.stream().anyMatch(condition -> !condition);

    if (isFirstLineInvalid) {
      throw new InvalidCsvFileException();
    }
  }

  private <T extends CsvBean> List<Boolean> getValidFirstLineConditions(
      String[] line, Class<T> returnElementType) {

    if (ClientCsvBean.class.equals(returnElementType)) {
      return getValidClientFirstLineConditions(line);
    } else if (AppointmentCsvBean.class.equals(returnElementType)) {
      return getValidAppointmentFirstLineConditions(line);
    } else if (PurchaseCsvBean.class.equals(returnElementType)
        || ServiceCsvBean.class.equals(returnElementType)) {

      return getValidPurchaseAndServiceFirstLineConditions(line);
    }

    return List.of(); // unreachable
  }

  private List<Boolean> getValidClientFirstLineConditions(String[] line) {
    return List.of(
        line.length == 7,
        line[0].equals("id"),
        line[1].equals("first_name"),
        line[2].equals("last_name"),
        line[3].equals("email"),
        line[4].equals("phone"),
        line[5].equals("gender"),
        line[6].equals("banned"));
  }

  private List<Boolean> getValidAppointmentFirstLineConditions(String[] line) {
    return List.of(
        line.length == 4,
        line[0].equals("id"),
        line[1].equals("client_id"),
        line[2].equals("start_time"),
        line[3].equals("end_time"));
  }

  private List<Boolean> getValidPurchaseAndServiceFirstLineConditions(String[] line) {
    return List.of(
        line.length == 5,
        line[0].equals("id"),
        line[1].equals("appointment_id"),
        line[2].equals("name"),
        line[3].equals("price"),
        line[4].equals("loyalty_points"));
  }

  public <T extends CsvBean> void validateNonFirstLine(String[] line, Class<T> returnElementType) {
    int validLineLength = getValidLineLength(returnElementType);

    if (line.length != validLineLength) {
      throw new InvalidCsvFileException();
    }
  }

  private <T extends CsvBean> int getValidLineLength(Class<T> returnElementType) {
    if (ClientCsvBean.class.equals(returnElementType)) {
      return 7;
    } else if (AppointmentCsvBean.class.equals(returnElementType)) {
      return 4;
    } else if (PurchaseCsvBean.class.equals(returnElementType)
        || ServiceCsvBean.class.equals(returnElementType)) {

      return 5;
    }

    return -1; // unreachable
  }
}

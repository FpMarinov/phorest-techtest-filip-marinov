package com.phorest.validator;

import com.phorest.model.csv.common.CsvBean;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CsvBeanValidator {
  private final Validator validator;

  public void validateCsvBean(CsvBean csvBean) {
    Set<ConstraintViolation<CsvBean>> violations = validator.validate(csvBean);

    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}

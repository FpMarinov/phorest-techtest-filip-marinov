package com.phorest.service;

import com.opencsv.CSVReader;
import com.phorest.exception.InvalidCsvFileException;
import com.phorest.factory.CsvBeanFactory;
import com.phorest.model.csv.common.CsvBean;
import com.phorest.util.CsvUtils;
import com.phorest.validator.CsvLineValidator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvServiceImpl implements CsvService {
  public static final String CSV_CONTENT_TYPE = "text/csv";

  private final CsvBeanFactory csvBeanFactory;
  private final CsvLineValidator csvLineValidator;

  @Override
  public <T extends CsvBean> Page<T> getElementsFromCsvFile(
      MultipartFile file, Class<T> returnElementType, int page, int size) {

    if (size <= 0 || page < 0) {
      throw new IllegalArgumentException();
    }

    if (file.isEmpty() || !CSV_CONTENT_TYPE.equals(file.getContentType())) {
      throw new InvalidCsvFileException();
    }

    try (CSVReader csvReader = CsvUtils.buildCsvReader(file.getBytes())) {
      Iterator<String[]> csvLineIterator = csvReader.iterator();

      String[] line = csvLineIterator.next();
      csvLineValidator.validateFirstLine(line, returnElementType);

      List<T> content = new ArrayList<>();
      long offset = (long) page * (long) size;
      long totalElementsProcessed = 0L;

      while (csvLineIterator.hasNext()) {
        line = csvLineIterator.next();
        csvLineValidator.validateNonFirstLine(line, returnElementType);

        if (isCurrentElementInPage(size, offset, totalElementsProcessed)) {
          content.add(csvBeanFactory.buildCsvBean(line, returnElementType));
        }

        totalElementsProcessed++;
      }

      Pageable pageable = PageRequest.of(page, size);

      return new PageImpl<>(content, pageable, totalElementsProcessed);
    } catch (Exception e) {
      throw new InvalidCsvFileException(e);
    }
  }

  private boolean isCurrentElementInPage(int size, long offset, long totalElementsProcessed) {
    return offset <= totalElementsProcessed && totalElementsProcessed < offset + size;
  }
}

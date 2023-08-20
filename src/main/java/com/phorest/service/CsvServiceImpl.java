package com.phorest.service;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.phorest.exception.InvalidCsvFileException;
import com.phorest.model.csv.common.CsvBean;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvServiceImpl implements CsvService {
  public static final String CSV_CONTENT_TYPE = "text/csv";

  @Override
  @SneakyThrows(IOException.class)
  public <T extends CsvBean> List<T> getElementsFromCsvFile(
      MultipartFile file, Class<T> returnElementType) {

    if (file.isEmpty() || !CSV_CONTENT_TYPE.equals(file.getContentType())) {
      throw new InvalidCsvFileException();
    }

    try (CSVReader csvReader =
        new CSVReader(new InputStreamReader(new ByteArrayInputStream(file.getBytes())))) {

      return parseCsv(csvReader, returnElementType);
    }
  }

  private <T extends CsvBean> List<T> parseCsv(CSVReader csvReader, Class<T> returnElementType) {
    CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(csvReader).withType(returnElementType).build();

    return csvToBean.parse();
  }
}

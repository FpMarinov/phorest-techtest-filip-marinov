package com.phorest.service;

import com.phorest.model.csv.common.CsvBean;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CsvService {
  <T extends CsvBean> List<T> getElementsFromCsvFile(
      MultipartFile file, Class<T> returnElementType);
}

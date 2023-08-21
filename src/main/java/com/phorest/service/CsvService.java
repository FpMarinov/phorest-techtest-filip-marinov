package com.phorest.service;

import com.phorest.model.csv.common.CsvBean;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CsvService {
  <T extends CsvBean> Page<T> getElementsFromCsvFile(
      MultipartFile file, Class<T> returnElementType, int page, int size);
}

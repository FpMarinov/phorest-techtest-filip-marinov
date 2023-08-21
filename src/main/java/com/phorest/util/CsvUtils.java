package com.phorest.util;

import com.opencsv.CSVReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CsvUtils {
  public static CSVReader buildCsvReader(byte[] array) {
    return new CSVReader(new InputStreamReader(new ByteArrayInputStream(array)));
  }
}

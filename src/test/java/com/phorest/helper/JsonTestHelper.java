package com.phorest.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonTestHelper {
  public static String toJson(Object body) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return toJson(mapper, body);
  }

  public static String toJson(ObjectMapper mapper, Object body) throws JsonProcessingException {
    return mapper.writeValueAsString(body);
  }

  public static <T> T fromJson(String string, TypeReference<T> typeReference) {
    ObjectMapper mapper = new ObjectMapper();

    return fromJson(mapper, string, typeReference);
  }

  @SneakyThrows(JsonProcessingException.class)
  public static <T> T fromJson(ObjectMapper mapper, String string, TypeReference<T> typeReference) {
    return mapper.readValue(string, typeReference);
  }
}

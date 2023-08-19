package com.phorest.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonHelper {
  public static String toJson(Object body) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return toJson(mapper, body);
  }

  public static String toJson(ObjectMapper mapper, Object body) throws JsonProcessingException {
    return mapper.writeValueAsString(body);
  }
}

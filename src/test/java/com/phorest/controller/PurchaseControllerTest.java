package com.phorest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql({"classpath:dataset/truncate.sql", "classpath:dataset/controller/purchase.sql"})
public class PurchaseControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper mapper;
}

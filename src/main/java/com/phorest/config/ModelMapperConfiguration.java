package com.phorest.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.module.jdk8.Jdk8Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfiguration {
  @Bean
  public ModelMapper modelMapper() {
    ModelMapper mapper = new ModelMapper().registerModule(new Jdk8Module());

    mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

    return mapper;
  }
}

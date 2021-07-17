package com.testcontainer.api.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

// https://www.baeldung.com/spring-data-mongodb-reactive
@Configuration
public class ConfigMongoReactive extends AbstractReactiveMongoConfiguration {

  @Bean
  public MongoClient mongoClient() {
    return MongoClients.create();
  }


  @Override
  protected String getDatabaseName() {
    return "reactive";
  }
}
package com.testcontainer.api.config;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

// https://www.baeldung.com/spring-data-mongodb-reactive
@Configuration
public class ReactiveMongoConfig {

  @Autowired
  MongoClient mongoClient;


  @Bean("mongoSingleton")
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public ReactiveMongoTemplate reactiveMongoTemplateSingleton() {
    return new ReactiveMongoTemplate(mongoClient,"test");
  }


  //https://www.baeldung.com/spring-inject-prototype-bean-into-singleton
  @Bean("mongoPrototype")
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public ReactiveMongoTemplate reactiveMongoTemplatePrototype() {
    return new ReactiveMongoTemplate(mongoClient,"test");
  }
}
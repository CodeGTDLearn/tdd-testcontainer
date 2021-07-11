package com.testcontainer.restartedContainer;

import org.junit.jupiter.api.extension.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;


//https://www.baeldung.com/spring-dynamicpropertysource
public class MongoDbExtension implements BeforeEachCallback, AfterEachCallback {

  private MongoDBContainer mongoDBContainer;

  @Override
  public void beforeEach(ExtensionContext context) {
    mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.2"));
    mongoDBContainer.start();
    System.out.println(String.format("--------------> uri: %s",mongoDBContainer.getReplicaSetUrl()));
    System.out.println(String.format("--------------> isRunning: %s",mongoDBContainer.isRunning()));
    System.setProperty("spring.data.mongodb.uri",mongoDBContainer.getReplicaSetUrl());
  }

  @Override
  public void afterEach(ExtensionContext context) {
    mongoDBContainer.stop();
  }
}
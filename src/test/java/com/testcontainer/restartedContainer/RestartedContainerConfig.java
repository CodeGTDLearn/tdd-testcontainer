package com.testcontainer.restartedContainer;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/*
RESTARTED TESTCONTAINERS
https://www.testcontainers.org/test_framework_integration/junit_5/#restarted-containers
 */
@Getter
@Testcontainers
public class RestartedContainerConfig {

  @Value("${test.mongodb.port}")
  private int MONGODB_PORT;

  @Container
  public final MongoDBContainer restartedContainer =
       new MongoDBContainer(DockerImageName.parse("mongo:4.4.2"))
            .addExposedPort(MONGODB_PORT) // todo 01: acrescentar porta e outras variaveis
       //.withEnv("MONGO_INITDB_DATABASE", "admin")
       //.withEnv(“MONGO_INITDB_ROOT_USERNAME”, “admin”)
       //.withEnv(“MONGO_INITDB_ROOT_PASSWORD”, “whatever”)
       ;
}






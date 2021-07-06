package com.testcontainer.sharedContainer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
SPEED-UP TESTCONTAINERS
https://callistaenterprise.se/blogg/teknik/2020/10/09/speed-up-your-testcontainers-tests/
https://medium.com/pictet-technologies-blog/speeding-up-your-integration-tests-with
-testcontainers-e54ab655c03d
 */
@Testcontainers
public class SharedContainerConfig {

    /* SPEED UP 01: TEST-CONTAINERS
        Containers declared as STATIC fields will be shared between test methods,
        starting only once before any test method is executed and
        stopped after the last test method has executed.
        Containers declared as INSTANCE fields will be started and stopped for every test method.
     */
    @Container
    public static final MongoDBContainer container = new MongoDBContainer("mongo:4.4.2");


    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",container::getReplicaSetUrl);
    }

}





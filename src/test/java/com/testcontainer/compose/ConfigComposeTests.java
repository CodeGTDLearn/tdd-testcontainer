package com.testcontainer.compose;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.blockhound.BlockHound;

import java.io.File;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/*------------------------------------------------------------
                         DataMongoTest
  ------------------------------------------------------------
a) AMBOS FUNCIONAM:
 - @DataMongoTest
 - @DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
b) USO ALTERNATIVO (DataMongoTest/SpringBootTest) - CONFLITAM ENTRE-SI:
 - @SpringBootTest(webEnvironment = RANDOM_PORT)
  ------------------------------------------------------------*/
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@Slf4j
@Testcontainers
public class ConfigComposeTests {

    final private static Long MAX_TIMEOUT = 15000L;
    final private static ContentType API_CONTENT_TYPE = ContentType.JSON;


    final private String COMPOSE_PATH = "src/test/resources/compose-testcontainers.yml";
    final private int SERVICE_DB_PORT = 27017;
    final private String SERVICE_DB = "db";


    //    @Container //Annotacao deve ficar na classe receptora
    public DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(new File(COMPOSE_PATH))
                    .withExposedService(
                            SERVICE_DB,
                            SERVICE_DB_PORT
                                       )
//                    .waitingFor(SERVICE_DB)
            ;


    @BeforeAll
    static void beforeAll() {

        BlockHound.install(
                builder -> builder
                        .allowBlockingCallsInside("java.io.PrintStream",
                                                  "write"
                                                 )
                          );

        //DEFINE CONFIG-GLOBAL PARA OS REQUESTS DOS TESTES
        RestAssuredWebTestClient.requestSpecification =
                new WebTestClientRequestSpecBuilder()
                        .setContentType(API_CONTENT_TYPE)
                        .build();

        //DEFINE CONFIG-GLOBAL PARA OS RESPONSE DOS TESTES
        RestAssuredWebTestClient.responseSpecification =
                new ResponseSpecBuilder()
                        .expectResponseTime(
                                Matchers.lessThanOrEqualTo(MAX_TIMEOUT))
                        .build();
    }


    @AfterAll
    static void afterAll() {
        RestAssuredWebTestClient.reset();
    }
}





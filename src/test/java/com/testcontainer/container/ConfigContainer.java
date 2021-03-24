package com.testcontainer.container;

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

//AMBOS FUNCIONAM 'DataMongoTest' com ou sem 'excludeAutoConfiguration',
//MAS TUTORIAL RECOMENDA o uso do 'excludeAutoConfiguration'
//@DataMongoTest
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@Slf4j
public class ConfigContainer extends ConfigTestContainers {

    final static String BASE_URI = "http://localhost:8080/customer";
    final private static Long MAX_TIMEOUT = 15000L;
    final private static ContentType API_CONTENT_TYPE = ContentType.JSON;
    //    @LocalServerPort
    final private static int port = 8080;


    @BeforeAll
    static void beforeAll() {
        //        BlockHound.install(
        //builder -> builder.allowBlockingCallsInside("java.util.UUID" ,"randomUUID")
        //                          );

        //substitue os ".log().And()." em todos os REstAssureTestes
        //        RestAssuredWebTestClient.enableLoggingOfRequestAndResponseIfValidationFails();
        //        RestAssuredWebTestClient.config = new RestAssuredWebTestClientConfig()
        //        .logConfig(LogDetail.BODY);

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




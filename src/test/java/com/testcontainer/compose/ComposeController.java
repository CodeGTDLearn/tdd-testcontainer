package com.testcontainer.compose;

import com.github.javafaker.Faker;
import com.testcontainer.api.Customer;
import com.testcontainer.api.ICustomerRepo;
import com.testcontainer.api.ICustomerService;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.testcontainer.databuilder.CustomerBuilder.customerWithIdAndName;
import static com.testcontainer.databuilder.CustomerBuilder.customerWithName;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
public class ComposeController extends ConfigCompose {

    private List<Customer> customerList;
    private Customer customerWithId;

    // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
    // SHOULD BE USED WITH 'TEST-CONTAINERS'
    // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER
    @Autowired
    WebTestClient mockedWebClient;

    @Autowired
    private ICustomerService service;

    final MediaType MTYPE_JSON = MediaType.APPLICATION_JSON;
    final ContentType CONT_ANY = ContentType.ANY;
    final ContentType CONT_JSON = ContentType.JSON;
    final String REQ_MAP = "/customer";


    @BeforeEach
    public void setUpLocal() {

        //REAL-SERVER INJECTED IN WEB-TEST-CLIENT(non-blocking client)'
        //SHOULD BE USED WHEN 'DOCKER-COMPOSE' UP A REAL-WEB-SERVER
        //BECAUSE THERE IS 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
        // realWebClient = WebTestClient.bindToServer()
        //                      .baseUrl("http://localhost:8080/customer")
        //                      .build();

        customerWithId = customerWithIdAndName(Faker.instance()
                                                    .idNumber()
                                                    .valid()).create();

        customerList = asList(customerWithName().create(),
                              customerWithName().create(),
                              customerWithName().create(),
                              customerWithId
                             );

        service.deleteAll()
            .thenMany(Flux.fromIterable(customerList))
            .flatMap(service::save)
            .doOnNext((item -> System.out.println("Inserted item is - TEST: " + item)))
            .blockLast(); // THATS THE WHY, BLOCKHOUND IS NOT BEING USED.
    }


    @Test
    public void save() {
        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)
                .header("Accept",ContentType.ANY)
                .header("Content-type",ContentType.JSON)
                .body(customerWithId)

                .when()
                .post(REQ_MAP)

                .then()
                .statusCode(CREATED.value())
        ;
    }


    @Test
    public void findAll() {
        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

                .when()
                .get(REQ_MAP)

                .then()
                .statusCode(OK.value())
                .log()
                .headers()
                .and()
                .log()
                .body()
                .and()

                .body("id",hasItem(customerWithId.getId()))
        ;
    }


    @Test
    public void deleteAll() {
        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

                .when()
                .delete(REQ_MAP)

                .then()
                .statusCode(NO_CONTENT.value())
        ;

    }


    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });

            Schedulers.parallel()
                      .schedule(task);

            task.get(10,TimeUnit.SECONDS);
            fail("should fail");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            assertTrue(e.getCause() instanceof BlockingOperationError,"detected");
        }
    }
}

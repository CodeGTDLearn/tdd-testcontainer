package com.testcontainer.container;


import com.github.javafaker.Faker;
import com.testcontainer.api.Customer;
import com.testcontainer.api.ICustomerRepo;
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
import reactor.core.publisher.Mono;
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
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@AutoConfigureWebTestClient
public class ControllerContainer extends ConfigTestContainers {

    private List<Customer> customerList;
    private Customer customerWithId;

    @Autowired
    WebTestClient client;

    @Autowired
    private ICustomerRepo repo;

    final MediaType MTYPE_JSON = MediaType.APPLICATION_JSON;
    final ContentType CONT_ANY = ContentType.ANY;
    final ContentType CONT_JSON = ContentType.JSON;


    @BeforeEach
    public void setUpLocal() {

        customerWithId = customerWithIdAndName(Faker.instance()
                                                    .idNumber()
                                                    .valid()).create();

        customerList = asList(customerWithName().create(),
                              customerWithName().create(),
                              customerWithName().create(),
                              customerWithId
                             );

        repo.deleteAll()
            .thenMany(Flux.fromIterable(customerList))
            .flatMap(repo::save)
            .doOnNext((item -> System.out.println("Inserted item is - TEST: " + item)))
            .blockLast(); // THATS THE WHY, BLOCKHOUND IS NOT BEING USED.
    }


    @Test
    public void webTestClient() {
        client
                .post()
                .uri("/customer")
                .body(Mono.just(customerWithId),Customer.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectHeader()
                .contentType(MTYPE_JSON)
                .expectBody()
                .jsonPath("$.id")
                .isEqualTo(customerWithId.getId())
                .jsonPath("$.email")
                .isEqualTo(customerWithId.getEmail())
                .jsonPath("$.rating")
                .isEqualTo(customerWithId.getRating())
        ;
    }


    @Test
    public void RA() {
        RestAssuredWebTestClient
                .given()
                .webTestClient(client)
                .header("Accept",CONT_ANY)
                .header("Content-type",CONT_JSON)
                .body(customerWithId)

                .when()
                .post("/customer")

                .then()
                .log()
                .headers()
                .and()
                .log()
                .body()
                .and()
                .contentType(CONT_JSON)
                .statusCode(CREATED.value())

                //equalTo para o corpo do Json
                .body("email",containsString(customerWithId.getEmail()));
    }


    @Test
    public void save() {
        RestAssuredWebTestClient
                .given()
                .webTestClient(client)
                .header("Accept",ContentType.ANY)
                .header("Content-type",ContentType.JSON)
                .body(customerWithId)

                .when()
                .post()

                .then()
                .statusCode(FORBIDDEN.value())
        ;
    }

    @Test
    public void findAll() {
        RestAssuredWebTestClient
                .given()
                .webTestClient(client)

                .when()
                .get()

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
    public void removeAll() {
        RestAssuredWebTestClient
                .given()
                .webTestClient(client)

                .when()
                .delete()

                .then()
                .statusCode(FORBIDDEN.value())
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

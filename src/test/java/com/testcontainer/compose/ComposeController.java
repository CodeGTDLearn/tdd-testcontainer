package com.testcontainer.compose;

import com.github.javafaker.Faker;
import com.testcontainer.api.Customer;
import com.testcontainer.api.ICustomerService;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.testcontainer.databuilder.CustomerBuilder.customerWithIdAndName;
import static com.testcontainer.databuilder.CustomerBuilder.customerWithName;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.http.HttpStatus.*;

public class ComposeController extends ConfigControllerTests {

    private List<Customer> customerList;
    private Customer customerWithId;
    final String REQ_MAP = "/customer";


    @Container
    private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;


    // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
    // SHOULD BE USED WITH 'TEST-CONTAINERS'
    // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
    @Autowired
    WebTestClient mockedWebClient;


    @Autowired
    private ICustomerService service;


    @BeforeAll
    static void beforeAll() {
        ConfigComposeTests.beforeAll();
    }


    @AfterAll
    static void afterAll() {
        ConfigComposeTests.afterAll();
    }


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

        customerList = asList(
                customerWithName().create(),
                customerWithId
                             );
    }


    void cleanDbToTest() {
        StepVerifier
                .create(service.deleteAll())
                .expectSubscription()
                .verifyComplete();

        System.out.println("\n\n==================> CLEAN-DB-TO-TEST" +
                                   " <==================\n\n");
    }


    private void StepVerifierCountCostumerFlux(Flux<Customer> customerFlux,int totalFluxElements) {
        StepVerifier
                .create(customerFlux)
                .expectSubscription()
                .expectNextCount(totalFluxElements)
                .verifyComplete();
    }


    //    @Disabled
    @Test
    public void findAll() {

        final Flux<Customer> customerFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(customerList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifierCountCostumerFlux(customerFlux,2);

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
    public void save() {
        cleanDbToTest();

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)
                //                .header("Accept",CONT_ANY)
                //                .header("Content-type",CONT_JSON)
                .body(customerWithId)

                .when()
                .post(REQ_MAP)

                .then()
                .statusCode(CREATED.value())
                .body("id",containsStringIgnoringCase(customerWithId.getId()))
        ;
    }


    @Test
    public void deleteAll() {
        cleanDbToTest();

        StepVerifier
                .create(service.save(customerWithId))
                .expectSubscription()
                .expectNext(customerWithId)
                .verifyComplete();

        StepVerifierCountCostumerFlux(service.findAll(),1);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

                .when()
                .delete(REQ_MAP)

                .then()
                .statusCode(NO_CONTENT.value())
        ;

        StepVerifierCountCostumerFlux(service.findAll(),0);
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
            Assertions.fail("should fail");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError,"detected");
        }
    }
}

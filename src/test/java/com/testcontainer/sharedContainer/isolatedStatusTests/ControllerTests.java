package com.testcontainer.sharedContainer.isolatedStatusTests;

import com.github.javafaker.Faker;
import com.testcontainer.api.Customer;
import com.testcontainer.api.ICustomerService;
import com.testcontainer.sharedContainer.ConfigTests;
import com.testcontainer.sharedContainer.ContainerConfig;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
public class ControllerTests extends ContainerConfig {

  private List<Customer> customerList;
  private Customer customerWithId;

  //MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  //SHOULD BE USED WITH 'TEST-CONTAINERS'
  //BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER
  @Autowired
  WebTestClient mockedWebClient;

  @Autowired
  private ICustomerService service;

  final private String REQ_MAP = "/customer";


  @BeforeAll
  public static void beforeAll() {
    ConfigTests.beforeAll();
  }


  @AfterAll
  public static void afterAll() {
    ConfigTests.afterAll();
  }


  @BeforeEach
  public void setUp() {
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


  @Test
  @DisplayName("Save_WebClient")
  public void save_WebTestClient() {
    mockedWebClient
         .post()
         .uri(REQ_MAP)
         .body(Mono.just(customerWithId),Customer.class)
         .exchange()
         .expectStatus()
         .isCreated()
         .expectHeader()
         .contentType(MediaType.APPLICATION_JSON)
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
  @DisplayName("Save")
  public void save_RA() {
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
         .and()
         .body("id",containsStringIgnoringCase(customerWithId.getId()))
         .and()
         .body("email",containsStringIgnoringCase(customerWithId.getEmail()))
         .and()
         .body("rating",is(customerWithId.getRating()))
    ;

    StepVerifier
         .create(service.findById(customerWithId.getId()))
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();
  }


  @Test
  @DisplayName("FindAll")
  public void findAll() {

    StepVerifier
         .create(service.save(customerWithId))
         .expectSubscription()
         .expectNext(customerWithId)
         .verifyComplete();

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
  @DisplayName("DeleteById")
  public void deleteById() {
    StepVerifier
         .create(service.save(customerWithId))
         .expectSubscription()
         .expectNext(customerWithId)
         .verifyComplete();

    StepVerifier
         .create(service.findById(customerWithId.getId()))
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)

         .when()
         .delete(REQ_MAP + "/" + customerWithId.getId())

         .then()
         .statusCode(NO_CONTENT.value())
    ;

    StepVerifier
         .create(service.findById(customerWithId.getId()))
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();
  }


  @Test
  @DisplayName("Container")
  public void checkContainer() {
    assertTrue(sharedContainer.isRunning());
  }


  @Test
  @DisplayName("BHWorks")
  public void bHWorks() {
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

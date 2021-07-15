package com.testcontainer.sharedContainer.isolatedStatusTests;

import com.testcontainer.api.Customer;
import com.testcontainer.api.ICustomerRepo;
import com.testcontainer.sharedContainer.ConfigTests;
import com.testcontainer.sharedContainer.ContainerConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.testcontainer.databuilder.CustomerBuilder.customerWithName;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepoTests extends ConfigTests {

  private List<Customer> customerList;
  private Flux<Customer> customerFlux;

  @Lazy
  @Autowired
  private ICustomerRepo repo;


  @BeforeAll
  public static void beforeAll() {
    ConfigTests.beforeAll();
  }


  @AfterAll
  public static void afterAll() {
    ConfigTests.afterAll();
  }


  @BeforeEach
  void setUp() {
    Customer customer1 = customerWithName().create();
    Customer customer2 = customerWithName().create();
    customerList = Arrays.asList(customer1,customer2);
    customerFlux = saveAndGetCustomerFlux(customerList);
  }


  @Test
  @DisplayName("Save")
  public void save() {
    StepVerifier.create(customerFlux)
                .expectNextSequence(customerList)
                .verifyComplete();
  }


  @Test
  @DisplayName("Find: Content")
  public void find_count() {
    StepVerifier
         .create(customerFlux)
         .expectSubscription()
         .expectNextMatches(customer -> customerList.get(0)
                                                    .getEmail()
                                                    .equals(customer.getEmail()))
         .expectNextMatches(customer -> customerList.get(1)
                                                    .getEmail()
                                                    .equals(customer.getEmail()))
         .verifyComplete();
  }


  @Test
  @DisplayName("Find: Objects")
  public void find_object() {
    StepVerifier
         .create(customerFlux)
         .expectNext(customerList.get(0))
         .expectNext(customerList.get(1))
         .verifyComplete();
  }


  @Test
  @DisplayName("Delete")
  public void delete() {
    StepVerifier.create(customerFlux)
                .expectNextSequence(customerList)
                .verifyComplete();

    StepVerifier
         .create(repo.deleteById(customerList.get(0)
                                             .getId()))
         .expectSubscription()
         .verifyComplete();

    Mono<Customer> monoTest = repo.findById(customerList.get(0)
                                                        .getId());

    StepVerifier
         .create(monoTest)
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


  private Flux<Customer> saveAndGetCustomerFlux(List<Customer> customerList) {

    //    return repo.deleteAll()
    //               .thenMany(Flux.fromIterable(customerList))
    //               .flatMap(repo::save)
    //               .doOnNext(item -> repo.findAll());


    return repo.saveAll(customerList);
  }
}
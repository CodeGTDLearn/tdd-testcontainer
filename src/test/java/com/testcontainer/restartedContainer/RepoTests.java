package com.testcontainer.restartedContainer;

import com.testcontainer.api.Customer;
import com.testcontainer.api.ICustomerRepo;
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

  private Customer customer1;
  private Customer customer2;
  private Customer customer3;
  private List<Customer> customerList;

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
    loadDataMass();
  }


  @AfterEach
  void tearDown() {
//    cleanDb();
  }


  private void loadDataMass() {
    customer1 = customerWithName().create();
    customer2 = customerWithName().create();
    customer3 = customerWithName().create();
    customerList = Arrays.asList(customer1,customer3);
  }


  private void cleanDb() {
    StepVerifier
         .create(repo.deleteAll())
         .expectSubscription()
         .verifyComplete();

    System.out.println("\n\n==================> CLEAN-DB-TO-TEST" +
                            " <==================\n\n");
  }


  private void saveAndCheckObjectInDb(Customer customer) {
    StepVerifier
         .create(repo.save(customer))
         .expectSubscription()
         .expectNext(customer)
         .verifyComplete();
  }


  @Test
  @DisplayName("Container")
  void checkContainer() {
    assertTrue(restartedContainer.isRunning());
  }


  @Test
  @DisplayName("Save")
  public void save() {
    saveAndCheckObjectInDb(customerWithName().create());
  }


  @Test
  @DisplayName("Find: Content")
  public void find_count() {
    var customer1 = customerWithName().create();
    var customer3 = customerWithName().create();
    var list = Arrays.asList(customer1,customer3);

    final Flux<Customer> customerFlux =
         repo.deleteAll()
             .thenMany(Flux.fromIterable(list))
             .flatMap(repo::save)
             .doOnNext(item -> repo.findAll());

    StepVerifier
         .create(customerFlux)
         .expectSubscription()
         .expectNextMatches(customer -> customer1.getEmail()
                                                 .equals(customer.getEmail()))
         .expectNextMatches(customer -> customer3.getEmail()
                                                 .equals(customer.getEmail()))
         .verifyComplete();
  }


  @Test
  @DisplayName("Find: Objects")
  public void find_2() {
    var customer1 = customerWithName().create();
    var customer3 = customerWithName().create();
    var list = Arrays.asList(customer1,customer3);

    final Flux<Customer> customerFlux =
         repo.deleteAll()
             .thenMany(Flux.fromIterable(list))
             .flatMap(repo::save)
             .doOnNext(item -> repo.findAll());

    StepVerifier
         .create(customerFlux)
         .expectNext(customer1)
         .expectNext(customer3)
         .verifyComplete();
  }


  @Test
  @DisplayName("Delete")
  public void deleteAll_count() {

    var customer = customerWithName().create();
    saveAndCheckObjectInDb(customer);

    StepVerifier
         .create(repo.deleteById(customer.getId()))
         .expectSubscription()
         .verifyComplete();

    Mono<Customer> monoTest = repo.findById(customer.getId());

    StepVerifier
         .create(monoTest)
         .expectSubscription()
         .expectNextCount(0)
         .verifyComplete();

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
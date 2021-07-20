package com.testcontainer.sharedContainer.isolatedStatusTests;

import com.testcontainer.api.entity.Customer;
import com.testcontainer.api.service.CustomerService;
import com.testcontainer.api.repo.ICustomerRepo;
import com.testcontainer.api.service.ICustomerService;
import com.testcontainer.sharedContainer.ConfigTests;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

public class ServiceTests extends ConfigTests {

  private List<Customer> customerList;
  private Flux<Customer> customerFlux;

  //  @Lazy
  //  @Autowired
  //  private ICustomerRepo repo;

  @Autowired
  ApplicationContext context;

  private ICustomerService service;


  ICustomerRepo repo = (ICustomerRepo) context.getBean("ICustomerRepo");


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
    //------------------------------------------//
    //VERY IMPORTANT!!!!
    //DEPENDENCY INJECTION MUST BE DONE MANUALLY
    //    repo = new TempRepo();
    service = new CustomerService(repo);
    //------------------------------------------//

    Customer customer1 = customerWithName().create();
    Customer customer2 = customerWithName().create();
    customerList = Arrays.asList(customer1,customer2);
    customerFlux = service.saveAll(customerList);
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
  @DisplayName("DeleteById")
  public void deleteById() {
    StepVerifier.create(customerFlux)
                .expectNextSequence(customerList)
                .verifyComplete();

    StepVerifier
         .create(service.deleteById(customerList.get(0)
                                                .getId()))
         .expectSubscription()
         .verifyComplete();

    Mono<Customer> monoTest = service.findById(customerList.get(0)
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
}


//  private Flux<Customer> saveAndGetCustomerFlux(List<Customer> customerList) {
//    return repo.deleteAll()
//               .thenMany(Flux.fromIterable(customerList))
//               .flatMap(repo::save)
//               .doOnNext(item -> repo.findAll());
//    return service.saveAll(customerList);
//  }
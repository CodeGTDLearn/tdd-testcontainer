package com.testcontainer.sharedContainer.isolatedStatusTests;

import com.testcontainer.api.Customer;
import com.testcontainer.api.CustomerService;
import com.testcontainer.api.ICustomerRepo;
import com.testcontainer.api.ICustomerService;
import com.testcontainer.sharedContainer.ConfigTests;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.testcontainer.databuilder.CustomerBuilder.customerWithName;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceTests2 extends ConfigTests {

  private List<Customer> customerList;
  private Flux<Customer> customerFlux;

  @Autowired
  ApplicationContext context;

  // https://www.baeldung.com/spring-data-mongodb-reactive
  // https://stackoverflow.com/questions/46092710/how-can-i-access-the-repository-from-the-entity-in-spring-boot
  // https://stackoverflow.com/questions/2425015/how-to-access-spring-context-in-junit-tests-annotated-with-runwith-and-context
  //https://stackoverflow.com/questions/20181022/creating-a-repository-instance-in-spring-data
  //https://stackoverflow.com/questions/14266089/how-to-retrieve-spring-data-repository-instance-for-given-domain-class
  //https://docs.spring.io/spring-data/data-commons/docs/1.6.1.RELEASE/reference/html/repositories.html
//1. Working with Spring Data Repositories
  // 1.2.3 Creating repository instances
  //  JavaConfig
//  The repository infrastructure...
  private ICustomerService service;
  ICustomerRepo repo = (ICustomerRepo) context.getBean("ICustomerRepo");


  @BeforeEach
  public void setUp() {
    service = new CustomerService(repo);

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
  @DisplayName("Find: Objects")
  public void find_object() {
    StepVerifier
         .create(customerFlux)
         .expectNext(customerList.get(0))
         .expectNext(customerList.get(1))
         .verifyComplete();
  }
}
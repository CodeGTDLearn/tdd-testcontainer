package com.testcontainer.compose;

import com.testcontainer.api.Customer;
import com.testcontainer.api.CustomerService;
import com.testcontainer.api.ICustomerRepo;
import com.testcontainer.api.ICustomerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
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

public class ComposeService extends ConfigComposeTests {

    private Customer customer1;
    private Customer customer2;
    private List<Customer> customerList;

    @Container
    private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;


    @Autowired
    private ICustomerRepo repo;
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
    void setUp() {
        //------------------------------------------//
        //VERY IMPORTANT!!!!
        //DEPENDENCY INJECTION MUST BE DONE MANUALLY
        service = new CustomerService(repo);
        //------------------------------------------//

        customer1 = customerWithName().create();
        customer2 = customerWithName().create();
        customerList = Arrays.asList(customer1,customer2);
    }


    void cleanDbToTest() {
        StepVerifier
                .create(repo.deleteAll())
                .expectSubscription()
                .verifyComplete();

        System.out.println("\n\n==================> CLEAN-DB-TO-TEST" +
                                   " <==================\n\n");
    }


    @Test
    public void save() {
        cleanDbToTest();

        Mono<Customer> customerMono = service.save(customer1);

        StepVerifier
                .create(customerMono)
                .expectSubscription()
                .expectNext(customer1)
                .verifyComplete();
    }


    @Test
    public void deleteAll() {

        StepVerifier
                .create(service.deleteAll())
                .expectSubscription()
                .verifyComplete();

        Flux<Customer> fluxTest = service.findAll();

        StepVerifier
                .create(fluxTest)
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    public void findAll() {

        final Flux<Customer> customerFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(customerList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifier
                .create(customerFlux)
                .expectSubscription()
                .expectNextMatches(customer -> customer1.getEmail()
                                                        .equals(customer.getEmail()))
                .expectNextMatches(customer -> customer2.getEmail()
                                                        .equals(customer.getEmail()))
                .verifyComplete();
    }


    @Test
    public void findAll_Count() {

        final Flux<Customer> customerFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(customerList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifier
                .create(customerFlux)
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
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
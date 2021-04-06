package com.testcontainer.compose;

import com.testcontainer.api.Customer;
import com.testcontainer.api.ICustomerRepo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.testcontainer.databuilder.CustomerBuilder.customerWithName;

@Slf4j
public class ComposeRepo extends ConfigComposeTests {

    private Customer customer1;
    private Customer customer2;
    private List<Customer> customerList;

    @Container
    private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;


    @Autowired
    private ICustomerRepo repo;


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


//    @Disabled
    @Test
    public void findAll_v1() {

        final Flux<Customer> customerFlux =
                repo.deleteAll()
                    .thenMany(Flux.fromIterable(customerList))
                    .flatMap(repo::save)
                    .doOnNext(item -> repo.findAll());

        StepVerifier
                .create(customerFlux)
                .expectSubscription()
                .expectNext(customer1,customer2)
                .verifyComplete();
    }


    @Test
    public void findAll_v2() {

        final Flux<Customer> customerFlux =
                repo.deleteAll()
                    .thenMany(Flux.fromIterable(customerList))
                    .flatMap(repo::save)
                    .doOnNext(item -> repo.findAll());

        StepVerifier
                .create(customerFlux)
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }


    @Test
    public void findAll_v3() {

        final Flux<Customer> customerFlux =
                repo.deleteAll()
                    .thenMany(Flux.fromIterable(customerList))
                    .flatMap(repo::save)
                    .doOnNext(item -> repo.findAll());

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
    public void save() {
        cleanDbToTest();

        StepVerifier
                .create(repo.save(customer1))
                .expectSubscription()
                .expectNext(customer1)
                .verifyComplete();
    }


    @Test
    public void deleteAll() {

        StepVerifier
                .create(repo.deleteAll())
                .expectSubscription()
                .verifyComplete();

        Flux<Customer> fluxTest = repo.findAll();

        StepVerifier
                .create(fluxTest)
                .expectSubscription()
                .expectNextCount(0)
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
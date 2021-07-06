package com.testcontainer.sharedContainer;

import com.testcontainer.api.Customer;
import com.testcontainer.api.CustomerService;
import com.testcontainer.api.ICustomerRepo;
import com.testcontainer.api.ICustomerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.junit.jupiter.api.Assertions.fail;

public class ServiceTests extends ConfigTests {

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;
    private List<Customer> customerList;

    @Autowired
    private ICustomerRepo repo;
    private ICustomerService service;


    @BeforeAll
    static void beforeAll() {
        ConfigTests.beforeAll();
    }


    @AfterAll
    static void afterAll() {
        ConfigTests.afterAll();
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
        customer3 = customerWithName().create();
        customerList = Arrays.asList(customer1,customer2);

        //        //------------------------------------------//
        //        //VERY IMPORTANT!!!!
        //        //DEPENDENCY INJECTION MUST BE DONE MANUALLY
        //        service = new CustomerService(repo);
        //        //------------------------------------------//
        //
        //        customer1 = customerWithName().create();
        //        customer2 = customerWithName().create();
        //        List<Customer> customerList = Arrays.asList(customer1,customer2);
        //
        //        service.deleteAll()
        //               .thenMany(Flux.fromIterable(customerList))
        //               .flatMap(service::save)
        //               .doOnNext(item -> System.out.println(" Inserted item is: " + item))
        //               .blockLast(); // THATS THE WHY, BLOCKHOUND IS NOT BEING USED.
    }


    private void cleanDbToTest() {
        StepVerifier
                .create(service.deleteAll())
                .expectSubscription()
                .verifyComplete();

        System.out.println("\n\n==================> CLEAN-DB-TO-TEST" +
                                   " <==================\n\n");
    }


    @Test
    void checkContainer() {
        assertTrue(container.isRunning());
    }


    @Test
    public void save_obj() {
        cleanDbToTest();

        Mono<Customer> customerMono = service.save(customer3);

        StepVerifier
                .create(customerMono)
                .expectSubscription()
                .expectNext(customer3)
                .verifyComplete();
    }


    @Test
    public void find_count() {

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
    public void find_obj_1() {

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
    public void find_obj_2() {

        final Flux<Customer> customerFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(customerList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifier
                .create(customerFlux)
                .expectNext(customer1)
                .expectNext(customer2)
                .verifyComplete();
    }


    @Test
    public void deleteAll_count() {

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
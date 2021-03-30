package com.testcontainer.container;

import com.testcontainer.api.Customer;
import com.testcontainer.api.CustomerService;
import com.testcontainer.api.ICustomerRepo;
import com.testcontainer.api.ICustomerService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ContainerService extends ConfigContainerTests {

    private Customer cust1, cust2;

    @Autowired
    private ICustomerRepo repo;
    private ICustomerService service;


    @BeforeAll
    static void beforeAll() {
        ConfigContainerTests.beforeAll();
    }


    @AfterAll
    static void afterAll() {
        ConfigContainerTests.afterAll();
    }


    @BeforeEach
    void setUp() {
        //------------------------------------------//
        //VERY IMPORTANT!!!!
        //DEPENDENCY INJECTION MUST BE DONE MANUALLY
        service = new CustomerService(repo);
        //------------------------------------------//

        cust1 = customerWithName().create();
        cust2 = customerWithName().create();
        List<Customer> customerList = Arrays.asList(cust1,cust2);

        service.deleteAll()
               .thenMany(Flux.fromIterable(customerList))
               .flatMap(service::save)
               .doOnNext(item -> System.out.println(" Inserted item is: " + item))
               .blockLast(); // THATS THE WHY, BLOCKHOUND IS NOT BEING USED.
    }


    @Test
    void checkContainer() {
        assertTrue(container.isRunning());
    }


    @Test
    public void save() {
        StepVerifier
                .create(service.save(cust1))
                .expectSubscription()
                .expectNext(cust1)
                .verifyComplete();
    }


    @Test
    public void findAll() {
        StepVerifier
                .create(service.findAll())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }


    @Test
    public void findNextMatches() {
        StepVerifier
                .create(service.findAll())
                .expectNextMatches(u -> u.getId()
                                         .equals(cust1.getId()))
                .expectComplete();
    }


    @Test
    public void findNext() {
        StepVerifier
                .create(service.findAll())
                .expectNext(cust1)
                .expectNext(cust2)
                .expectComplete();
    }


    @Test
    public void deleteAll() {

        StepVerifier
                .create(service.deleteAll())
                .expectSubscription()
                .verifyComplete();

        StepVerifier
                .create(service.findAll())
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
            fail("should fail");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            assertTrue(e.getCause() instanceof BlockingOperationError,"detected");
        }
    }
}
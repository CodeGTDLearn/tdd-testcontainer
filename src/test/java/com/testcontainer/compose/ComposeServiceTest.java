package com.testcontainer.compose;

import com.testcontainer.api.Customer;
import com.testcontainer.api.CustomerService;
import com.testcontainer.api.ICustomerRepo;
import com.testcontainer.api.ICustomerService;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.testcontainer.databuilder.CustomerBuilder.customerWithName;

class ComposeServiceTest extends ComposeConfigTest {

    @Autowired
    private ICustomerRepo repo;

//    @Autowired
    private ICustomerService service;

    private Customer cust1, cust2;
    private List<Customer> customerList;

    static final int COMP_DBPORT = 27017;
    static final String COMP_PATH = "src/test/resources/v2-test-compose.yml";
    static final String COMP_SERVICE = "db";

    @Container
    public static DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(
                    new File(COMP_PATH))
                    .withExposedService(COMP_SERVICE,COMP_DBPORT);


    public String testContainerDbUrl() {
        return "http://" +
                compose.getServiceHost(COMP_SERVICE,COMP_DBPORT) + ":" +
                compose.getServicePort(COMP_SERVICE,COMP_DBPORT);
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
        customerList = Arrays.asList(cust1,cust2);

        System.out.println(testContainerDbUrl());

        service.removeAll()
            .thenMany(Flux.fromIterable(customerList))
            .flatMap(service::save)
            .doOnNext(item -> System.out.println(" Inserted item is: " + item))
            .blockLast(); // THATS THE WHY, BLOCKHOUND IS NOT BEING USED.
    }


    @AfterEach
    void tearDown() {
        service.removeAll();
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
    public void findAllCount() {
        StepVerifier
                .create(service.findAll())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }


    @Test
    public void findAllNextMatches() {
        StepVerifier
                .create(service.findAll())
                .expectNextMatches(u -> u.getId()
                                         .equals(cust1.getId()))
                .expectComplete();
    }


    @Test
    public void findAllNext() {

        StepVerifier
                .create(service.findAll())
                .expectNext(cust1)
                .expectNext(cust2)
                .expectComplete();
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
            Assert.fail("should fail");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Assert.assertTrue("detected",e.getCause() instanceof BlockingOperationError);
        }
    }
}
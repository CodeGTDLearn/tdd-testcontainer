package com.testcontainer.compose;

import com.testcontainer.api.Customer;
import com.testcontainer.api.CustomerService;
import com.testcontainer.api.ICustomerRepo;
import com.testcontainer.api.ICustomerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
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
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

public class ComposeService extends ConfigComposeTests {

    static final int DBPORT = 27017;
    static final String PATH = "src/test/resources/compose-testcontainers.yml";
    static final String SERVICE = "db";

    @Container
    static DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(
                    new File(PATH))
                    .withExposedService(SERVICE,DBPORT);

    private Customer cust1, cust2;
    private List<Customer> customerList;

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

        cust1 = customerWithName().create();
        cust2 = customerWithName().create();
        customerList = Arrays.asList(cust1,cust2);

        service.deleteAll()
               .thenMany(Flux.fromIterable(customerList))
               .flatMap(service::save)
               .doOnNext(item -> System.out.println(" Inserted item is: " + item))
               .blockLast(); // THATS THE WHY, BLOCKHOUND IS NOT BEING USED.
    }


    @AfterEach
    void tearDown() {
        service.deleteAll();
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
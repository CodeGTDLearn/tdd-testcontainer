package com.testcontainer.V1_container;

import com.testcontainer.V1_rieckpil.Customer;
import com.testcontainer.V1_rieckpil.CustomerService;
import com.testcontainer.V1_rieckpil.ICustomerRepo;
import com.testcontainer.V1_rieckpil.ICustomerService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

//TUTORIAL: https://rieckpil.de/mongodb-testcontainers-setup-for-datamongotest/
//@Slf4j
//@Testcontainers
//@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class ServiceTest extends ConfigTest {

    private Customer cust1, cust2;

    @Autowired
    private ICustomerRepo repo;

    private ICustomerService service;

    @Container
    static MongoDBContainer container = new MongoDBContainer("mongo:4.4.2");


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",container::getReplicaSetUrl);
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

        service.removeAll()
               .thenMany(Flux.fromIterable(customerList))
               .flatMap(service::saveCustomer)
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
    public void findCount() {
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
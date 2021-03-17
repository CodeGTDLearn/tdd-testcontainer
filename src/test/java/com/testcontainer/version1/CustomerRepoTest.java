package com.testcontainer.version1;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class CustomerRepoTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private CustomerRepo repo;

    @AfterEach
    void cleanUp() {
        this.repo.deleteAll();
    }

    @Test
    void shouldReturnListOfCustomerWithMatchingRate() {
        Mono<Customer> customer1 = Mono.justOrEmpty(new Customer("mike@spring.io",1));
        Mono<Customer> customer2 = Mono.justOrEmpty(new Customer("duke@spring",42));
        Mono<Customer> customer3 = Mono.justOrEmpty(new Customer("hannah@spring.io",55));

        repo.save(customer1);
        repo.save(customer2);
        repo.save(customer3);

        List<Customer> customers = repo.findByRatingBetween(40,56);

        assertEquals(2,customers.size());
    }
}
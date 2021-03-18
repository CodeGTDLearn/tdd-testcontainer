package com.testcontainer.V1_rieckpil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICustomerService {
    Mono<Customer> save(Customer customer);

    Flux<Customer> findAll();

    Mono<Void> removeAll();

    Mono<Customer> saveCustomer(Customer customer);
}

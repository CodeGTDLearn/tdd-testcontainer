package com.testcontainer.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//TUTORIAL: https://rieckpil.de/mongodb-testcontainers-setup-for-datamongotest/
@Service
@AllArgsConstructor
@Slf4j
public class CustomerService implements ICustomerService {

    private final ICustomerRepo repo;

    @Override
    public Mono<Customer> save(Customer customer) {
        return repo.save(customer);
    }

    @Override
    public Flux<Customer> findAll() {
        return repo.findAll();
    }

    @Override
    public Mono<Void> removeAll(){
        return repo.deleteAll();
    }
}

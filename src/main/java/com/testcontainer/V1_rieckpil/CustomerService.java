package com.testcontainer.V1_rieckpil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//TUTORIAL: https://rieckpil.de/mongodb-testcontainers-setup-for-datamongotest/
@AllArgsConstructor
@Slf4j
@Service
public class CustomerService implements ICustomerService {

    private ICustomerRepo repo;

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

    @Override
    public Mono<Customer> saveCustomer(Customer customer){
        return repo.save(customer);
    }
}

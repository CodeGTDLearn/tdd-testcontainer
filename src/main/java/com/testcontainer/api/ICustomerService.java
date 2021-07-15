package com.testcontainer.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICustomerService {
  Mono<Customer> save(Customer customer);

  Flux<Customer> findAll();

  Mono<Void> deleteAll();

  Flux<Customer> saveList_IfThrowExceptionExecutesTheRollback(List<Customer> customer);

  Flux<Customer> saveAll(List<Customer> customerList);

  Mono<Void>  deleteById(String id);

  Mono<Customer> findById(String id);
}

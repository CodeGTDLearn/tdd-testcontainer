package com.testcontainer.api;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

//TUTORIAL: https://rieckpil.de/mongodb-testcontainers-setup-for-datamongotest/
@Slf4j
@Service
@AllArgsConstructor
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
  public Mono<Void> deleteAll() {
    return repo.deleteAll();
  }


  @Override
  @Transactional
  public Flux<Customer> saveList_IfThrowExceptionExecutesTheRollback(List<Customer> customerList) {

    return repo
         .saveAll(customerList)
         .doOnNext(this::throwResponseStatusExceptionWhenMissingEmail);
  }


  @Override
  public Flux<Customer> saveAll(List<Customer> customerList) {
    return repo.saveAll(customerList);
  }


  @Override
  public Mono<Void> deleteById(String id) {
    return repo.deleteById(id);
  }


  @Override
  public Mono<Customer> findById(String id) {
    return repo.findById(id);
  }


  private void throwResponseStatusExceptionWhenMissingEmail(Customer customer) {
    if (StringUtil.isNullOrEmpty(customer.getEmail()))
      throw new ResponseStatusException(
           HttpStatus.BAD_REQUEST,
           "Email is missing - ROLLBACK DONE!"
      );
  }
}



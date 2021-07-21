package com.testcontainer.api.repository;

import com.testcontainer.api.entity.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRepository extends ReactiveCrudRepository<Customer, String> {

}
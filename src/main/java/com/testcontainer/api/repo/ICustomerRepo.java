package com.testcontainer.api.repo;

import com.testcontainer.api.entity.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICustomerRepo extends ReactiveCrudRepository<Customer, String> {

}
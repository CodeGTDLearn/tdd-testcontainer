package com.testcontainer.V1_rieckpil;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

//TUTORIAL: https://rieckpil.de/mongodb-testcontainers-setup-for-datamongotest/

@Repository
public interface ICustomerRepo extends ReactiveCrudRepository<Customer, String> {

}
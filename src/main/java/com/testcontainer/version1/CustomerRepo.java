package com.testcontainer.version1;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CustomerRepo extends MongoRepository<Customer, String> {
    @Query(sort = "{ rating : 1 }")
    List<Customer> findByRatingBetween(int from,int to);
}
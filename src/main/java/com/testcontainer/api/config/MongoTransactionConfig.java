package com.testcontainer.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;


//https://www.youtube.com/watch?v=9henAE6VUbk&t=364s
@Configuration
//@EnableReactiveMongoRepositories
public class MongoTransactionConfig {

    @Bean
    TransactionalOperator transactionOperator(ReactiveTransactionManager txm) {
        return TransactionalOperator.create(txm);
    }


    @Bean
    ReactiveTransactionManager transactionManager(ReactiveMongoDatabaseFactory dbf) {
        return new ReactiveMongoTransactionManager(dbf);
    }
}

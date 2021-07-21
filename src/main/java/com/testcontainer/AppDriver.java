package com.testcontainer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = {"com.testcontainer.api.repository"})
@EnableTransactionManagement
public class AppDriver {

    //    static {
    //        BlockHound.install(
    //                builder -> builder
    //                        .allowBlockingCallsInside("java.util.UUID","randomUUID")
    //                          );
    //    }


    public static void main(String[] args) {
        SpringApplication.run(AppDriver.class,args);
    }


}

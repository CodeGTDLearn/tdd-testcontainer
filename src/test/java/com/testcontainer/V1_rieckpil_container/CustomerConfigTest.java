package com.testcontainer.V1_rieckpil_container;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;

//AMBOS FUNCIONAM 'DataMongoTest' com ou sem 'excludeAutoConfiguration',
//MAS TUTORIAL RECOMENDA o uso do 'excludeAutoConfiguration'
//@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@DataMongoTest
@Testcontainers
@Slf4j
public class CustomerConfigTest {

}





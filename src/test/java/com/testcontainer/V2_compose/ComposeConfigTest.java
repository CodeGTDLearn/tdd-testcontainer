package com.testcontainer.V2_compose;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;

//AMBOS FUNCIONAM 'DataMongoTest' ou 'SpringBootTest',
// POIS ESTENDEM O '@ExtendWith({SpringExtension.class})'
//@SpringBootTest
@DataMongoTest
@Testcontainers
@Slf4j
public class ComposeConfigTest {

}





package com.testcontainer.compose;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;

//AMBOS FUNCIONAM 'DataMongoTest' ou 'SpringBootTest',
// POIS ESTENDEM O '@ExtendWith({SpringExtension.class})'
//@SpringBootTest
@DataMongoTest
@Testcontainers
@Slf4j
public class ComposeConfigTest {

    @BeforeAll
    static void beforeAll() {
//        BlockHound.install(
                //builder -> builder.allowBlockingCallsInside("java.util.UUID" ,"randomUUID")
//                          );
    }

}





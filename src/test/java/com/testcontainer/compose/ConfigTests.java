package com.testcontainer.compose;

import com.testcontainer.container.ConfigContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

//AMBOS FUNCIONAM 'DataMongoTest' ou 'SpringBootTest',
// POIS ESTENDEM O '@ExtendWith({SpringExtension.class})'
//@SpringBootTest
@DataMongoTest
@Slf4j
public class ConfigTests extends ConfigContainer {

    @BeforeAll
    static void beforeAll() {
        //        BlockHound.install(
        //builder -> builder.allowBlockingCallsInside("java.util.UUID" ,"randomUUID")
        //                          );
    }

}





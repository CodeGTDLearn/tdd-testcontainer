package com.testcontainer.compose;

import com.testcontainer.container.ConfigContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import reactor.blockhound.BlockHound;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

/*------------------------------------------------------------
                         DataMongoTest
  ------------------------------------------------------------
a) AMBOS FUNCIONAM:
 - @DataMongoTest
 - @DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
b) USO ALTERNATIVO (DataMongoTest/SpringBootTest) - DO CONTRARIO CONFLITARAO:
 - @SpringBootTest(webEnvironment = RANDOM_PORT)
  ------------------------------------------------------------*/
@DataMongoTest
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@Slf4j
public class ConfigTests extends ConfigContainer {

    @BeforeAll
    static void beforeAll() {
//                BlockHound.install(
        //builder -> builder.allowBlockingCallsInside("java.util.UUID" ,"randomUUID")
//                                  );
    }

}





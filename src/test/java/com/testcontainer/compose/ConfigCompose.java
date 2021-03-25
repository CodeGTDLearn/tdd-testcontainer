package com.testcontainer.compose;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

@Testcontainers
@Slf4j
public class ConfigCompose {

    static final int COMP_DBPORT = 27017;
    static final String COMP_PATH = "src/test/resources/docker-compose-test.yml";
    static final String COMP_SERVICE = "db";

    @Container
    public static DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(
                    new File(COMP_PATH))
                    .withExposedService(COMP_SERVICE,COMP_DBPORT);

}





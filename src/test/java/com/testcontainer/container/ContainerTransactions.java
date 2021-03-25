package com.testcontainer.container;

import academy.devdojo.webflux.GlobalTestConfig;
import academy.devdojo.webflux.entity.Anime;
import academy.devdojo.webflux.utils.RoleUsersHeaders;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static academy.devdojo.webflux.databuilder.AnimeCreatorBuilder.animeWithName;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;


public class ContainerTransactions extends GlobalTestConfig {

    //WEB-TEST-CLIENT(non-blocking client) WITH MOCK-SERVER
    @Autowired
    private WebTestClient webTestClient;

    private Anime anime_1, anime_2;

    @Before
    public void setUpLocal() {
        anime_1 = animeWithName().create();
        anime_2 = animeWithName().create();

        //REAL-SERVER(non-blocking client)  IN WEB-TEST-CLIENT:
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:8080/animes").build();
    }

    @After
    public void tearDownLocal() {
//        service.delete(anime.getId());
    }

    @Test
    public void saveall_transaction_rollback() {
        List<Anime> listAnime = Arrays.asList(anime_1 ,anime_2);

        RestAssuredWebTestClient
                .given()
                .webTestClient(webTestClient)
                .header("Accept" ,ContentType.ANY)
                .header(RoleUsersHeaders.role_admin_header)
                .body(listAnime)

                .when()
                .post("/saveall_rollback")

                .then()
                .contentType(ContentType.JSON)
                .statusCode(CREATED.value())
                .log().headers().and()
                .log().body().and()

                .body("size()" ,is(listAnime.size()))
                .body("name" ,hasItems(anime_1.getName() ,anime_2.getName()))
        ;
    }

    @Test
    public void saveall_transaction_rollback_ERROR() {
        List<Anime> listAnime = Arrays.asList(anime_1 ,anime_2);

        RestAssuredWebTestClient
                .given()
                .webTestClient(webTestClient)
                .header("Accept" ,ContentType.ANY)
                .header(RoleUsersHeaders.role_admin_header)
                .body(listAnime)

                .when()
                .post("/saveall_rollback")

                .then()
                .contentType(ContentType.JSON)
                .statusCode(BAD_REQUEST.value())
                .log().headers().and()
                .log().body().and()

                .body("developerMensagem" ,is("A ResponseStatusException happened!!!"))
        ;
    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });

            Schedulers.parallel().schedule(task);

            task.get(10 ,TimeUnit.SECONDS);
            Assert.fail("should fail");
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Assert.assertTrue("detected" ,e.getCause() instanceof BlockingOperationError);
        }
    }
}

/* JAMAIS DELETAR!!!!!!!

java.lang.IllegalStateException: You haven't configured a WebTestClient instance. You can do this statically

RestAssuredWebTestClient.mockMvc(..)
RestAssuredWebTestClient.standaloneSetup(..);
RestAssuredWebTestClient.webAppContextSetup(..);

or using the DSL:

given().
		mockMvc(..). ..
 */

//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private WebApplicationContext webApplicationContext;
//    @Test
//    public void get_Webcontext() {
//        //        testClient = WebTestClient.bindToController(controller).build();
//        testClient = WebTestClient.bindToApplicationContext(applicationContext)
//                .configureClient()
//                .build();
//
//        RestAssuredWebTestClient
//                .given()
//                .webTestClient(testClient)
//
//                .when()
//                .get()
//
//                .then()
//                .statusCode(OK.value())
//
//                .body("name" ,hasItem("GLAUCO"))
//        ;
//    }
//
//    @Test
//    public void get_StandAlone() {
////        RestAssuredWebTestClient.standaloneSetup(new AnimeController());
//        RestAssuredWebTestClient.webAppContextSetup(webApplicationContext);
//        RestAssuredWebTestClient
//                .given()
////                .standaloneSetup(new AnimeController())
//
//                .when()
//                .get()
//
//                .then()
//                .statusCode(OK.value())
//
//                .body("name" ,hasItem("paulo"))
//        ;
//    }

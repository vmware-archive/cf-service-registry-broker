package com.mattstine.cf.srb.controller;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.mattstine.cf.srb.repository.StubServiceRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.hasItems;

public class ServiceBrokerControllerTest {

    @Before
    public void setUp() {
        ServiceBrokerController serviceBrokerController = new ServiceBrokerController(new StubServiceRepository());
        RestAssuredMockMvc.standaloneSetup(serviceBrokerController);
    }

    @Test
    public void catalogTest() {
        given()
                .when()
                .get("/v2/catalog").
                then()
                .statusCode(200)
                .body("services.id", hasItems("123-456-789"))
                .body("services.bindable", hasItems(true))
                .body("services.plans.id", hasItems(Arrays.asList("123-456-789")))
                .body("services.plans.name", hasItems(Arrays.asList("Basic")));
    }
}

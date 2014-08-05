package com.gopivotal.cf.srb.controller;

import com.gopivotal.cf.srb.Application;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class ServiceBrokerControllerIntegrationTest {

    @Test
    @Ignore
    public void catalogTest() {
        given()
                .when()
                .auth().basic("warreng", "natedogg")
                .get("/v2/catalog")

                .then().log().all()
                .statusCode(200)

                .body("services.id", hasItems("BD7097C1-F12A-11E3-AC10-0800200C9A66"))
                .body("services.bindable", hasItems(true))
                .body("services.plans.id", hasItems(Arrays.asList("BD7097C0-F12A-11E3-AC10-0800200C9A66")))
                .body("services.plans.name", hasItems(Arrays.asList("only")));
    }
}

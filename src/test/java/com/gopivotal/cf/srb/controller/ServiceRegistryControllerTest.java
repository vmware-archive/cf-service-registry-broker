package com.gopivotal.cf.srb.controller;

import com.gopivotal.cf.srb.Application;
import com.gopivotal.cf.srb.model.*;
import com.gopivotal.cf.srb.repository.RegisteredServiceRepository;
import com.gopivotal.cf.srb.repository.ServiceRepository;
import com.gopivotal.cf.srb.service.ServiceBrokerRegistrationService;
import com.jayway.restassured.http.ContentType;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by pivotal on 8/5/14.
 */
public class ServiceRegistryControllerTest {

    private RegisteredServiceRepository registeredServiceRepository;
    private ServiceRegistryController serviceRegistryController;
    private ServiceRepository serviceRepository;
    private ServiceBrokerRegistrationService serviceBrokerRegistationService;

    private Service service;
    private RegisteredService registeredService;

    @Before
    public void setUp() {
        PlanMetadataCostAmount amount = new PlanMetadataCostAmount();
        amount.setUsd(BigDecimal.ZERO);

        PlanMetadataCost cost = new PlanMetadataCost();
        cost.setAmount(amount);
        cost.setUnit("MONTH");

        PlanMetadata planMetadata = new PlanMetadata();
        planMetadata.addCost(cost);
        planMetadata.setBullets(Arrays.asList("Feature 1", "Feature 2", "Feature 3"));

        Plan plan = new Plan();
        plan.setName("Standard");
        plan.setDescription("Standard Plan");
        plan.setFree(true);
        plan.setMetadata(planMetadata);

        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setDisplayName("A Web Service");
        serviceMetadata.setLongDescription("A wicked cool web service that will provide you with unicorns and rainbows.");
        serviceMetadata.setProviderDisplayName("My Awesome Startup");
        serviceMetadata.setImageUrl(Application.IMAGE_URL_FOR_SERVICE_REGISTRY);

        service = new Service();
        service.setDescription("A wicked cool web service!");
        service.setName("a-web-service");
        service.setBindable(true);
        service.setMetadata(serviceMetadata);
        service.addPlan(plan);

        registeredService = new RegisteredService();
        registeredService.setName("a-web-service");
        registeredService.setDescription("A wicked cool web service!");
        registeredService.setLongDescription("A wicked cool web service that will provide you with unicorns and rainbows.");
        registeredService.setDisplayName("A Web Service");
        registeredService.setProvider("My Awesome Startup");
        registeredService.setFeatures(Arrays.asList("Feature 1", "Feature 2", "Feature 3"));
        registeredService.setUrl("http://my.url.com");
        registeredService.setBasicAuthUser("tupac");
        registeredService.setBasicAuthPassword("makaveli");

        registeredServiceRepository = mock(RegisteredServiceRepository.class);
        serviceRepository = mock(ServiceRepository.class);
        serviceBrokerRegistationService = mock(ServiceBrokerRegistrationService.class);

        serviceRegistryController = new ServiceRegistryController(registeredServiceRepository,
                serviceRepository,
                serviceBrokerRegistationService);
    }

    @Test
    public void testRegister() {
         given()
                 .standaloneSetup(serviceRegistryController)
                 .contentType(ContentType.JSON)
                 .body(registeredService)
                 .when()
                 .post("/registry")
                 .then()
                 .statusCode(201)
                 .body("id", anything());

        verify(registeredServiceRepository).save(registeredService);
        verify(serviceRepository).save(service);
        verify(serviceBrokerRegistationService).registerSelfIdempotent();
    }

    @Test
    public void testUnregister() {
        service.setId("ABCDEF");
        when(registeredServiceRepository.findOne("12345")).thenReturn(registeredService);
        when(serviceRepository.findByName("a-web-service")).thenReturn(service);

        given()
                .standaloneSetup(serviceRegistryController)
                .when()
                .delete("/registry/12345")
                .then()
                .statusCode(200)
                .assertThat().body(equalTo("{}"));

        verify(registeredServiceRepository).delete("12345");
        verify(serviceRepository).delete("ABCDEF");
        verify(serviceBrokerRegistationService).registerSelfIdempotent();
    }
}

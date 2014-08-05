package com.gopivotal.cf.srb.controller;

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
import static org.mockito.Mockito.mock;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.Mockito.verify;


/**
 * Created by pivotal on 8/5/14.
 */
public class ServiceRegistryControllerTest {

    private RegisteredServiceRepository registeredServiceRepository;
    private ServiceRegistryController serviceRegistryController;
    private ServiceRepository serviceRepository;
    private ServiceBrokerRegistrationService serviceBrokerRegistationService;

    @Before
    public void setUp() {
        registeredServiceRepository = mock(RegisteredServiceRepository.class);
        serviceRepository = mock(ServiceRepository.class);
        serviceBrokerRegistationService = mock(ServiceBrokerRegistrationService.class);

        serviceRegistryController = new ServiceRegistryController(registeredServiceRepository,
                serviceRepository,
                serviceBrokerRegistationService);
    }

    @Test
    public void testRegister() {
        RegisteredService requestBody = new RegisteredService();
        requestBody.setName("a-web-service");
        requestBody.setDescription("A wicked cool web service!");
        requestBody.setLongDescription("A wicked cool web service that will provide you with unicorns and rainbows.");
        requestBody.setDisplayName("A Web Service");
        requestBody.setProvider("My Awesome Startup");
        requestBody.setFeatures(Arrays.asList("Feature 1", "Feature 2", "Feature 3"));
        requestBody.setUrl("http://my.url.com");
        requestBody.setBasicAuthUser("tupac");
        requestBody.setBasicAuthPassword("makaveli");

         given()
                 .standaloneSetup(serviceRegistryController)
                 .contentType(ContentType.JSON)
                 .body(requestBody)
                 .when()
                 .post("/registry")
                 .then()
                 .statusCode(201)
                 .body("id", anything());

        PlanMetadataCostAmount amount = new PlanMetadataCostAmount();
        amount.setUsd(BigDecimal.ZERO);

        PlanMetadataCost cost = new PlanMetadataCost();
        cost.setAmount(amount);
        cost.setUnit("MONTH");

        PlanMetadata planMetadata = new PlanMetadata();
        planMetadata.addCost(cost);
        planMetadata.setBullets(requestBody.getFeatures());

        Plan plan = new Plan();
        plan.setName("Standard");
        plan.setDescription("Standard Plan");
        plan.setFree(true);
        plan.setMetadata(planMetadata);

        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setDisplayName(requestBody.getDisplayName());
        serviceMetadata.setLongDescription(requestBody.getLongDescription());
        serviceMetadata.setProviderDisplayName(requestBody.getProvider());
        serviceMetadata.setImageUrl("data:image/png;base64,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");

        Service service = new Service();
        service.setDescription(requestBody.getDescription());
        service.setName(requestBody.getName());
        service.setBindable(true);
        service.setMetadata(serviceMetadata);
        service.addPlan(plan);

        verify(registeredServiceRepository).save(requestBody);
        verify(serviceRepository).save(service);
        verify(serviceBrokerRegistationService).registerSelfIdempotent();
    }
}

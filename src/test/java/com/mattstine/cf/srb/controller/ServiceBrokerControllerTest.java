package com.mattstine.cf.srb.controller;

import com.jayway.restassured.http.ContentType;
import com.mattstine.cf.srb.model.Plan;
import com.mattstine.cf.srb.model.Service;
import com.mattstine.cf.srb.model.ServiceInstance;
import com.mattstine.cf.srb.repository.ServiceInstanceRepository;
import com.mattstine.cf.srb.repository.ServiceRepository;
import com.mattstine.cf.srb.repository.StubServiceInstanceRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceBrokerControllerTest {

    private ServiceBrokerController serviceBrokerController;
    private ServiceInstanceRepository serviceInstanceRepository;


    @Before
    public void setUp() {
        Service service = new Service();
        service.setId("123-456-789");
        service.setName("HaaSh");
        service.setBindable(true);
        service.setDescription("HashMap as a Service");

        Plan plan = new Plan();
        plan.setId("123-456-789");
        plan.setName("Basic");
        plan.setDescription("Basic Plan");
        service.addPlan(plan);

        List services = Arrays.asList(service);

        ServiceRepository serviceRepository = mock(ServiceRepository.class);
        when(serviceRepository.findAll()).thenReturn(services);

        serviceInstanceRepository = mock(ServiceInstanceRepository.class);

        serviceBrokerController = new ServiceBrokerController(
                serviceRepository,
                serviceInstanceRepository);
    }

    @Test
    public void catalogTest() {
        given()
                .standaloneSetup(serviceBrokerController)
                .when()
                .get("/v2/catalog").
                then()
                .statusCode(200)
                .body("services.id", hasItems("123-456-789"))
                .body("services.bindable", hasItems(true))
                .body("services.plans.id", hasItems(Arrays.asList("123-456-789")))
                .body("services.plans.name", hasItems(Arrays.asList("Basic")));
    }

    @Test
    public void createTest() {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setPlanId("12345");
        serviceInstance.setOrganizationGuid("12345");
        serviceInstance.setSpaceGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(serviceInstance)
                .when()
                .put("/v2/service_instances/12345")
                .then()
                .statusCode(201)
                .assertThat().body(equalTo("{}"));

         verify(serviceInstanceRepository).exists("12345");
    }
}

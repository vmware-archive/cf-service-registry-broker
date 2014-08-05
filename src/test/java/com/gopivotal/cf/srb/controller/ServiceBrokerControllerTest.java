package com.gopivotal.cf.srb.controller;

import com.jayway.restassured.http.ContentType;
import com.gopivotal.cf.srb.model.*;
import com.gopivotal.cf.srb.repository.ServiceBindingRepository;
import com.gopivotal.cf.srb.repository.ServiceInstanceRepository;
import com.gopivotal.cf.srb.repository.ServiceRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.app.ApplicationInstanceInfo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.Mockito.*;

public class ServiceBrokerControllerTest {

    private ServiceBrokerController serviceBrokerController;
    private ServiceInstanceRepository serviceInstanceRepository;
    private ServiceBindingRepository serviceBindingRepository;

    @Before
    public void setUp() {
        ApplicationInstanceInfo applicationInstanceInfo = new ApplicationInstanceInfo() {
            @Override
            public String getInstanceId() {
                return null;
            }

            @Override
            public String getAppId() {
                return null;
            }

            @Override
            public Map<String, Object> getProperties() {
                Map<String, Object> properties = new HashMap();
                properties.put("uris", Arrays.asList("my.uri.com"));
                return properties;
            }
        };

        Cloud cloud = mock(Cloud.class);
        when(cloud.getApplicationInstanceInfo()).thenReturn(applicationInstanceInfo);

        Service service = new Service();
        service.setId("123-456-789");
        service.setName("HaaSh");
        service.setBindable(true);
        service.setDescription("HashMap as a Service");

        PlanMetadataCostAmount amount = new PlanMetadataCostAmount();
        amount.setUsd(BigDecimal.ZERO);

        PlanMetadataCost cost = new PlanMetadataCost();
        cost.setAmount(amount);
        cost.setUnit("USD");

        PlanMetadata planMetadata = new PlanMetadata();
        planMetadata.addCost(cost);
        planMetadata.setBullets(Arrays.asList("Feature 1", "Feature 2", "Feature 3"));

        Plan plan = new Plan();
        plan.setId("123-456-789");
        plan.setName("Basic");
        plan.setDescription("Basic Plan");
        plan.setMetadata(planMetadata);


        service.addPlan(plan);

        List services = Arrays.asList(service);

        ServiceRepository serviceRepository = mock(ServiceRepository.class);
        when(serviceRepository.findAll()).thenReturn(services);

        serviceInstanceRepository = mock(ServiceInstanceRepository.class);
        serviceBindingRepository = mock(ServiceBindingRepository.class);

        serviceBrokerController = new ServiceBrokerController(
                cloud,
                serviceRepository,
                serviceInstanceRepository,
                serviceBindingRepository);
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
        ServiceInstance requestBody = new ServiceInstance();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setOrganizationGuid("12345");
        requestBody.setSpaceGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345")
                .then()
                .statusCode(201)
                .assertThat().body(equalTo("{}"));

        verify(serviceInstanceRepository).exists("12345");

        ServiceInstance newServiceInstance = new ServiceInstance();
        newServiceInstance.setId("12345");
        newServiceInstance.setServiceId("12345");
        newServiceInstance.setPlanId("12345");
        newServiceInstance.setOrganizationGuid("12345");
        newServiceInstance.setSpaceGuid("12345");

        verify(serviceInstanceRepository).save(newServiceInstance);
    }

    @Test
    public void createWithConflict() {
        ServiceInstance existingServiceInstance = new ServiceInstance();
        existingServiceInstance.setId("12345");
        existingServiceInstance.setServiceId("12345");
        existingServiceInstance.setPlanId("12345");
        existingServiceInstance.setOrganizationGuid("12345");
        existingServiceInstance.setSpaceGuid("something different");

        when(serviceInstanceRepository.exists("12345")).thenReturn(true);
        when(serviceInstanceRepository.findOne("12345")).thenReturn(existingServiceInstance);

        ServiceInstance requestBody = new ServiceInstance();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setOrganizationGuid("12345");
        requestBody.setSpaceGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345")
                .then()
                .statusCode(409)
                .assertThat().body(equalTo("{}"));
    }

    @Test
    public void createWithCopy() {
        ServiceInstance existingServiceInstance = new ServiceInstance();
        existingServiceInstance.setId("12345");
        existingServiceInstance.setServiceId("12345");
        existingServiceInstance.setPlanId("12345");
        existingServiceInstance.setOrganizationGuid("12345");
        existingServiceInstance.setSpaceGuid("12345");

        when(serviceInstanceRepository.exists("12345")).thenReturn(true);
        when(serviceInstanceRepository.findOne("12345")).thenReturn(existingServiceInstance);

        ServiceInstance requestBody = new ServiceInstance();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setOrganizationGuid("12345");
        requestBody.setSpaceGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345")
                .then()
                .statusCode(200)
                .assertThat().body(equalTo("{}"));
    }

    @Test
    public void createBinding() {
        when(serviceInstanceRepository.exists("12345")).thenReturn(true);
        when(serviceBindingRepository.exists("12345")).thenReturn(false);

        ServiceBinding requestBody = new ServiceBinding();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setAppGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345/service_bindings/12345")
                .then()
                .statusCode(201)
                .assertThat().body("credentials.uri", equalTo("http://my.uri.com/registry/12345"))
                .body("credentials.username", equalTo("warreng"))
                .body("credentials.password", equalTo("natedogg"));

        Credentials newCredentials = new Credentials();
        newCredentials.setUri("http://my.uri.com/HaaSh/12345");
        newCredentials.setUsername("warreng");
        newCredentials.setPassword("natedogg");

        ServiceBinding newServiceBinding = new ServiceBinding();
        newServiceBinding.setId("12345");
        newServiceBinding.setInstanceId("12345");
        newServiceBinding.setServiceId("12345");
        newServiceBinding.setPlanId("12345");
        newServiceBinding.setAppGuid("12345");
        newServiceBinding.setCredentials(newCredentials);

        verify(serviceBindingRepository).save(newServiceBinding);
    }

    @Test
    public void createBindingWithConflict() {
        ServiceBinding existingServiceBinding = new ServiceBinding();
        existingServiceBinding.setId("12345");
        existingServiceBinding.setInstanceId("12345");
        existingServiceBinding.setServiceId("12345");
        existingServiceBinding.setPlanId("12345");
        existingServiceBinding.setAppGuid("something different");

        Credentials existingCredentials = new Credentials();
        existingCredentials.setId("12345");
        existingCredentials.setUri("http://my.uri.com/HaaSh/12345");
        existingCredentials.setUsername("warreng");
        existingCredentials.setPassword("natedogg");
        existingServiceBinding.setCredentials(existingCredentials);

        when(serviceInstanceRepository.exists("12345")).thenReturn(true);
        when(serviceBindingRepository.exists("12345")).thenReturn(true);
        when(serviceBindingRepository.findOne("12345")).thenReturn(existingServiceBinding);

        ServiceBinding requestBody = new ServiceBinding();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setAppGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345/service_bindings/12345")
                .then()
                .statusCode(409)
                .assertThat().body(equalTo("{}"));
    }

    @Test
    public void createBindingWithCopy() {
        ServiceBinding existingServiceBinding = new ServiceBinding();
        existingServiceBinding.setId("12345");
        existingServiceBinding.setInstanceId("12345");
        existingServiceBinding.setServiceId("12345");
        existingServiceBinding.setPlanId("12345");
        existingServiceBinding.setAppGuid("12345");

        Credentials existingCredentials = new Credentials();
        existingCredentials.setId("12345");
        existingCredentials.setUri("http://my.uri.com/HaaSh/12345");
        existingCredentials.setUsername("warreng");
        existingCredentials.setPassword("natedogg");
        existingServiceBinding.setCredentials(existingCredentials);

        when(serviceInstanceRepository.exists("12345")).thenReturn(true);
        when(serviceBindingRepository.exists("12345")).thenReturn(true);
        when(serviceBindingRepository.findOne("12345")).thenReturn(existingServiceBinding);

        ServiceBinding requestBody = new ServiceBinding();
        requestBody.setServiceId("12345");
        requestBody.setPlanId("12345");
        requestBody.setAppGuid("12345");

        given()
                .standaloneSetup(serviceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/12345/service_bindings/12345")
                .then()
                .statusCode(200)
                .assertThat().body("credentials.uri", equalTo("http://my.uri.com/HaaSh/12345"))
                .body("credentials.username", equalTo("warreng"))
                .body("credentials.password", equalTo("natedogg"));
    }

    @Test
    public void deleteExistingBinding() {
        when(serviceBindingRepository.exists("12345")).thenReturn(true);

        given()
                .standaloneSetup(serviceBrokerController)
                .queryParam("service_id", "12345")
                .queryParam("plan_id", "12345")
                .when()
                .delete("/v2/service_instances/12345/service_bindings/12345")
                .then()
                .statusCode(200)
                .assertThat().body(equalTo("{}"));

        verify(serviceBindingRepository).delete("12345");
    }

    @Test
    public void deleteMissingBinding() {
        when(serviceBindingRepository.exists("12345")).thenReturn(false);

        given()
                .standaloneSetup(serviceBrokerController)
                .queryParam("service_id", "12345")
                .queryParam("plan_id", "12345")
                .when()
                .delete("/v2/service_instances/12345/service_bindings/12345")
                .then()
                .statusCode(410)
                .assertThat().body(equalTo("{}"));
    }

    @Test
    public void deleteExisting() {
        when(serviceInstanceRepository.exists("12345")).thenReturn(true);

        given()
                .standaloneSetup(serviceBrokerController)
                .queryParam("service_id", "12345")
                .queryParam("plan_id", "12345")
                .when()
                .delete("/v2/service_instances/12345")
                .then()
                .statusCode(200)
                .assertThat().body(equalTo("{}"));

        verify(serviceInstanceRepository).delete("12345");
    }

    @Test
    public void deleteMissing() {
        when(serviceInstanceRepository.exists("12345")).thenReturn(false);

        given()
                .standaloneSetup(serviceBrokerController)
                .queryParam("service_id", "12345")
                .queryParam("plan_id", "12345")
                .when()
                .delete("/v2/service_instances/12345")
                .then()
                .statusCode(410)
                .assertThat().body(equalTo("{}"));
    }
}

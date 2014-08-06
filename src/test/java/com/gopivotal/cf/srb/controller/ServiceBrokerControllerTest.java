package com.gopivotal.cf.srb.controller;

import com.gopivotal.cf.srb.Application;
import com.gopivotal.cf.srb.model.*;
import com.gopivotal.cf.srb.repository.RegisteredServiceRepository;
import com.gopivotal.cf.srb.repository.ServiceBindingRepository;
import com.gopivotal.cf.srb.repository.ServiceInstanceRepository;
import com.gopivotal.cf.srb.repository.ServiceRepository;
import com.jayway.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.Mockito.*;

public class ServiceBrokerControllerTest {

    private static final String UNIVERSAL_DUMMY_ID = "12345";

    private static final String REGISTERED_SERVICE_URL = "http://my.url.com";
    private static final String REGISTERED_SERVICE_USER = "tupac";
    private static final String REGISTERED_SERVICE_PASSWORD = "makaveli";

    public static final int STATUS_200_OK = 200;
    public static final int STATUS_201_CREATED = 201;
    public static final int STATUS_409_CONFLICT = 409;
    public static final int STATUS_410_GONE = 410;

    private static final String EMPTY_RESPONSE_BODY = "{}";

    private static final String DUMMY_SERVICE_DISPLAY_NAME = "A Web Service";
    private static final String DUMMY_SERVICE_LONG_DESCRIPTION = "A wicked cool web service that will provide you with unicorns and rainbows.";
    private static final String DUMMY_SERVICE_PROVIDER = "My Awesome Startup";
    private static final String DUMMY_SERVICE_NAME = "a-web-service";
    private static final String DUMMY_SERVICE_DESCRIPTION = "A wicked cool web service!";
    private static final String DUMMY_SERVICE_PLAN_NAME = "Basic";
    private static final String DUMMY_SERVICE_PLAN_DESCRIPTION = "Basic Plan";
    private static final List<String> DUMMY_SERVICE_PLAN_METADATA_BULLETS = Arrays.asList("Feature 1", "Feature 2", "Feature 3");
    private static final String DUMMY_SERVICE_PLAN_METADATA_COST_UNIT = "MONTH";
    public static final String SERVICE_METADATA_CONFLICT = "something different";

    private ServiceBrokerController mockServiceBrokerController;
    private ServiceRepository mockServiceRepository;
    private ServiceInstanceRepository mockServiceInstanceRepository;
    private ServiceBindingRepository mockServiceBindingRepository;
    private RegisteredServiceRepository mockRegisteredServiceRepository;

    private Service dummyService;

    @Before
    public void setUp() {
        prepareMockServiceData();
        prepareMockServiceRepository();

        mockServiceInstanceRepository = mock(ServiceInstanceRepository.class);
        mockServiceBindingRepository = mock(ServiceBindingRepository.class);
        mockRegisteredServiceRepository = mock(RegisteredServiceRepository.class);

        mockServiceBrokerController = new ServiceBrokerController(
                mockServiceRepository,
                mockServiceInstanceRepository,
                mockServiceBindingRepository,
                mockRegisteredServiceRepository);
    }

    private void prepareMockServiceData() {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setDisplayName(DUMMY_SERVICE_DISPLAY_NAME);
        serviceMetadata.setLongDescription(DUMMY_SERVICE_LONG_DESCRIPTION);
        serviceMetadata.setProviderDisplayName(DUMMY_SERVICE_PROVIDER);
        serviceMetadata.setImageUrl(Application.IMAGE_URL_FOR_SERVICE_REGISTRY);

        dummyService = new Service();
        dummyService.setId(UNIVERSAL_DUMMY_ID);
        dummyService.setName(DUMMY_SERVICE_NAME);
        dummyService.setBindable(true);
        dummyService.setDescription(DUMMY_SERVICE_DESCRIPTION);
        dummyService.setMetadata(serviceMetadata);

        PlanMetadataCostAmount amount = new PlanMetadataCostAmount();
        amount.setUsd(BigDecimal.ZERO);

        PlanMetadataCost cost = new PlanMetadataCost();
        cost.setAmount(amount);
        cost.setUnit(DUMMY_SERVICE_PLAN_METADATA_COST_UNIT);

        PlanMetadata planMetadata = new PlanMetadata();
        planMetadata.addCost(cost);
        planMetadata.setBullets(DUMMY_SERVICE_PLAN_METADATA_BULLETS);

        Plan plan = new Plan();
        plan.setId(UNIVERSAL_DUMMY_ID);
        plan.setName(DUMMY_SERVICE_PLAN_NAME);
        plan.setDescription(DUMMY_SERVICE_PLAN_DESCRIPTION);
        plan.setMetadata(planMetadata);

        dummyService.addPlan(plan);
    }

    private void prepareMockServiceRepository() {
        List services = Arrays.asList(dummyService);
        mockServiceRepository = mock(ServiceRepository.class);
        when(mockServiceRepository.findAll()).thenReturn(services);
    }

    @Test
    public void catalogTest() {
        given()
                .standaloneSetup(mockServiceBrokerController)
                .when()
                .get("/v2/catalog").
                then()
                .statusCode(200)
                .body("services.id", hasItems(UNIVERSAL_DUMMY_ID))
                .body("services.name", hasItems(DUMMY_SERVICE_NAME))
                .body("services.description", hasItems(DUMMY_SERVICE_DESCRIPTION))
                .body("services.metadata.providerDisplayName", hasItems(DUMMY_SERVICE_PROVIDER))
                .body("services.bindable", hasItems(true))
                .body("services.plans.id", hasItems(Arrays.asList(UNIVERSAL_DUMMY_ID)))
                .body("services.plans.name", hasItems(Arrays.asList(DUMMY_SERVICE_PLAN_NAME)));
    }

    @Test
    public void createTest() {
        given()
                .standaloneSetup(mockServiceBrokerController)
                .contentType(ContentType.JSON)
                .body(prepareServiceInstanceRequestBody())
                .when()
                .put("/v2/service_instances/{service_instance_id}", UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_201_CREATED)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));

        verify(mockServiceInstanceRepository).exists(UNIVERSAL_DUMMY_ID);

        ServiceInstance newServiceInstance = new ServiceInstance();
        newServiceInstance.setId(UNIVERSAL_DUMMY_ID);
        newServiceInstance.setServiceId(UNIVERSAL_DUMMY_ID);
        newServiceInstance.setPlanId(UNIVERSAL_DUMMY_ID);
        newServiceInstance.setOrganizationGuid(UNIVERSAL_DUMMY_ID);
        newServiceInstance.setSpaceGuid(UNIVERSAL_DUMMY_ID);

        verify(mockServiceInstanceRepository).save(newServiceInstance);
    }

    @Test
    public void createWithConflict() {
        prepareExistingServiceInstance(true);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .contentType(ContentType.JSON)
                .body(prepareServiceInstanceRequestBody())
                .when()
                .put("/v2/service_instances/{service_instance_id}", UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_409_CONFLICT)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));
    }

    @Test
    public void createWithCopy() {
        prepareExistingServiceInstance(false);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .contentType(ContentType.JSON)
                .body(prepareServiceInstanceRequestBody())
                .when()
                .put("/v2/service_instances/{service_instance_id}", UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_200_OK)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));
    }

    private ServiceInstance prepareServiceInstanceRequestBody() {
        ServiceInstance requestBody = new ServiceInstance();
        requestBody.setServiceId(UNIVERSAL_DUMMY_ID);
        requestBody.setPlanId(UNIVERSAL_DUMMY_ID);
        requestBody.setOrganizationGuid(UNIVERSAL_DUMMY_ID);
        requestBody.setSpaceGuid(UNIVERSAL_DUMMY_ID);
        return requestBody;
    }

    private void prepareExistingServiceInstance(boolean withConflict) {
        ServiceInstance existingServiceInstance = new ServiceInstance();
        existingServiceInstance.setId(UNIVERSAL_DUMMY_ID);
        existingServiceInstance.setServiceId(UNIVERSAL_DUMMY_ID);
        existingServiceInstance.setPlanId(UNIVERSAL_DUMMY_ID);
        existingServiceInstance.setOrganizationGuid(UNIVERSAL_DUMMY_ID);

        if (withConflict) {
            existingServiceInstance.setSpaceGuid(SERVICE_METADATA_CONFLICT);
        } else {
            existingServiceInstance.setSpaceGuid(UNIVERSAL_DUMMY_ID);
        }

        when(mockServiceInstanceRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);
        when(mockServiceInstanceRepository.findOne(UNIVERSAL_DUMMY_ID)).thenReturn(existingServiceInstance);
    }

    @Test
    public void createBinding() {
        RegisteredService registeredService = new RegisteredService();
        registeredService.setName(DUMMY_SERVICE_NAME);
        registeredService.setDescription(DUMMY_SERVICE_PLAN_DESCRIPTION);
        registeredService.setLongDescription(DUMMY_SERVICE_LONG_DESCRIPTION);
        registeredService.setDisplayName(DUMMY_SERVICE_DISPLAY_NAME);
        registeredService.setProvider(DUMMY_SERVICE_PROVIDER);
        registeredService.setFeatures(DUMMY_SERVICE_PLAN_METADATA_BULLETS);
        registeredService.setUrl(REGISTERED_SERVICE_URL);
        registeredService.setBasicAuthUser(REGISTERED_SERVICE_USER);
        registeredService.setBasicAuthPassword(REGISTERED_SERVICE_PASSWORD);

        when(mockServiceInstanceRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);
        when(mockServiceBindingRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(false);
        when(mockServiceRepository.findOne(UNIVERSAL_DUMMY_ID)).thenReturn(dummyService);
        when(mockRegisteredServiceRepository.findByName(dummyService.getName())).thenReturn(registeredService);

        ServiceBinding requestBody = new ServiceBinding();
        requestBody.setServiceId(UNIVERSAL_DUMMY_ID);
        requestBody.setPlanId(UNIVERSAL_DUMMY_ID);
        requestBody.setAppGuid(UNIVERSAL_DUMMY_ID);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/{service_instance_id}/service_bindings/{service_binding_id}",
                        UNIVERSAL_DUMMY_ID,
                        UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_201_CREATED)
                .assertThat().body("credentials.uri", equalTo(REGISTERED_SERVICE_URL))
                .body("credentials.username", equalTo(REGISTERED_SERVICE_USER))
                .body("credentials.password", equalTo(REGISTERED_SERVICE_PASSWORD));

        Credentials newCredentials = new Credentials();
        newCredentials.setUri(REGISTERED_SERVICE_URL);
        newCredentials.setUsername(REGISTERED_SERVICE_USER);
        newCredentials.setPassword(REGISTERED_SERVICE_PASSWORD);

        ServiceBinding newServiceBinding = new ServiceBinding();
        newServiceBinding.setId(UNIVERSAL_DUMMY_ID);
        newServiceBinding.setInstanceId(UNIVERSAL_DUMMY_ID);
        newServiceBinding.setServiceId(UNIVERSAL_DUMMY_ID);
        newServiceBinding.setPlanId(UNIVERSAL_DUMMY_ID);
        newServiceBinding.setAppGuid(UNIVERSAL_DUMMY_ID);
        newServiceBinding.setCredentials(newCredentials);

        verify(mockServiceBindingRepository).save(newServiceBinding);
    }

    @Test
    public void createBindingWithConflict() {
        ServiceBinding existingServiceBinding = prepareExistingServiceBinding(true);

        when(mockServiceInstanceRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);
        when(mockServiceBindingRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);
        when(mockServiceBindingRepository.findOne(UNIVERSAL_DUMMY_ID)).thenReturn(existingServiceBinding);

        ServiceBinding requestBody = new ServiceBinding();
        requestBody.setServiceId(UNIVERSAL_DUMMY_ID);
        requestBody.setPlanId(UNIVERSAL_DUMMY_ID);
        requestBody.setAppGuid(UNIVERSAL_DUMMY_ID);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/{service_instance_id}/service_bindings/{service_binding_id}",
                        UNIVERSAL_DUMMY_ID,
                        UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_409_CONFLICT)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));
    }

    @Test
    public void createBindingWithCopy() {
        ServiceBinding existingServiceBinding = prepareExistingServiceBinding(false);

        when(mockServiceInstanceRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);
        when(mockServiceBindingRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);
        when(mockServiceBindingRepository.findOne(UNIVERSAL_DUMMY_ID)).thenReturn(existingServiceBinding);

        ServiceBinding requestBody = new ServiceBinding();
        requestBody.setServiceId(UNIVERSAL_DUMMY_ID);
        requestBody.setPlanId(UNIVERSAL_DUMMY_ID);
        requestBody.setAppGuid(UNIVERSAL_DUMMY_ID);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .put("/v2/service_instances/{service_instance_id}/service_bindings/{service_binding_id}",
                        UNIVERSAL_DUMMY_ID,
                        UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_200_OK)
                .assertThat().body("credentials.uri", equalTo(REGISTERED_SERVICE_URL))
                .body("credentials.username", equalTo(REGISTERED_SERVICE_USER))
                .body("credentials.password", equalTo(REGISTERED_SERVICE_PASSWORD));
    }

    private ServiceBinding prepareExistingServiceBinding(boolean withConflict) {
        ServiceBinding existingServiceBinding = new ServiceBinding();
        existingServiceBinding.setId(UNIVERSAL_DUMMY_ID);
        existingServiceBinding.setInstanceId(UNIVERSAL_DUMMY_ID);
        existingServiceBinding.setServiceId(UNIVERSAL_DUMMY_ID);
        existingServiceBinding.setPlanId(UNIVERSAL_DUMMY_ID);
        if (withConflict) {
            existingServiceBinding.setAppGuid(SERVICE_METADATA_CONFLICT);
        } else {
            existingServiceBinding.setAppGuid(UNIVERSAL_DUMMY_ID);
        }

        Credentials existingCredentials = new Credentials();
        existingCredentials.setId(UNIVERSAL_DUMMY_ID);
        existingCredentials.setUri(REGISTERED_SERVICE_URL);
        existingCredentials.setUsername(REGISTERED_SERVICE_USER);
        existingCredentials.setPassword(REGISTERED_SERVICE_PASSWORD);
        existingServiceBinding.setCredentials(existingCredentials);
        return existingServiceBinding;
    }

    @Test
    public void deleteExistingBinding() {
        when(mockServiceBindingRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .queryParam("service_id", UNIVERSAL_DUMMY_ID)
                .queryParam("plan_id", UNIVERSAL_DUMMY_ID)
                .when()
                .delete("/v2/service_instances/{service_instance_id}/service_bindings/{service_binding_id}",
                        UNIVERSAL_DUMMY_ID,
                        UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_200_OK)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));

        verify(mockServiceBindingRepository).delete(UNIVERSAL_DUMMY_ID);
    }

    @Test
    public void deleteMissingBinding() {
        when(mockServiceBindingRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(false);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .queryParam("service_id", UNIVERSAL_DUMMY_ID)
                .queryParam("plan_id", UNIVERSAL_DUMMY_ID)
                .when()
                .delete("/v2/service_instances/{service_instance_id}/service_bindings/{service_binding_id}",
                        UNIVERSAL_DUMMY_ID,
                        UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_410_GONE)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));
    }

    @Test
    public void deleteExisting() {
        when(mockServiceInstanceRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .queryParam("service_id", UNIVERSAL_DUMMY_ID)
                .queryParam("plan_id", UNIVERSAL_DUMMY_ID)
                .when()
                .delete("/v2/service_instances/{service_instance_id}", UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_200_OK)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));

        verify(mockServiceInstanceRepository).delete(UNIVERSAL_DUMMY_ID);
    }

    @Test
    public void deleteMissing() {
        when(mockServiceInstanceRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(false);

        given()
                .standaloneSetup(mockServiceBrokerController)
                .queryParam("service_id", UNIVERSAL_DUMMY_ID)
                .queryParam("plan_id", UNIVERSAL_DUMMY_ID)
                .when()
                .delete("/v2/service_instances/{service_instance_id}", UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_410_GONE)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));
    }
}

package com.gopivotal.cf.srb.controller;

import com.gopivotal.cf.srb.model.*;
import com.gopivotal.cf.srb.repository.RegisteredServiceRepository;
import com.gopivotal.cf.srb.repository.ServiceBindingRepository;
import com.gopivotal.cf.srb.repository.ServiceInstanceRepository;
import com.gopivotal.cf.srb.repository.ServiceRepository;
import com.gopivotal.cf.srb.service.ServiceBrokerRegistrationService;
import com.jayway.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.gopivotal.cf.srb.controller.TestDataUtil.*;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.Mockito.*;

public class ServiceBrokerControllerTest {

    public static final String SERVICE_METADATA_CONFLICT = "something different";

    private ServiceBrokerController mockServiceBrokerController;
    private ServiceRepository mockServiceRepository;
    private ServiceInstanceRepository mockServiceInstanceRepository;
    private ServiceBindingRepository mockServiceBindingRepository;
    private RegisteredServiceRepository mockRegisteredServiceRepository;
    private ServiceBrokerRegistrationService mockServiceBrokerRegistrationService;

    private Service dummyService;

    @Before
    public void setUp() {
        dummyService = prepareMockServiceData();
        prepareMockServiceRepository();

        mockServiceInstanceRepository = mock(ServiceInstanceRepository.class);
        mockServiceBindingRepository = mock(ServiceBindingRepository.class);
        mockRegisteredServiceRepository = mock(RegisteredServiceRepository.class);
        mockServiceBrokerRegistrationService = mock(ServiceBrokerRegistrationService.class);

        mockServiceBrokerController = new ServiceBrokerController(
                mockServiceRepository,
                mockServiceInstanceRepository,
                mockServiceBindingRepository,
                mockRegisteredServiceRepository,
                mockServiceBrokerRegistrationService);
    }

    private void prepareMockServiceRepository() {
        List<Service> services = Arrays.asList(dummyService);
        mockServiceRepository = mock(ServiceRepository.class);
        when(mockServiceRepository.findAll()).thenReturn(services);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void catalogTest() {
        given()
                .standaloneSetup(mockServiceBrokerController)
                .when()
                .get("/v2/catalog").
                then()
                .statusCode(STATUS_200_OK)
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
        RegisteredService registeredService = prepareRegisteredService();

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
    public void createBindingForServiceRegistry() {
        when(mockServiceInstanceRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(true);
        when(mockServiceBindingRepository.exists(UNIVERSAL_DUMMY_ID)).thenReturn(false);

        dummyService.setName("service-registry");
        when(mockServiceRepository.findOne(UNIVERSAL_DUMMY_ID)).thenReturn(dummyService);
        when(mockServiceBrokerRegistrationService.firstRoute()).thenReturn("my.url.com");

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
                .assertThat().body("credentials.uri", equalTo("http://my.url.com/registry"))
                .body("credentials.username", equalTo("warreng"))
                .body("credentials.password", equalTo("natedogg"));

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

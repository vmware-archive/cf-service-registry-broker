package com.gopivotal.cf.srb.controller;

import com.gopivotal.cf.srb.model.RegisteredService;
import com.gopivotal.cf.srb.model.Service;
import com.gopivotal.cf.srb.repository.RegisteredServiceRepository;
import com.gopivotal.cf.srb.repository.ServiceRepository;
import com.gopivotal.cf.srb.service.ServiceBrokerRegistrationService;
import com.jayway.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;

import static com.gopivotal.cf.srb.controller.TestDataUtil.*;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ServiceRegistryControllerTest {

    private RegisteredServiceRepository registeredServiceRepository;
    private ServiceRegistryController serviceRegistryController;
    private ServiceRepository serviceRepository;
    private ServiceBrokerRegistrationService serviceBrokerRegistationService;

    private Service dummyService;
    private RegisteredService registeredService;

    @Before
    public void setUp() {
        dummyService = prepareMockServiceData();
        registeredService = prepareRegisteredService();

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
                .statusCode(STATUS_201_CREATED)
                .body("id", anything());

        verify(registeredServiceRepository).save(registeredService);
        verify(serviceRepository).save(dummyService);
        verify(serviceBrokerRegistationService).registerSelfIdempotent();
    }

    @Test
    public void testUnregister() {
        dummyService.setId("ABCDEF");
        when(registeredServiceRepository.findOne(UNIVERSAL_DUMMY_ID)).thenReturn(registeredService);
        when(serviceRepository.findByName(DUMMY_SERVICE_NAME)).thenReturn(dummyService);

        given()
                .standaloneSetup(serviceRegistryController)
                .when()
                .delete("/registry/{registered_service_id}", UNIVERSAL_DUMMY_ID)
                .then()
                .statusCode(STATUS_200_OK)
                .assertThat().body(equalTo(EMPTY_RESPONSE_BODY));

        verify(registeredServiceRepository).delete(UNIVERSAL_DUMMY_ID);
        verify(serviceRepository).delete("ABCDEF");
        verify(serviceBrokerRegistationService).registerSelfIdempotent();
    }
}

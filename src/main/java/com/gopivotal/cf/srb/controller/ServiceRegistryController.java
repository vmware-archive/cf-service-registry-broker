package com.gopivotal.cf.srb.controller;

import com.gopivotal.cf.srb.Application;
import com.gopivotal.cf.srb.model.*;
import com.gopivotal.cf.srb.repository.RegisteredServiceRepository;
import com.gopivotal.cf.srb.repository.ServiceRepository;
import com.gopivotal.cf.srb.service.ServiceBrokerRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class ServiceRegistryController {

    private final RegisteredServiceRepository registeredServiceRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceBrokerRegistrationService serviceBrokerRegistrationService;

    @Autowired
    public ServiceRegistryController(RegisteredServiceRepository registeredServiceRepository, ServiceRepository serviceRepository, ServiceBrokerRegistrationService serviceBrokerRegistrationService) {
        this.registeredServiceRepository = registeredServiceRepository;
        this.serviceRepository = serviceRepository;
        this.serviceBrokerRegistrationService = serviceBrokerRegistrationService;
    }

    @RequestMapping(value = "/registry",
            method = RequestMethod.POST)
    public ResponseEntity<Object> register(@RequestBody RegisteredService registeredService) {
        registeredService.setId(UUID.randomUUID().toString());
        registeredServiceRepository.save(registeredService);

        PlanMetadataCostAmount amount = new PlanMetadataCostAmount();
        amount.setUsd(BigDecimal.ZERO);

        PlanMetadataCost cost = new PlanMetadataCost();
        cost.setAmount(amount);
        cost.setUnit("MONTH");

        PlanMetadata planMetadata = new PlanMetadata();
        planMetadata.addCost(cost);
        planMetadata.setBullets(registeredService.getFeatures());

        Plan plan = new Plan();
        plan.setId(UUID.randomUUID().toString());
        plan.setName("Standard");
        plan.setDescription("Standard Plan");
        plan.setFree(true);
        plan.setMetadata(planMetadata);

        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setDisplayName(registeredService.getDisplayName());
        serviceMetadata.setLongDescription(registeredService.getLongDescription());
        serviceMetadata.setProviderDisplayName(registeredService.getProvider());
        serviceMetadata.setImageUrl(Application.IMAGE_URL_FOR_SERVICE_REGISTRY);

        Service service = new Service();
        service.setId(UUID.randomUUID().toString());
        service.setDescription(registeredService.getDescription());
        service.setName(registeredService.getName());
        service.setBindable(true);
        service.setMetadata(serviceMetadata);
        service.addPlan(plan);

        serviceRepository.save(service);
        serviceBrokerRegistrationService.registerSelfIdempotent();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", registeredService.getId());

        return new ResponseEntity<Object>(responseBody, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/registry/{id}",
            method = RequestMethod.DELETE)
    public ResponseEntity<String> unregister(@PathVariable("id") String id) {
        RegisteredService registeredService = registeredServiceRepository.findOne(id);
        Service service = serviceRepository.findByName(registeredService.getName());

        serviceRepository.delete(service.getId());
        registeredServiceRepository.delete(id);
        serviceBrokerRegistrationService.registerSelfIdempotent();

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}

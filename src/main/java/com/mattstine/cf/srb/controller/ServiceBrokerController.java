package com.mattstine.cf.srb.controller;

import com.mattstine.cf.srb.model.Service;
import com.mattstine.cf.srb.model.ServiceInstance;
import com.mattstine.cf.srb.repository.ServiceInstanceRepository;
import com.mattstine.cf.srb.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ServiceBrokerController {

    private final ServiceRepository serviceRepository;
    private final ServiceInstanceRepository serviceInstanceRepository;

    @Autowired
    public ServiceBrokerController(ServiceRepository serviceRepository,
                                   ServiceInstanceRepository serviceInstanceRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceInstanceRepository = serviceInstanceRepository;
    }

    @RequestMapping("/v2/catalog")
    public Map<String, Iterable<Service>> catalog() {
        Map<String, Iterable<Service>> wrapper = new HashMap<>();
        wrapper.put("services", serviceRepository.findAll());
        return wrapper;
    }

    @RequestMapping(value = "/v2/service_instances/{id}", method = RequestMethod.PUT)
    public ResponseEntity<String> create(@PathVariable("id") String id, @RequestBody ServiceInstance serviceInstance) {
        serviceInstance.setId(id);

        boolean exists = serviceInstanceRepository.exists(id);

        if (exists) {
            ServiceInstance existing = serviceInstanceRepository.findOne(id);
            if (existing.equals(serviceInstance)) {
                return new ResponseEntity<>("{}", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("{}", HttpStatus.CONFLICT);
            }
        } else {
            serviceInstanceRepository.save(serviceInstance);
            return new ResponseEntity<>("{}", HttpStatus.CREATED);
        }
    }
}

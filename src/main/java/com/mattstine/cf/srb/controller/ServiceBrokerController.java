package com.mattstine.cf.srb.controller;

import com.mattstine.cf.srb.model.Service;
import com.mattstine.cf.srb.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ServiceBrokerController {

    @Autowired
    public ServiceBrokerController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    private ServiceRepository serviceRepository;

    @RequestMapping("/v2/catalog")
    public Map<String, Iterable<Service>> catalog() {
        Map<String, Iterable<Service>> wrapper = new HashMap<>();
        wrapper.put("services", serviceRepository.findAll());
        return wrapper;
    }
}

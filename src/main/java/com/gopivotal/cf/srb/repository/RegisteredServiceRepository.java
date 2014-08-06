package com.gopivotal.cf.srb.repository;

import com.gopivotal.cf.srb.model.RegisteredService;
import org.springframework.data.repository.CrudRepository;

public interface RegisteredServiceRepository extends CrudRepository<RegisteredService, String> {
    RegisteredService findByName(String name);
}

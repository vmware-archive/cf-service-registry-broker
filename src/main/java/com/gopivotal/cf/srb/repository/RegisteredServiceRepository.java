package com.gopivotal.cf.srb.repository;

import com.gopivotal.cf.srb.model.RegisteredService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface RegisteredServiceRepository extends CrudRepository<RegisteredService, String> {
}

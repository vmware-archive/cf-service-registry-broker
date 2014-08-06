package com.gopivotal.cf.srb.repository;

import com.gopivotal.cf.srb.model.ServiceInstance;
import org.springframework.data.repository.CrudRepository;

public interface ServiceInstanceRepository extends CrudRepository<ServiceInstance, String> {
}

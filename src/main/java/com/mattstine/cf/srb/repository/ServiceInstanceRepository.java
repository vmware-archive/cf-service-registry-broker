package com.mattstine.cf.srb.repository;

import com.mattstine.cf.srb.model.ServiceInstance;
import org.springframework.data.repository.CrudRepository;

public interface ServiceInstanceRepository extends CrudRepository<ServiceInstance, String> {
}

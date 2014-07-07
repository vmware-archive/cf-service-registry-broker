package com.mattstine.cf.srb.repository;

import com.mattstine.cf.srb.model.ServiceInstance;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by pivotal on 6/26/14.
 */
public interface ServiceInstanceRepository extends CrudRepository<ServiceInstance, String> {
}

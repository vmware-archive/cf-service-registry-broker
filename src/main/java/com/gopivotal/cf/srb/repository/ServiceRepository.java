package com.gopivotal.cf.srb.repository;

import com.gopivotal.cf.srb.model.Service;
import org.springframework.data.repository.CrudRepository;

public interface ServiceRepository extends CrudRepository<Service, String> {
}

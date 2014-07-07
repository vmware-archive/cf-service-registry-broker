package com.gopivotal.cf.srb.repository;

import com.gopivotal.cf.srb.model.ServiceBinding;
import org.springframework.data.repository.CrudRepository;

public interface ServiceBindingRepository extends CrudRepository<ServiceBinding,String> {
}

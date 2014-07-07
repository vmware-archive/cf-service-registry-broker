package com.mattstine.cf.srb.repository;

import com.mattstine.cf.srb.model.Plan;
import com.mattstine.cf.srb.model.Service;

import java.util.Arrays;

/**
 * Created by pivotal on 7/7/14.
 */
public class StubServiceRepository implements ServiceRepository {
    @Override
    public <S extends Service> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Service> Iterable<S> save(Iterable<S> entities) {
        return null;
    }

    @Override
    public Service findOne(String s) {
        return null;
    }

    @Override
    public boolean exists(String s) {
        return false;
    }

    @Override
    public Iterable<Service> findAll() {
        Service service = new Service();
        service.setId("123-456-789");
        service.setName("HaaSh");
        service.setBindable(true);
        service.setDescription("HashMap as a Service");

        Plan plan = new Plan();
        plan.setId("123-456-789");
        plan.setName("Basic");
        plan.setDescription("Basic Plan");

        service.addPlan(plan);

        return Arrays.asList(service);
    }

    @Override
    public Iterable<Service> findAll(Iterable<String> strings) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(String s) {

    }

    @Override
    public void delete(Service entity) {

    }

    @Override
    public void delete(Iterable<? extends Service> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
package com.mattstine.cf.srb.repository;

import com.mattstine.cf.srb.model.ServiceInstance;

public class StubServiceInstanceRepository implements ServiceInstanceRepository {
    @Override
    public <S extends ServiceInstance> S save(S entity) {
        return null;
    }

    @Override
    public <S extends ServiceInstance> Iterable<S> save(Iterable<S> entities) {
        return null;
    }

    @Override
    public ServiceInstance findOne(String s) {
        return null;
    }

    @Override
    public boolean exists(String s) {
        return false;
    }

    @Override
    public Iterable<ServiceInstance> findAll() {
        return null;
    }

    @Override
    public Iterable<ServiceInstance> findAll(Iterable<String> strings) {
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
    public void delete(ServiceInstance entity) {

    }

    @Override
    public void delete(Iterable<? extends ServiceInstance> entities) {

    }

    @Override
    public void deleteAll() {

    }
}

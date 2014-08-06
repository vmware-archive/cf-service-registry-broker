package com.gopivotal.cf.srb.service;

import org.springframework.context.annotation.Bean;

public interface ServiceBrokerRegistrationService {
    void registerSelfIdempotent();

    @SuppressWarnings("unchecked")
    @Bean
    String firstRoute();
}

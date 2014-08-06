package com.gopivotal.cf.srb.config;

import com.gopivotal.cf.srb.service.ServiceBrokerRegistrationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@Profile("default")
public class LocalConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setUrl("jdbc:mysql://localhost/cf_srb");
        driverManagerDataSource.setUsername("root");
        driverManagerDataSource.setPassword("password");
        return driverManagerDataSource;
    }

    @Bean
    public ServiceBrokerRegistrationService serviceBrokerRegistrationService() {
        return new ServiceBrokerRegistrationService() {
            @Override
            public void registerSelfIdempotent() {
                // Do nothing...stub!
            }
        };
    }
}

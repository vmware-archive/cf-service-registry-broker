package com.gopivotal.cf.srb;

import com.gopivotal.cf.srb.service.ServiceBrokerRegistrationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableJpaRepositories
@EnableScheduling
public class Application {

    Log log = LogFactory.getLog(Application.class);

    @Value("${CF_API}")
    private String cfApi;

    @Value("${CF_ADMIN_USER}")
    private String cfAdminUser;

    @Value("${CF_ADMIN_PASSWORD}")
    private String cfAdminPassword;

    @Value("${CF_ORG}")
    private String cfOrg;

    @Value("${CF_SPACE}")
    private String cfSpace;

    @Bean
    public Cloud cloud() {
        return new CloudFactory().getCloud();
    }

    @Bean
    public CloudFoundryOperations cloudFoundryOperations() throws MalformedURLException {
        CloudCredentials credentials = new CloudCredentials(cfAdminUser, cfAdminPassword);
        CloudFoundryOperations cfOps = new CloudFoundryClient(credentials, new URL(cfApi), cfOrg, cfSpace, true);
        cfOps.login();
        return cfOps;
    }

    @Autowired
    private ServiceBrokerRegistrationService serviceBrokerRegistrationService;

    private boolean registered = false;

    @Scheduled(fixedDelay = 5000)
    public void init() throws Exception {
        if (!registered) {
            log.info("Starting Service Broker Registration...");

            serviceBrokerRegistrationService.registerSelfIdempotent();
            registered = true;

            log.info("Service Broker Registration Complete!");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

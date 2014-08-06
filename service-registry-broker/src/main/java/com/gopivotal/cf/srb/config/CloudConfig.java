package com.gopivotal.cf.srb.config;

import com.gopivotal.cf.srb.service.ServiceBrokerRegistrationService;
import com.gopivotal.cf.srb.service.ServiceBrokerRegistrationServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@Profile("cloud")
public class CloudConfig extends AbstractCloudConfig {

    Log log = LogFactory.getLog(CloudConfig.class);

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
    public DataSource dataSource() {
        return connectionFactory().dataSource();
    }

    @Bean
    public CloudFoundryOperations cloudFoundryOperations() throws MalformedURLException {
        CloudCredentials credentials = new CloudCredentials(cfAdminUser, cfAdminPassword);
        CloudFoundryOperations cfOps = new CloudFoundryClient(credentials, new URL(cfApi), cfOrg, cfSpace, true);
        cfOps.login();
        return cfOps;
    }

    @Bean
    public ServiceBrokerRegistrationService serviceBrokerRegistrationService() throws MalformedURLException {
        return new ServiceBrokerRegistrationServiceImpl(cloud(), cloudFoundryOperations());
    }

    private boolean registered = false;

    @Scheduled(fixedDelay = 5000)
    public void init() throws Exception {
        if (!registered) {
            log.info("Starting Service Broker Registration...");

            serviceBrokerRegistrationService().registerSelfIdempotent();
            registered = true;

            log.info("Service Broker Registration Complete!");
        }
    }
}

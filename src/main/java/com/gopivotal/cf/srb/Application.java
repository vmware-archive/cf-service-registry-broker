package com.gopivotal.cf.srb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudServiceBroker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.List;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableJpaRepositories
@EnableScheduling
public class Application {

    Log log = LogFactory.getLog(Application.class);

    CloudFoundryOperations cfOps;

    @Value("${security.user.name}")
    private String securityUsername;

    @Value("${security.user.password}")
    private String securityPassword;

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

    private boolean registered = false;

    @Scheduled(fixedDelay = 5000)
    public void init() throws Exception {
        if (!registered) {
            log.info("Starting Service Broker Registration...");

            ApplicationInstanceInfo applicationInstanceInfo = cloud().getApplicationInstanceInfo();
            String applicationName = (String) applicationInstanceInfo.getProperties().get("application_name");

            List<Object> routes = (List<Object>) applicationInstanceInfo.getProperties().get("application_uris");
            String firstRoute = (String) routes.get(0);

            CloudCredentials credentials = new CloudCredentials(cfAdminUser, cfAdminPassword);
            this.cfOps = new CloudFoundryClient(credentials, new URL(cfApi), cfOrg, cfSpace, true);
            this.cfOps.login();

            CloudServiceBroker serviceBroker = this.cfOps.getServiceBroker(applicationName);
            if (serviceBroker != null) {
                serviceBroker = new CloudServiceBroker(serviceBroker.getMeta(), serviceBroker.getName(), serviceBroker.getUrl(), serviceBroker.getUsername(), securityPassword);
                log.info("Updating Service Broker...");
                this.cfOps.updateServiceBroker(serviceBroker);
            } else {
                serviceBroker = new CloudServiceBroker(CloudEntity.Meta.defaultMeta(), applicationName, "http://" + firstRoute, securityUsername, securityPassword);
                log.info("Creating Service Broker...");
                this.cfOps.createServiceBroker(serviceBroker);
            }

            log.info("Updating Service Plan Visibilities...");
            this.cfOps.updateServicePlanVisibilityForBroker(serviceBroker.getName(), true);
            registered = true;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

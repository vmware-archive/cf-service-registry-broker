package com.gopivotal.cf.srb.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudServiceBroker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.app.ApplicationInstanceInfo;

import java.util.List;

public class ServiceBrokerRegistrationServiceImpl implements ServiceBrokerRegistrationService {

    @Value("${security.user.name}")
    private String securityUsername;

    @Value("${security.user.password}")
    private String securityPassword;

    private final Log log = LogFactory.getLog(ServiceBrokerRegistrationServiceImpl.class);

    private final Cloud cloud;
    private final CloudFoundryOperations cloudFoundryOperations;

    public ServiceBrokerRegistrationServiceImpl(Cloud cloud, CloudFoundryOperations cloudFoundryOperations) {
        this.cloud = cloud;
        this.cloudFoundryOperations = cloudFoundryOperations;
    }

    @Override
    public void registerSelfIdempotent() {
        String applicationName = applicationName();

        CloudServiceBroker serviceBroker = cloudFoundryOperations.getServiceBroker(applicationName);
        if (serviceBroker != null) {
            serviceBroker = updateSelf(serviceBroker);
        } else {
            serviceBroker = registerSelf(applicationName);
        }

        updateServicePlanVisibilitiesForSelf(serviceBroker);
    }

    private void updateServicePlanVisibilitiesForSelf(CloudServiceBroker serviceBroker) {
        log.info("Updating Service Plan Visibilities...");
        cloudFoundryOperations.updateServicePlanVisibilityForBroker(serviceBroker.getName(), true);
    }

    private CloudServiceBroker registerSelf(String applicationName) {
        CloudServiceBroker serviceBroker = new CloudServiceBroker(CloudEntity.Meta.defaultMeta(), applicationName, "http://" + firstRoute(), securityUsername, securityPassword);
        log.info("Creating Service Broker...");
        cloudFoundryOperations.createServiceBroker(serviceBroker);
        return serviceBroker;
    }

    private CloudServiceBroker updateSelf(CloudServiceBroker serviceBroker) {
        serviceBroker = new CloudServiceBroker(serviceBroker.getMeta(), serviceBroker.getName(), serviceBroker.getUrl(), serviceBroker.getUsername(), securityPassword);
        log.info("Updating Service Broker...");
        cloudFoundryOperations.updateServiceBroker(serviceBroker);
        return serviceBroker;
    }

    @SuppressWarnings("unchecked")
    private String firstRoute() {
        ApplicationInstanceInfo applicationInstanceInfo = cloud.getApplicationInstanceInfo();
        List<Object> routes = (List<Object>) applicationInstanceInfo.getProperties().get("application_uris");
        return (String) routes.get(0);
    }

    private String applicationName() {
        ApplicationInstanceInfo applicationInstanceInfo = cloud.getApplicationInstanceInfo();
        return (String) applicationInstanceInfo.getProperties().get("application_name");
    }
}

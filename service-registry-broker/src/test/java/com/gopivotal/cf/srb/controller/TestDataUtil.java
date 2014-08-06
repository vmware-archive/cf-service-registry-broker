package com.gopivotal.cf.srb.controller;

import com.gopivotal.cf.srb.Application;
import com.gopivotal.cf.srb.model.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class TestDataUtil {

    public static final int STATUS_201_CREATED = 201;
    public static final int STATUS_200_OK = 200;
    public static final int STATUS_409_CONFLICT = 409;
    public static final int STATUS_410_GONE = 410;

    public static final String EMPTY_RESPONSE_BODY = "{}";

    public static final String UNIVERSAL_DUMMY_ID = "12345";

    public static final String DUMMY_SERVICE_DISPLAY_NAME = "A Web Service";
    public static final String DUMMY_SERVICE_LONG_DESCRIPTION = "A wicked cool web service that will provide you with unicorns and rainbows.";
    public static final String DUMMY_SERVICE_PROVIDER = "My Awesome Startup";
    public static final String DUMMY_SERVICE_NAME = "a-web-service";
    public static final String DUMMY_SERVICE_DESCRIPTION = "A wicked cool web service!";
    public static final String DUMMY_SERVICE_PLAN_NAME = "Standard";
    public static final String DUMMY_SERVICE_PLAN_DESCRIPTION = "Standard Plan";
    public static final List<String> DUMMY_SERVICE_PLAN_METADATA_BULLETS = Arrays.asList("Feature 1", "Feature 2", "Feature 3");
    public static final String DUMMY_SERVICE_PLAN_METADATA_COST_UNIT = "MONTH";

    public static final String REGISTERED_SERVICE_URL = "http://my.url.com";
    public static final String REGISTERED_SERVICE_USER = "tupac";
    public static final String REGISTERED_SERVICE_PASSWORD = "makaveli";

    public static Service prepareMockServiceData() {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setDisplayName(DUMMY_SERVICE_DISPLAY_NAME);
        serviceMetadata.setLongDescription(DUMMY_SERVICE_LONG_DESCRIPTION);
        serviceMetadata.setProviderDisplayName(DUMMY_SERVICE_PROVIDER);
        serviceMetadata.setImageUrl(Application.IMAGE_URL_FOR_SERVICE_REGISTRY);

        Service dummyService = new Service();
        dummyService.setId(UNIVERSAL_DUMMY_ID);
        dummyService.setDescription(DUMMY_SERVICE_DESCRIPTION);
        dummyService.setName(DUMMY_SERVICE_NAME);
        dummyService.setBindable(true);
        dummyService.setMetadata(serviceMetadata);

        PlanMetadataCostAmount amount = new PlanMetadataCostAmount();
        amount.setUsd(BigDecimal.ZERO);

        PlanMetadataCost cost = new PlanMetadataCost();
        cost.setAmount(amount);
        cost.setUnit(DUMMY_SERVICE_PLAN_METADATA_COST_UNIT);

        PlanMetadata planMetadata = new PlanMetadata();
        planMetadata.addCost(cost);
        planMetadata.setBullets(DUMMY_SERVICE_PLAN_METADATA_BULLETS);

        Plan plan = new Plan();
        plan.setId(UNIVERSAL_DUMMY_ID);
        plan.setName(DUMMY_SERVICE_PLAN_NAME);
        plan.setDescription(DUMMY_SERVICE_PLAN_DESCRIPTION);
        plan.setFree(true);
        plan.setMetadata(planMetadata);

        dummyService.addPlan(plan);
        return dummyService;
    }

    public static RegisteredService prepareRegisteredService() {
        RegisteredService registeredService = new RegisteredService();
        registeredService.setName(DUMMY_SERVICE_NAME);
        registeredService.setDescription(DUMMY_SERVICE_DESCRIPTION);
        registeredService.setLongDescription(DUMMY_SERVICE_LONG_DESCRIPTION);
        registeredService.setDisplayName(DUMMY_SERVICE_DISPLAY_NAME);
        registeredService.setProvider(DUMMY_SERVICE_PROVIDER);
        registeredService.setFeatures(DUMMY_SERVICE_PLAN_METADATA_BULLETS);
        registeredService.setUrl(REGISTERED_SERVICE_URL);
        registeredService.setBasicAuthUser(REGISTERED_SERVICE_USER);
        registeredService.setBasicAuthPassword(REGISTERED_SERVICE_PASSWORD);
        return registeredService;
    }
}

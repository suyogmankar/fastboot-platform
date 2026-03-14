package io.platform.controllers;

import org.springframework.stereotype.Component;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.platform.crds.platform.Platform;
import io.platform.services.database.DatabaseService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ControllerConfiguration
public class PlatformController implements Reconciler<Platform> {

    private final DatabaseService databaseService;

    public PlatformController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public UpdateControl<Platform> reconcile(Platform platform, Context<Platform> context) {
        log.info("----------[Platform reconciler started]----------");
        var platformServices = platform.getSpec().getServices();

        if (platformServices == null) {
            log.info("No Services are configured!!!!");
            return UpdateControl.noUpdate();
        }

        var databases = platformServices.getDatabases();
        if (databases != null && !databases.isEmpty()){
            databaseService.createDatabaseCustomResource(platform);
        } else log.info("No Database is configured!!!!");

        return UpdateControl.noUpdate();
    }
}

package io.platform.controllers;

import org.springframework.stereotype.Component;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.platform.crds.database.Database;
import io.platform.services.database.DatabaseProvisionerRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ControllerConfiguration
public class DatabaseController implements Reconciler<Database> {

    private final DatabaseProvisionerRegistry databaseProvisionerRegistry;

    public DatabaseController(DatabaseProvisionerRegistry databaseProvisionerRegistry) {
        this.databaseProvisionerRegistry = databaseProvisionerRegistry;
    }

    @Override
    public UpdateControl<Database> reconcile(Database database, Context context) {
        log.info("----------Database reconciler----------");
        databaseProvisionerRegistry.getProvisioner(database).provision(database);
        return UpdateControl.noUpdate();
    }
}
package io.platform.controllers;

import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.platform.crds.database.Database;
import io.platform.crds.platform.Platform;
import io.platform.services.database.DatabaseProvisionerRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ControllerConfiguration
public class DatabaseController implements Reconciler<Database> {

    private final DatabaseProvisionerRegistry databaseProvisionerRegistry;
    private final KubernetesClient client;

    public DatabaseController(DatabaseProvisionerRegistry databaseProvisionerRegistry,KubernetesClient client) {
        this.databaseProvisionerRegistry = databaseProvisionerRegistry;
        this.client = client;
    }

    @Override
    public UpdateControl<Database> reconcile(Database database, Context context) {
        log.info("----------Database reconciler----------");

        String namespace = database.getMetadata().getNamespace();

        Platform platform = client.resources(Platform.class)
            .inNamespace(namespace)
            .list()
            .getItems()
            .stream()
            .findFirst()
            .orElse(null);

        databaseProvisionerRegistry
            .getProvisioner(database)
            .provision(platform, database);

        return UpdateControl.noUpdate();
    }
}
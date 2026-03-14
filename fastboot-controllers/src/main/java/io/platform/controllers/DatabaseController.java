package io.platform.controllers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.platform.crds.database.Database;
import io.platform.crds.platform.Platform;
import io.platform.services.database.DatabaseService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ControllerConfiguration
public class DatabaseController implements Reconciler<Database> {

    private final KubernetesClient client;
    private final DatabaseService databaseService;

    public DatabaseController(KubernetesClient client, DatabaseService databaseService) {
        this.client = client;
        this.databaseService = databaseService;
    }

    @Override
    public UpdateControl<Database> reconcile(Database database, Context context) {
        log.info("----------[Database reconciler started]----------");
        String namespace = database.getMetadata().getNamespace();

        Optional<Platform> optionalPlatform = client.resources(Platform.class)
            .inNamespace(namespace)
            .list()
            .getItems()
            .stream()
            .findFirst();
        optionalPlatform.ifPresent(platform -> databaseService.provisionDatabase(platform, database));

        return UpdateControl.noUpdate();
    }
}
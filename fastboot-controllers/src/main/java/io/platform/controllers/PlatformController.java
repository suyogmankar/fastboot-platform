package io.platform.controllers;

import org.springframework.stereotype.Component;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.platform.crds.database.Database;
import io.platform.crds.platform.Platform;
import io.platform.crds.database.DatabaseSpec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ControllerConfiguration
public class PlatformController implements Reconciler<Platform> {

    private final KubernetesClient client;

    public PlatformController(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public UpdateControl<Platform> reconcile(Platform platform, Context<Platform> context) {
        log.info("----------Platform reconciler----------");
        if (platform.getSpec().getServices() == null) {
            log.info("No Services are configured!!!!");
        }

        // Database as a Service
        createDatabaseResource(platform);

        return UpdateControl.noUpdate();
    }

    private void createDatabaseResource(Platform platform) {
        log.info("Enabling database as service");
        String namespace = platform.getMetadata().getNamespace();

        var databases = platform.getSpec().getServices().getDatabases();

        if (databases == null) {
            log.debug("Database list is empty");
            return;
        }

        for (DatabaseSpec dbSpec : databases) {
            Database existingDatabase = client.resources(Database.class)
                .inNamespace(namespace)
                .withName(dbSpec.getName())
                .get();

            if (existingDatabase == null) {
                log.info("Creating Database CR: {}", dbSpec.getName());
                Database database = new Database();
                database.setMetadata(new ObjectMeta());
                database.getMetadata().setName(dbSpec.getName());
                database.getMetadata().setNamespace(namespace);

                database.setSpec(dbSpec);

                database.addOwnerReference(platform);

                client.resources(Database.class)
                    .inNamespace(namespace)
                    .resource(database)
                    .create();
            } else if (!existingDatabase.getSpec().equals(dbSpec)) {
                log.info("Updating Database CR: {}", dbSpec.getName());
                existingDatabase.setSpec(dbSpec);

                client.resources(Database.class)
                    .inNamespace(namespace)
                    .resource(existingDatabase)
                    .update();
            }
        }
    }
}

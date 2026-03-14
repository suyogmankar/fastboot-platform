package io.platform.services.database;

import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.platform.crds.database.Database;
import io.platform.crds.database.DatabaseSpec;
import io.platform.crds.platform.Platform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DatabaseService {

    private final KubernetesClient client;
    private final DatabaseProvisionerRegistry databaseProvisionerRegistry;

    public DatabaseService(KubernetesClient client, DatabaseProvisionerRegistry databaseProvisionerRegistry) {
        this.client = client;
        this.databaseProvisionerRegistry = databaseProvisionerRegistry;
    }

    public void createDatabaseCustomResource(Platform platform) {
        String namespace = platform.getMetadata().getNamespace();
        var databases = platform.getSpec().getServices().getDatabases();

        for (DatabaseSpec dbSpec : databases) {
            Database existing = client.resources(Database.class)
                .inNamespace(namespace)
                .withName(dbSpec.getDatabaseName())
                .get();

            if (existing != null) {
                log.info("Database CR {} already exists", dbSpec.getDatabaseName());
                continue;
            }

            Database database = new Database();
            database.setMetadata(new ObjectMeta());
            database.getMetadata().setName(dbSpec.getDatabaseName());
            database.getMetadata().setNamespace(namespace);

            database.setSpec(dbSpec);
            database.addOwnerReference(platform);

            client.resources(Database.class)
                .inNamespace(namespace)
                .resource(database)
                .create();
            log.info("Database Custom Resource: {} is successfully created", dbSpec.getDatabaseName());
        }
    }

    public void provisionDatabase(Platform platform, Database database) {
        log.info("------<Database provisioning started>------");
        databaseProvisionerRegistry
            .getProvisioner(database)
            .provision(platform, database);
    }
}
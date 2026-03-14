package io.platform.services.database;

import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.HasMetadata;
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

    public void createDatabaseResource(String namespace,
                                   String databaseName,
                                   DatabaseSpec dbSpec,
                                   HasMetadata owner) {
        Database existing = client.resources(Database.class)
            .inNamespace(namespace)
            .withName(databaseName)
            .get();

        if (existing != null) {
            log.info("Database CR {} already exists", databaseName);
            return;
        }

        Database database = new Database();
        database.setMetadata(new ObjectMeta());
        database.getMetadata().setName(databaseName);
        database.getMetadata().setNamespace(namespace);

        database.setSpec(dbSpec);
        database.addOwnerReference(owner);

        client.resources(Database.class)
            .inNamespace(namespace)
            .resource(database)
            .create();

        log.info("Database Custom Resource {} created successfully", databaseName);
    }

    public void createDatabaseResourceFromPlatform(Platform platform) {
       String namespace = platform.getMetadata().getNamespace();
       var databases = platform.getSpec().getServices().getDatabases();

       for (DatabaseSpec dbSpec : databases) {
           createDatabaseResource(
                namespace,
                dbSpec.getDatabaseName(),
                dbSpec,
                platform
           );
       }
    }

    public void provisionDatabase(Platform platform, Database database) {
        log.info("------<Database provisioning started>------");
        databaseProvisionerRegistry
            .getProvisioner(database)
            .provision(platform, database);
    }
}
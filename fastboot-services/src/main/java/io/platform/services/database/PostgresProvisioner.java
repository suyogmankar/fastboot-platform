package io.platform.services.database;

import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
import io.platform.crds.database.Database;
import io.platform.crds.platform.Platform;
import io.platform.services.platform.PlatformDefaultResolver;
import io.platform.services.utils.KubernetesResourceUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostgresProvisioner implements DatabaseProvisioner {

    private static final String POSTGRES = "postgres";
    private final KubernetesClient client;
    private final PlatformDefaultResolver platformDefaultResolver;

    public PostgresProvisioner(KubernetesClient client, PlatformDefaultResolver platformDefaultResolver) {
        this.client = client;
        this.platformDefaultResolver = platformDefaultResolver;
    }

    @Override
    public boolean supports(String type) {
        return POSTGRES.equalsIgnoreCase(type);
    }

    @Override
    public boolean isRequiredResourcesAvailable(Database database) {
        Secret secret = client.secrets()
            .inNamespace(database.getMetadata().getNamespace())
            .withName(database.getSpec().getSecretName())
            .get();

        if (secret == null) {
            throw new ResourceNotFoundException("Secret " + database.getSpec().getSecretName() + " not found");
        }
        return true;
    }

    @Override
    public void provision(Platform platform, Database db) {
        Database database = platformDefaultResolver.applyDatabaseDefaults(platform, db);

        String name = database.getMetadata().getName();
        String namespace = database.getMetadata().getNamespace();
        int port = database.getSpec().getPort();
        String version = database.getSpec().getVersion();
        String secretName = database.getSpec().getSecretName();
        Boolean externalAccess = database.getSpec().getExternalAccess();

        log.info("Database provisioning started for PostgreSQL");
        StatefulSet statefulSet = KubernetesResourceUtils.createStatefulSet(database, name, namespace, port, version, secretName, POSTGRES);
        client.resource(statefulSet).serverSideApply();

        log.info("Creating service for {}", name);
        Service service = KubernetesResourceUtils.createClientService(database, name, namespace, port, POSTGRES, externalAccess);
        client.resource(service).serverSideApply();

        Service headless = KubernetesResourceUtils.createHeadlessService(database, name, namespace, port, POSTGRES);
        client.resource(headless).serverSideApply();
        log.info("Service created successfully for {}", name);

        log.info("Postgres database started successfully");
    }
}
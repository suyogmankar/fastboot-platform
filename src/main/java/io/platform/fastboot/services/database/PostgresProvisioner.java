package io.platform.fastboot.services.database;

import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
import io.platform.fastboot.crds.Database;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostgresProvisioner implements DatabaseProvisioner {

    private static final String POSTGRES = "postgres";
    private final KubernetesClient client;

    public PostgresProvisioner(KubernetesClient client) {
        this.client = client;
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
            throw new ResourceNotFoundException("Secret " + database.getMetadata().getNamespace() + " not found");
        }
        return true;
    }

    @Override
    public void provision(Database database) {
        String name = database.getMetadata().getName();
        String namespace = database.getMetadata().getNamespace();
        int port = database.getSpec().getPort();
        String version = database.getSpec().getVersion();
        String secretName = database.getSpec().getSecretName();
        boolean externalAccess = database.getSpec().isExternalAccess();

        log.info("Database provisioning started for PostgreSQL");
        createStatefulSet(database, name, namespace, port, version, secretName);

        log.info("Creating {} service for {}", externalAccess ? "external LoadBalancer" : "headless", name);
        Service service = createService(database, name, namespace, port, POSTGRES, externalAccess);
        client.resource(service).serverSideApply();
        log.info("{} service created successfully", externalAccess ? "External LoadBalancer" : "Headless");

        log.info("Postgres database started successfully");
    }

    private void createStatefulSet(Database database, String name, String namespace, int port, String version, String secretName) {
        log.debug("Creating StatefulSet for {}", name);
        StatefulSet ss = new StatefulSetBuilder()
            .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .withOwnerReferences(owner(database))
            .endMetadata()
            .withNewSpec()
                .withServiceName(name)
                .withReplicas(1)
                .withNewSelector()
                    .addToMatchLabels("app", name)
                .endSelector()
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("app", name)
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withName(POSTGRES)
                            .withImage("postgres:" + version)
                            .addNewEnv()
                                .withName("POSTGRES_DB")
                                .withValue(name)
                            .endEnv()
                            .addNewEnv()
                                .withName("POSTGRES_USER")
                                .withNewValueFrom()
                                    .withNewSecretKeyRef("POSTGRES_USER", secretName, false)
                                .endValueFrom()
                            .endEnv()
                            .addNewEnv()
                                .withName("POSTGRES_PASSWORD")
                                .withNewValueFrom()
                                    .withNewSecretKeyRef("POSTGRES_PASSWORD", secretName, false)
                                .endValueFrom()
                            .endEnv()
                            .addNewPort()
                                .withContainerPort(port)
                            .endPort()
                        .endContainer()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();

        client.resource(ss).serverSideApply();
        log.debug("StatefulSet created successfully");
    }
}
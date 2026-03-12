package io.platform.fastboot.services.database;

import org.springframework.stereotype.Component;

import io.platform.fastboot.crds.Database;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
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

        log.info("Database provisioning started for PostgreSQL");
        createStatefulSet(database, name, namespace, port, version, secretName);
        createService(database, name, namespace, port);
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

        client.resource(ss).create();
        log.debug("StatefulSet created successfully");
    }

    private void createService(Database database, String name, String namespace, int port) {
        log.debug("Creating service for {}", name);
        Service svc = new ServiceBuilder()
            .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .withOwnerReferences(owner(database))
            .endMetadata()
            .withNewSpec()
                .withClusterIP("None")
                    .addToSelector("app", name)
                    .addNewPort()
                    .withPort(port)
                        .withTargetPort(new IntOrString(port))
                        .withName(POSTGRES)
                    .endPort()
            .endSpec()
            .build();

        client.resource(svc).create();
        log.debug("Service for database created successfully");
    }

    private OwnerReference owner(Database database) {
        return new OwnerReferenceBuilder()
            .withApiVersion(database.getApiVersion())
            .withKind(database.getKind())
            .withName(database.getMetadata().getName())
            .withUid(database.getMetadata().getUid())
            .withController(true)
            .withBlockOwnerDeletion(true)
            .build();
    }
}
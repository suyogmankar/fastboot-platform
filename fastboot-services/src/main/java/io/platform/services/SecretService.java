package io.platform.services;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.platform.crds.database.Database;
import io.platform.services.utils.KubernetesResourceUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SecretService {

    private final KubernetesClient kubernetesClient;

    public SecretService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public Secret buildDatabaseSecret(Database database) {
        log.info("[STEP:1] - Building secret for {} database", database.getSpec().getType());

        Secret existing = kubernetesClient.secrets()
            .inNamespace(database.getMetadata().getNamespace())
            .withName(database.getSpec().getType().concat("-secret"))
            .get();

        if (existing != null) {
            return existing;
        }

        String password = UUID.randomUUID().toString().replace("-", "");

        return new SecretBuilder()
            .withNewMetadata()
                .withName(database.getSpec().getType().concat("-secret"))
                .withNamespace(database.getMetadata().getNamespace())
                .withOwnerReferences(KubernetesResourceUtils.owner(database))
            .endMetadata()
            .withType("Opaque")
            .withStringData(Map.of(
                "POSTGRES_USER", "admin",
                "POSTGRES_PASSWORD", password
            ))
            .build();
    }
}
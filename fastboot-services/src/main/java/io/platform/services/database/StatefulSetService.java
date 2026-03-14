package io.platform.services.database;

import static io.platform.services.utils.KubernetesResourceUtils.owner;

import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.platform.crds.database.Database;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StatefulSetService {

    public StatefulSet buildStatefulSet(
        Database database,
        String namespace,
        Integer port,
        String version,
        String secretName,
        String containerName) {

        log.info("[STEP:2] - Building StatefulSetService for {}", database.getSpec().getType());
        return new StatefulSetBuilder()
            .withNewMetadata()
                .withName(database.getMetadata().getName())
                .withNamespace(namespace)
                .withOwnerReferences(owner(database))
            .endMetadata()
            .withNewSpec()
                .withServiceName(database.getMetadata().getName())
                .withReplicas(1)
                .withNewPersistentVolumeClaimRetentionPolicy()
                    .withWhenDeleted("Delete")
                    .withWhenScaled("Delete")
                .endPersistentVolumeClaimRetentionPolicy()
                .withNewSelector()
                    .addToMatchLabels("app", database.getMetadata().getName())
                .endSelector()
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("app", database.getMetadata().getName())
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withName(containerName)
                            .withImage("postgres:" + version)
                            .addNewEnv()
                                .withName("POSTGRES_DB")
                                .withValue(database.getMetadata().getName())
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
                            .addNewVolumeMount()
                                .withName(containerName.concat("-data"))
                                .withMountPath("/var/lib/postgresql/data")
                            .endVolumeMount()
                        .endContainer()
                    .endSpec()
                .endTemplate()
                .addNewVolumeClaimTemplate()
                    .withNewMetadata()
                        .withName(containerName.concat("-data"))
                    .endMetadata()
                    .withNewSpec()
                        .addToAccessModes("ReadWriteOnce")
                        .withStorageClassName(database.getSpec().getStorageClass())
                        .withNewResources()
                            .addToRequests("storage",
                                new Quantity(database.getSpec().getStorage()))
                        .endResources()
                    .endSpec()
                .endVolumeClaimTemplate()
            .endSpec()
            .build();
    }
}
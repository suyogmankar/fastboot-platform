package io.platform.services.utils;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.platform.crds.database.Database;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KubernetesResourceUtils {

    private KubernetesResourceUtils() {
        /* This utility class should not be instantiated */
    }

    public static StatefulSet createStatefulSet(
        Database database,
        String name,
        String namespace,
        int port,
        String version,
        String secretName,
        String containerName) {
        log.debug("Creating StatefulSet for {}", name);
        StatefulSet statefulSet = new StatefulSetBuilder()
            .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .withOwnerReferences(owner(database))
            .endMetadata()
            .withNewSpec()
                .withServiceName(name)
                .withReplicas(1)
                .withNewPersistentVolumeClaimRetentionPolicy()
                    .withWhenDeleted("Delete")
                    .withWhenScaled("Delete")
                .endPersistentVolumeClaimRetentionPolicy()
                .withNewSelector()
                    .addToMatchLabels("app", name)
                .endSelector()
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("app", name)
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withName(containerName)
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

        log.debug("StatefulSet created successfully");
        return statefulSet;
    }

    public static Service createHeadlessService(
        Database database,
        String name,
        String namespace,
        int port,
        String containerName) {
        return new ServiceBuilder()
            .withNewMetadata()
                .withName(name + "-headless")
                .withNamespace(namespace)
                .withOwnerReferences(owner(database))
                .addToLabels("app", name)
            .endMetadata()
            .withNewSpec()
                .withClusterIP("None")
                .addToSelector("app", name)
                .addNewPort()
                    .withName(containerName)
                    .withPort(port)
                    .withTargetPort(new IntOrString(port))
                .endPort()
            .endSpec()
            .build();
    }

    public static Service createClientService(
        Database database,
        String name,
        String namespace,
        int port,
        String containerName,
        boolean external) {
        ServiceBuilder builder = new ServiceBuilder()
            .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .withOwnerReferences(owner(database))
                .addToLabels("app", name)
            .endMetadata()
            .withNewSpec()
                .addToSelector("app", name)
                .addNewPort()
                    .withName(containerName)
                    .withPort(port)
                    .withTargetPort(new IntOrString(port))
                .endPort()
            .endSpec();

        if (external) {
            builder.editSpec()
                .withType("LoadBalancer")
                .endSpec();
        } else {
            builder.editSpec()
                .withType("ClusterIP")
                .endSpec();
        }
        return builder.build();
    }

    public static OwnerReference owner(Database database) {
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
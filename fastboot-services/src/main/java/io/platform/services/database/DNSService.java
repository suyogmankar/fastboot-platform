package io.platform.services.database;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.platform.crds.database.Database;
import io.platform.services.utils.KubernetesResourceUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@org.springframework.stereotype.Service
public class DNSService {

    public Service createClientService(
        Database database,
        String name,
        String namespace,
        Integer port,
        String containerName,
        boolean external) {

        log.info("[STEP:3.1] - Building Client service for {}", database.getSpec().getType());
        ServiceBuilder builder = new ServiceBuilder()
            .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .withOwnerReferences(KubernetesResourceUtils.owner(database))
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

    public Service createHeadlessService(
        Database database,
        String name,
        String namespace,
        Integer port,
        String containerName) {

        log.info("[STEP:3.2] - Building headless service for {}", database.getSpec().getType());
        return new ServiceBuilder()
            .withNewMetadata()
                .withName(name + "-headless")
                .withNamespace(namespace)
                .withOwnerReferences(KubernetesResourceUtils.owner(database))
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
}
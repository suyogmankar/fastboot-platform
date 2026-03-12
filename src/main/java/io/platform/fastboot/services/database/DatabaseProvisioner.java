package io.platform.fastboot.services.database;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.platform.fastboot.crds.Database;
import io.platform.fastboot.crds.specs.DatabaseStatus;

public interface DatabaseProvisioner {
    void provision(Database database);
    boolean supports(String type);
    boolean isRequiredResourcesAvailable(Database database);

    default Service createService(Database database, String name, String namespace, int port, String containerName, boolean external) {
        String svcName = external ? name + "-external" : name;

        ServiceBuilder builder = new ServiceBuilder()
            .withNewMetadata()
                .withName(svcName)
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
            builder.editSpec().withType("LoadBalancer").endSpec();
        } else {
            builder.editSpec().withClusterIP("None").endSpec();
        }
        return builder.build();
    }

    default Database updateDatabaseStatus(Database database, String host, int port, String secretName) {
        DatabaseStatus status = new DatabaseStatus();
        status.setHost(host);
        status.setPort(port);
        status.setDatabase(database.getMetadata().getName());
        status.setSecretName(secretName);

        database.setStatus(status);
        return database;
    }

    default OwnerReference owner(Database database) {
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

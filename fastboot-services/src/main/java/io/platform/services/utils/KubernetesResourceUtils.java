package io.platform.services.utils;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KubernetesResourceUtils {

    private KubernetesResourceUtils() {
        /* This utility class should not be instantiated */
    }

    public static OwnerReference owner(HasMetadata resource) {
        return new OwnerReferenceBuilder()
            .withApiVersion(resource.getApiVersion())
            .withKind(resource.getKind())
            .withName(resource.getMetadata().getName())
            .withUid(resource.getMetadata().getUid())
            .withController(true)
            .withBlockOwnerDeletion(true)
            .build();
    }
}
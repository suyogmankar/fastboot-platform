package io.platform.crds.platform;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Singular;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("io.platform.fastboot")
@Version("v1alpha1")
@Singular("platform")
@Plural("platforms")
@ShortNames({ "pf" })
public class Platform extends CustomResource<PlatformSpec, Void> implements Namespaced {}

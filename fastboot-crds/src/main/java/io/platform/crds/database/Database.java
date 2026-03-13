package io.platform.crds.database;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Singular;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("io.platform.fastboot")
@Version("v1alpha1")
@Singular("database")
@Plural("databases")
@ShortNames({ "db" })
public class Database extends CustomResource<DatabaseSpec, DatabaseStatus> implements Namespaced {}

package io.platform.crds.database;

import io.platform.core.database.DatabaseSize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseSpec {

    private String databaseName;
    private String type;
    private String version;
    private Integer port;

    // Storage
    private String storage;
    private String storageClass;
    private Boolean externalAccess;
    private DatabaseSize size;
}

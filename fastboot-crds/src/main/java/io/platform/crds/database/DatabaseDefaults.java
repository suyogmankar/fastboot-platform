package io.platform.crds.database;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseDefaults {
    private String storage = "10Gi";
    private String storageClass = "local-path";
    private Boolean externalAccess = true;
}
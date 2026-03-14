package io.platform.crds.database;

import java.util.Map;

import io.platform.core.database.DatabaseSize;
import io.platform.core.database.DatabaseSizeProfile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseDefaults {
    private String storage = "5Gi";
    private String storageClass = "local-path";
    private Boolean externalAccess = true;
    private Map<DatabaseSize, DatabaseSizeProfile> sizes;
}
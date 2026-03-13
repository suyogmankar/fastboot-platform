package io.platform.services.database;

import io.platform.crds.database.Database;
import io.platform.crds.platform.Platform;

public interface DatabaseProvisioner {
    void provision(Platform platform, Database database);
    boolean supports(String type);
    boolean isRequiredResourcesAvailable(Database database);
}
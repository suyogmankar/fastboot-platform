package io.platform.services.database;

import io.platform.crds.database.Database;

public interface DatabaseProvisioner {
    void provision(Database database);
    boolean supports(String type);
    boolean isRequiredResourcesAvailable(Database database);
}
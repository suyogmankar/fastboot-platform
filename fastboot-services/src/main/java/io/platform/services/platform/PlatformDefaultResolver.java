package io.platform.services.platform;

import org.springframework.stereotype.Component;

import io.platform.core.database.DatabaseSize;
import io.platform.core.database.DatabaseSizeProfile;
import io.platform.crds.database.Database;
import io.platform.crds.database.DatabaseDefaults;
import io.platform.crds.platform.Platform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PlatformDefaultResolver {

    public Database applyDatabaseDefaults(Platform platform, Database database) {

        DatabaseDefaults defaults = platform.getSpec().getDefaults().getDatabase();

        if (database.getSpec().getSize() != null && defaults.getSizes() != null) {
            DatabaseSize size = database.getSpec().getSize();
            DatabaseSizeProfile profile = defaults.getSizes().get(size);

            if (profile != null) {
                database.getSpec().setStorage(profile.getStorage());
            }
        }

        if (database.getSpec().getStorage() == null) {
            database.getSpec().setStorage(defaults.getStorage());
        }

        if (database.getSpec().getStorageClass() == null) {
            database.getSpec().setStorageClass(defaults.getStorageClass());
        }

        if (database.getSpec().getExternalAccess() == null) {
            database.getSpec().setExternalAccess(defaults.getExternalAccess());
        }
        return database;
    }
}
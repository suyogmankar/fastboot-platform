package io.platform.services.platform;

import org.springframework.stereotype.Component;

import io.platform.crds.database.Database;
import io.platform.crds.database.DatabaseDefaults;
import io.platform.crds.platform.Platform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PlatformDefaultResolver {

    public Database applyDatabaseDefaults(Platform platform, Database database) {

        String storage = database.getSpec().getStorage();
        String storageClass = database.getSpec().getStorageClass();
        Boolean externalAccess = database.getSpec().getExternalAccess();

        DatabaseDefaults databaseDefaults = platform.getSpec()
            .getDefaults()
            .getDatabase();

        database.getSpec().setStorage(storage == null ? databaseDefaults.getStorage() : storage);
        database.getSpec().setStorageClass(storageClass == null ? databaseDefaults.getStorageClass() : storageClass);
        database.getSpec().setExternalAccess(externalAccess == null ? databaseDefaults.getExternalAccess() : externalAccess);
        return database;
    }
}
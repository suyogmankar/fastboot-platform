package io.platform.services.database;

import java.util.List;

import org.springframework.stereotype.Component;

import io.platform.crds.database.Database;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DatabaseProvisionerRegistry {

    private final List<DatabaseProvisioner> provisioners;

    public DatabaseProvisionerRegistry(List<DatabaseProvisioner> provisioners) {
        this.provisioners = provisioners;
    }

    public DatabaseProvisioner getProvisioner(Database database) {
        // Find provisioner that supports the type
        log.debug("Checking database support");

        return provisioners.stream()
            .filter(p -> p.supports(database.getSpec().getType()))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Unsupported database type " + database.getSpec().getType()));
    }
}
package io.platform.fastboot.controllers;

import java.util.List;

import org.springframework.stereotype.Component;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.platform.fastboot.crds.Database;
import io.platform.fastboot.services.database.DatabaseProvisioner;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ControllerConfiguration
public class DatabaseController implements Reconciler<Database> {

    private final List<DatabaseProvisioner> provisioners;

    public DatabaseController(List<DatabaseProvisioner> provisioners) {
        this.provisioners = provisioners;
    }

    @Override
    public UpdateControl<Database> reconcile(Database database, Context context) {
        log.info("----------Database reconciler----------");
        String type = database.getSpec().getType();

        // Find provisioner that supports the type
        log.debug("Checking database support");
        DatabaseProvisioner databaseProvisioner = provisioners.stream()
            .filter(p -> p.supports(type))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported DB type: " + type));

        // Check if required resources are created or available in namespace
        log.debug("Checking required kubernetes resources");
        if (!databaseProvisioner.isRequiredResourcesAvailable(database)) {
            throw new IllegalStateException("Required resources are not available for Database provisioning");
        }
        databaseProvisioner.provision(database);

        return UpdateControl.noUpdate();
    }
}
package io.platform.controllers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.platform.crds.database.DatabaseClaim;
import io.platform.crds.database.DatabaseSpec;
import io.platform.crds.platform.Platform;
import io.platform.services.database.DatabaseService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ControllerConfiguration
public class DatabaseClaimController implements Reconciler<DatabaseClaim> {

    private final DatabaseService databaseService;

    public DatabaseClaimController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public UpdateControl<DatabaseClaim> reconcile(DatabaseClaim claim, Context context) {
        log.info("----------[DatabaseClaim reconciler started]----------");
        String namespace = claim.getMetadata().getNamespace();

        DatabaseSpec spec = new DatabaseSpec();
        spec.setType(claim.getSpec().getEngine());
        spec.setVersion(claim.getSpec().getVersion());
        spec.setSize(claim.getSpec().getSize());

        Optional<Platform> optionalPlatform = context.getClient().resources(Platform.class)
            .inNamespace(namespace)
            .list()
            .getItems()
            .stream()
            .findFirst();


        databaseService.createDatabaseResource(
            namespace,
            claim.getMetadata().getName(),
            spec,
            optionalPlatform.isPresent() ? optionalPlatform.get() : claim
        );
        return UpdateControl.noUpdate();
    }
}
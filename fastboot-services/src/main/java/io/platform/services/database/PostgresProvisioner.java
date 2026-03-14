package io.platform.services.database;

import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.platform.crds.database.Database;
import io.platform.crds.platform.Platform;
import io.platform.services.platform.PlatformDefaultResolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PostgresProvisioner implements DatabaseProvisioner {

    private static final String POSTGRES = "postgres";
    private final KubernetesClient client;
    private final PlatformDefaultResolver platformDefaultResolver;
    private final SecretService secretService;
    private final StatefulSetService statefulSetService;
    private final DNSService dnsService;

    public PostgresProvisioner(KubernetesClient client,
        PlatformDefaultResolver platformDefaultResolver, SecretService secretService,
        StatefulSetService statefulSetService, DNSService dnsService) {
        this.client = client;
        this.platformDefaultResolver = platformDefaultResolver;
        this.secretService = secretService;
        this.statefulSetService = statefulSetService;
        this.dnsService = dnsService;
    }

    @Override
    public boolean supports(String type) {
        return POSTGRES.equalsIgnoreCase(type);
    }

    @Override
    public void provision(Platform platform, Database db) {
        Database database = platformDefaultResolver.applyDatabaseDefaults(platform, db);

        String namespace = database.getMetadata().getNamespace();
        String databaseName = database.getMetadata().getName();
        String type = database.getSpec().getType();
        String version = database.getSpec().getVersion();
        Integer port = database.getSpec().getPort() == null ? 5432 : database.getSpec().getPort();

        // 1. Create secretService
        Secret secret = this.secretService.buildDatabaseSecret(database);
        client.resource(secret).serverSideApply();
        log.info("-> Secret created successfully for {}", type);

        // 2. Create StatefulSetService
        StatefulSet statefulSet = this.statefulSetService.buildStatefulSet(database, namespace, port, version, secret.getMetadata().getName(), POSTGRES);
        client.resource(statefulSet).serverSideApply();
        log.info("-> StatefulSetService created successfully for {}", type);

        // 3. Create DNS
        Service service = dnsService.createClientService(database, databaseName, namespace, port, POSTGRES, database.getSpec().getExternalAccess());
        client.resource(service).serverSideApply();

        Service headless = dnsService.createHeadlessService(database, databaseName, namespace, port, POSTGRES);
        client.resource(headless).serverSideApply();
        log.info("-> DNSService created successfully for {}", type);

        log.info("------<Postgres database started successfully>------");
    }
}
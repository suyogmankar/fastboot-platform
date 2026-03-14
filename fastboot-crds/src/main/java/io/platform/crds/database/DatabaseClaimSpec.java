package io.platform.crds.database;

import io.platform.core.database.DatabaseSize;
import lombok.Data;

@Data
public class DatabaseClaimSpec {

    private String engine;
    private String version;
    private DatabaseSize size;
}
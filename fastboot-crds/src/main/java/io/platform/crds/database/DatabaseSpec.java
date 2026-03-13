package io.platform.crds.database;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseSpec {
    private String name;
    private String type;
    private String version;
    private int port;
    private String secretName;
    private boolean externalAccess;
}

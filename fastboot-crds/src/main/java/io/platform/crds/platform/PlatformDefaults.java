package io.platform.crds.platform;

import io.platform.crds.database.DatabaseDefaults;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformDefaults {
    private DatabaseDefaults database;
}
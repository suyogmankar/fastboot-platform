package io.platform.crds.platform;

import java.util.List;

import io.platform.crds.database.DatabaseSpec;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformServices {
    private List<DatabaseSpec> databases;
}

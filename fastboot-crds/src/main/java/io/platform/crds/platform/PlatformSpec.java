package io.platform.crds.platform;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformSpec {
    private String name;
    private String stage;
    private String version;
    private PlatformServices services;
}

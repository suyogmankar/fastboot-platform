package io.platform.core.database;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseSizeProfile {

    private String storage;
    private String cpu;
    private String memory;
}
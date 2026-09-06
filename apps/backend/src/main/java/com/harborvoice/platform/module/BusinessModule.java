package com.harborvoice.platform.module;

import java.util.Set;

/** Reviewed, compiled business capability exposed to the platform runtime. */
public interface BusinessModule {
    ModuleDescriptor descriptor();

    Set<String> supportedIntents();

    default Set<ToolCapability> tools() { return Set.of(); }
}

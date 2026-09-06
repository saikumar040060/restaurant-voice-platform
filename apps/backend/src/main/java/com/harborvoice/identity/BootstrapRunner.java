package com.harborvoice.identity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("bootstrap")
public class BootstrapRunner implements ApplicationRunner {
    private final BootstrapService bootstrap;
    private final Environment environment;
    private final ConfigurableApplicationContext context;

    public BootstrapRunner(BootstrapService bootstrap, Environment environment, ConfigurableApplicationContext context) {
        this.bootstrap = bootstrap;
        this.environment = environment;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!"none".equals(environment.getProperty("spring.main.web-application-type"))) {
            throw new IllegalStateException("Bootstrap requires web-application-type=none");
        }
        Path file = Path.of(environment.getRequiredProperty("VOICE_BOOTSTRAP_PASSWORD_FILE"));
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file);
        if (permissions.stream().anyMatch(p -> p.name().startsWith("GROUP_") || p.name().startsWith("OTHERS_"))) {
            throw new IllegalStateException("Bootstrap password file must be readable only by its owner");
        }
        if (Files.size(file) > 74) {
            throw new IllegalArgumentException("Bootstrap password exceeds limit");
        }
        String password = Files.readString(file).replaceFirst("\r?\n$", "");
        bootstrap.initialize(environment.getRequiredProperty("VOICE_BOOTSTRAP_TENANT"),
                environment.getRequiredProperty("VOICE_BOOTSTRAP_USERNAME"), password);
        context.close();
    }
}

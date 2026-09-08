package com.harborvoice.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DashboardAssetsTest {
    private static final Path ROOT = Path.of("src/main/resources/static/dashboard");

    @Test void dashboardUsesOnlySameOriginAssetsAndSessionScopedTokenStorage() throws Exception {
        String html = Files.readString(ROOT.resolve("index.html"));
        String script = Files.readString(ROOT.resolve("dashboard.js"));
        assertThat(html).contains("/dashboard/dashboard.css", "/dashboard/dashboard.js")
                .doesNotContain("http://", "https://", "<script>");
        assertThat(script).contains("sessionStorage", "Authorization")
                .doesNotContain("localStorage", "innerHTML", "eval(");
    }
}

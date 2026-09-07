package com.harborvoice;

import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public liveness probe. It intentionally returns no tenant or provider details. */
@RestController
public class HealthController {
    @GetMapping("/healthz")
    ResponseEntity<Map<String, String>> healthz() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("status", "ok"));
    }
}

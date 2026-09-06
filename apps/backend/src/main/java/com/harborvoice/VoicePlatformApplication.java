package com.harborvoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class)
public class VoicePlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoicePlatformApplication.class, args);
    }
}

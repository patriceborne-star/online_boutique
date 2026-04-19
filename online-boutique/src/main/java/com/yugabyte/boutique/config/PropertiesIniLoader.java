package com.yugabyte.boutique.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads APPLICATION_PORT from properties.ini before Spring Boot configures the server.
 * Registered via META-INF/spring.factories.
 */
public class PropertiesIniLoader implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, String> props = loadPropertiesIni();
        String appPort = props.getOrDefault("APPLICATION_PORT", "");
        if (!appPort.isEmpty()) {
            Map<String, Object> override = new HashMap<>();
            override.put("server.port", appPort);
            environment.getPropertySources().addFirst(
                    new MapPropertySource("propertiesIniPort", override));
        }
    }

    static Map<String, String> loadPropertiesIni() {
        Map<String, String> props = new HashMap<>();

        Path[] candidates = {
            Path.of("../properties.ini"),
            Path.of("properties.ini"),
            Path.of(System.getProperty("user.dir"), "properties.ini"),
            Path.of(System.getProperty("user.dir"), "..", "properties.ini")
        };

        Path found = null;
        for (Path p : candidates) {
            if (Files.exists(p)) {
                found = p;
                break;
            }
        }

        if (found == null) return props;

        try (BufferedReader reader = new BufferedReader(new FileReader(found.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("[")) continue;
                int eq = line.indexOf('=');
                if (eq > 0) {
                    props.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                }
            }
        } catch (Exception e) {
            // silently ignore — DatabaseConfig will also read this file
        }

        return props;
    }
}

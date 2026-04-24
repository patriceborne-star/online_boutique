package com.oracle.boutique.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        Map<String, String> props = loadPropertiesIni();

        String host = props.getOrDefault("DATABASE_HOST", "localhost");
        String port = props.getOrDefault("DATABASE_PORT", "1521");
        String name = props.getOrDefault("DATABASE_NAME", "xe");
        String user = props.getOrDefault("DATABASE_USER", "oracle");
        String pass = props.getOrDefault("DATABASE_PASSWORD", "");

        // jdbc:oracle:thin:@localhost:1521:xe
        String url = String.format("jdbc:oracle:thin:@%s:%s/%s", host, port, name);

        return DataSourceBuilder.create()
                .url(url)
                .username(user)
                .password(pass)
                .driverClassName("oracle.jdbc.driver.OracleDriver")
                .build();
    }

    private Map<String, String> loadPropertiesIni() {
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

        if (found == null) {
            System.out.println("WARN: properties.ini not found, using defaults");
            return props;
        }

        System.out.println("Loading database config from: " + found.toAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(found.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("[")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).trim();

                    String value = line.substring(eq + 1).trim();
                    props.put(key, value);
                }
            }
        } catch (Exception e) {
            System.err.println("WARN: Failed to read properties.ini: " + e.getMessage());
        }

        return props;
    }
}

package com.ct5121.shareit.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final String DATABASE_URL_ENV = "DATABASE_URL";
    private static final String PROPERTY_SOURCE_NAME = "renderDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))
                || environment.containsProperty("spring.datasource.url")) {
            return;
        }

        String databaseUrl = environment.getProperty(DATABASE_URL_ENV);
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        environment.getPropertySources().addFirst(
                new MapPropertySource(PROPERTY_SOURCE_NAME, toDatasourceProperties(databaseUrl))
        );
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private Map<String, Object> toDatasourceProperties(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (!"postgresql".equalsIgnoreCase(scheme) && !"postgres".equalsIgnoreCase(scheme)) {
            throw new IllegalStateException("DATABASE_URL must use the postgresql:// scheme");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("DATABASE_URL must include a host");
        }

        String databaseName = uri.getPath();
        if (databaseName == null || databaseName.isBlank() || "/".equals(databaseName)) {
            throw new IllegalStateException("DATABASE_URL must include a database name");
        }

        String rawUserInfo = uri.getRawUserInfo();
        if (rawUserInfo == null || rawUserInfo.isBlank()) {
            throw new IllegalStateException("DATABASE_URL must include database credentials");
        }

        int separator = rawUserInfo.indexOf(':');
        String username = separator >= 0 ? rawUserInfo.substring(0, separator) : rawUserInfo;
        String password = separator >= 0 ? rawUserInfo.substring(separator + 1) : "";

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", buildJdbcUrl(uri, host, databaseName));
        properties.put("spring.datasource.username", decode(username));
        properties.put("spring.datasource.password", decode(password));
        return properties;
    }

    private String buildJdbcUrl(URI uri, String host, String databaseName) {
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
        jdbcUrl.append(formatHost(host));
        if (uri.getPort() > 0) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        jdbcUrl.append(databaseName);
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }
        return jdbcUrl.toString();
    }

    private String formatHost(String host) {
        return host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

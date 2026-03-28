package com.ct5121.shareit.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesBindingTest {
    private static final String SECRET = "test-signing-secret-key-with-32-bytes";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldBindJwtPropertiesFromRenderEnvironmentVariables() {
        contextRunner
                .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                        new SystemEnvironmentPropertySource(
                                "renderEnv",
                                Map.of(
                                        "APP_SECURITY_JWT_SECRET", SECRET,
                                        "APP_SECURITY_JWT_EXPIRATION", "24h"))))
                .run(context -> {
                    assertThat(context).hasNotFailed();

                    JwtProperties properties = context.getBean(JwtProperties.class);
                    assertThat(properties.getSecret()).isEqualTo(SECRET);
                    assertThat(properties.getExpiration()).isEqualTo(Duration.ofHours(24));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(JwtProperties.class)
    static class TestConfig {
    }
}

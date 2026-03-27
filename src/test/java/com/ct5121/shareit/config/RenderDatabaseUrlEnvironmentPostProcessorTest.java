package com.ct5121.shareit.config;

import com.ct5121.shareit.ShareItApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class RenderDatabaseUrlEnvironmentPostProcessorTest {
    private final RenderDatabaseUrlEnvironmentPostProcessor processor =
            new RenderDatabaseUrlEnvironmentPostProcessor();

    @Test
    void shouldTranslateRenderDatabaseUrlForProdProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty(
                "DATABASE_URL",
                "postgresql://shareit%40user:pa%3Ass@db.internal:5432/shareit?sslmode=require"
        );

        processor.postProcessEnvironment(environment, new SpringApplication(ShareItApp.class));

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.internal:5432/shareit?sslmode=require");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("shareit@user");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("pa:ss");
    }

    @Test
    void shouldIgnoreRenderDatabaseUrlOutsideProdProfile() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        environment.setProperty("DATABASE_URL", "postgresql://user:password@db.internal:5432/shareit");

        processor.postProcessEnvironment(environment, new SpringApplication(ShareItApp.class));

        assertThat(environment.getProperty("spring.datasource.url")).isNull();
        assertThat(environment.getProperty("spring.datasource.username")).isNull();
        assertThat(environment.getProperty("spring.datasource.password")).isNull();
    }
}

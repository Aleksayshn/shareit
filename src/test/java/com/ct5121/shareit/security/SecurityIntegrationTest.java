package com.ct5121.shareit.security;

import com.ct5121.shareit.booking.repository.BookingRepository;
import com.ct5121.shareit.comment.repository.CommentRepository;
import com.ct5121.shareit.item.repository.ItemRepository;
import com.ct5121.shareit.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldAllowAnonymousAccessToDocsAndSearch() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRegisterAndLoginAnonymously() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("alice@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("alice@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAcceptBearerTokenForProtectedEndpoints() throws Exception {
        String token = registerAndExtractToken("bob@example.com");

        mockMvc.perform(get("/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@example.com"));

        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("bob@example.com"));
    }

    @Test
    void shouldIgnoreInvalidBearerTokenOnPublicEndpoints() throws Exception {
        registerAndExtractToken("carol@example.com");

        mockMvc.perform(post("/auth/login")
                        .header(HttpHeaders.AUTHORIZATION, bearer("invalid-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("carol@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(get("/items/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer("invalid-token"))
                        .param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidBearerTokenOnProtectedEndpoints() throws Exception {
        registerAndExtractToken("dave@example.com");

        mockMvc.perform(get("/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer("invalid-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDescribeSecurityInOpenApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode openApi = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(openApi.at("/components/securitySchemes/bearerAuth/type").asText()).isEqualTo("http");
        assertThat(openApi.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(openApi.at("/components/securitySchemes/bearerAuth/bearerFormat").asText()).isEqualTo("JWT");
        assertThatSecurityIsOpen(openApi, "/paths/~1auth~1register/post/security");
        assertThatSecurityIsOpen(openApi, "/paths/~1auth~1login/post/security");
        assertThatSecurityIsOpen(openApi, "/paths/~1items~1search/get/security");
        assertThat(openApi.at("/paths/~1auth~1me/get/security/0/bearerAuth").isArray()).isTrue();
        assertThat(openApi.at("/paths/~1bookings/get/security/0/bearerAuth").isArray()).isTrue();
        assertThat(openApi.at("/paths/~1users/get/security/0/bearerAuth").isArray()).isTrue();
    }

    private String registerAndExtractToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(email)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String registerPayload(String email) {
        return (
                "{\n"
                        + "  \"name\": \"Test User\",\n"
                        + "  \"email\": \"%s\",\n"
                        + "  \"password\": \"%s\"\n"
                        + "}\n")
                .formatted(email, PASSWORD);
    }

    private String loginPayload(String email) {
        return (
                "{\n"
                        + "  \"email\": \"%s\",\n"
                        + "  \"password\": \"%s\"\n"
                        + "}\n")
                .formatted(email, PASSWORD);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void assertThatSecurityIsOpen(JsonNode openApi, String pointer) {
        JsonNode security = openApi.at(pointer);
        assertThat(security.isMissingNode() || security.isEmpty()).isTrue();
    }
}

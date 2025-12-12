package com.acainfo.shared.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base class for E2E integration tests.
 * Provides common configuration and utility methods for all E2E tests.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Full Spring Boot context with MockMvc</li>
 *   <li>H2 in-memory database with PostgreSQL compatibility mode</li>
 *   <li>Automatic rollback after each test via @Transactional</li>
 *   <li>JSON serialization utilities</li>
 *   <li>Common HTTP request helpers</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseE2ETest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestAuthHelper authHelper;

    @Autowired
    protected TestDataHelper dataHelper;

    // ===========================================
    // JSON Serialization Utilities
    // ===========================================

    /**
     * Serialize object to JSON string.
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Deserialize JSON string to object.
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Deserialize MvcResult response body to object.
     */
    protected <T> T fromResponse(MvcResult result, Class<T> clazz) throws Exception {
        return fromJson(result.getResponse().getContentAsString(), clazz);
    }

    // ===========================================
    // HTTP Request Helpers
    // ===========================================

    /**
     * Perform GET request without authentication.
     */
    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Perform GET request with Bearer token authentication.
     */
    protected ResultActions performGet(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Perform POST request without authentication.
     */
    protected ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    /**
     * Perform POST request with Bearer token authentication.
     */
    protected ResultActions performPost(String url, Object body, String token) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    /**
     * Perform POST request with Bearer token authentication and no body.
     */
    protected ResultActions performPost(String url, String token) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Perform PUT request without authentication.
     */
    protected ResultActions performPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    /**
     * Perform PUT request with Bearer token authentication.
     */
    protected ResultActions performPut(String url, Object body, String token) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    /**
     * Perform DELETE request without authentication.
     */
    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Perform DELETE request with Bearer token authentication.
     */
    protected ResultActions performDelete(String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON));
    }

    // ===========================================
    // Request Builder Helpers (for complex cases)
    // ===========================================

    /**
     * Create authenticated GET request builder.
     */
    protected MockHttpServletRequestBuilder authenticatedGet(String url, String token) {
        return get(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Create authenticated POST request builder.
     */
    protected MockHttpServletRequestBuilder authenticatedPost(String url, String token) {
        return post(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Create authenticated PUT request builder.
     */
    protected MockHttpServletRequestBuilder authenticatedPut(String url, String token) {
        return put(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Create authenticated DELETE request builder.
     */
    protected MockHttpServletRequestBuilder authenticatedDelete(String url, String token) {
        return delete(url)
                .header("Authorization", bearerHeader(token))
                .contentType(MediaType.APPLICATION_JSON);
    }

    // ===========================================
    // Authorization Header Helper
    // ===========================================

    /**
     * Create Bearer token header value.
     * Handles the token prefix correctly to match JWT filter expectations.
     * When token-prefix is "Bearer" (without trailing space), we add the space.
     */
    private String bearerHeader(String token) {
        // The JWT filter expects "Bearer<space>token" format
        // Since jwt.token-prefix=Bearer (no trailing space), we add it here
        return "Bearer " + token;
    }
}

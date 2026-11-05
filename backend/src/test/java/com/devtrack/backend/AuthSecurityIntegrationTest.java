package com.devtrack.backend;

import com.devtrack.backend.model.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// No @WithMockUser, user(), csrf(), mocked beans or test-managed transactions.
class AuthSecurityIntegrationTest extends PostgresIntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void registersAndAuthenticatesUsingRealPasswordEncoderAndSession() throws Exception {
        Csrf csrf = csrf(null);
        mvc.perform(withCsrf(post("/auth/register"), csrf)
                        .content(credentials("NewUser@devtrack.com", PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@devtrack.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        AppUser persisted = users.findByEmail("newuser@devtrack.com").orElseThrow();
        assertThat(persisted.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, persisted.getPasswordHash())).isTrue();
        mvc.perform(get("/auth/me").session(csrf.session())).andExpect(status().isUnauthorized());

        MockHttpSession session = login("NEWUSER@devtrack.com", csrf);
        mvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(persisted.getId()))
                .andExpect(jsonPath("$.email").value(persisted.getEmail()));
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"existing@devtrack.com", "unknown@devtrack.com"})
    void invalidCredentialsNeverAuthenticateSession(String email) throws Exception {
        users.saveAndFlush(new AppUser("existing@devtrack.com", passwordEncoder.encode(PASSWORD)));
        Csrf csrf = csrf(null);
        mvc.perform(withCsrf(post("/auth/login"), csrf).content(credentials(email, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/auth/me").session(csrf.session())).andExpect(status().isUnauthorized());
    }

    @Test
    void csrfIsSessionBoundRequiredAndRotatedOnLogin() throws Exception {
        Csrf initial = csrf(null);
        String body = credentials("csrf@devtrack.com", PASSWORD);
        mvc.perform(post("/auth/register").session(initial.session())
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/auth/register").session(initial.session())
                        .header(initial.header(), "invalid-token").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        Csrf otherSession = csrf(null);
        mvc.perform(post("/auth/register").session(otherSession.session())
                        .header(initial.header(), initial.token()).contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        assertThat(users.count()).isZero();

        mvc.perform(withCsrf(post("/auth/register"), initial).content(body)).andExpect(status().isCreated());
        String anonymousSessionId = initial.session().getId();
        MockHttpSession session = login("csrf@devtrack.com", initial);
        assertThat(session.getId()).isNotEqualTo(anonymousSessionId);

        // An old token must stop working after authentication, even in the same session.
        mvc.perform(withCsrf(post("/auth/logout"), initial)).andExpect(status().isForbidden());
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isOk());
        Csrf authenticated = csrf(session);
        mvc.perform(withCsrf(post("/auth/logout"), authenticated)).andExpect(status().isNoContent());
    }

    @Test
    void logoutInvalidatesSessionAndClearsSessionCookie() throws Exception {
        Account account = createAccount("logout@devtrack.com");
        DatabaseSnapshot before = snapshot();
        MockHttpSession session = login(account.email(), csrf(null));

        mvc.perform(withCsrf(post("/auth/logout"), csrf(session)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("JSESSIONID", 0));

        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isUnauthorized());
        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    void deletingAccountRejectsWrongPasswordAndRevokesOtherAuthenticatedSession() throws Exception {
        Account account = createAccount("delete@devtrack.com");
        MockHttpSession first = login(account.email(), csrf(null));
        MockHttpSession second = login(account.email(), csrf(null));
        mvc.perform(get("/auth/me").session(first)).andExpect(status().isOk());
        mvc.perform(get("/auth/me").session(second)).andExpect(status().isOk());
        DatabaseSnapshot before = snapshot();
        Csrf token = csrf(first);

        mvc.perform(withCsrf(delete("/auth/me"), token)
                        .content(mapper.writeValueAsString(Map.of("password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
        assertThat(snapshot()).isEqualTo(before);
        mvc.perform(get("/auth/me").session(first)).andExpect(status().isOk());
        mvc.perform(get("/auth/me").session(second)).andExpect(status().isOk());

        mvc.perform(withCsrf(delete("/auth/me"), token)
                        .content(mapper.writeValueAsString(Map.of("password", PASSWORD))))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("JSESSIONID", 0));
        assertThat(first.isInvalid()).isTrue();
        assertThat(users.existsById(account.id())).isFalse();
        assertThat(blocks.findAllById(account.blockIds())).isEmpty();
        assertThat(problems.findAllById(account.problemIds())).isEmpty();

        mvc.perform(get("/auth/me").session(second)).andExpect(status().isUnauthorized());
        assertThat(second.isInvalid()).isTrue();
        mvc.perform(get("/auth/me").session(first)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void resourcesAreIsolatedInBothDirections(boolean reverse) throws Exception {
        Account first = createAccount("first@devtrack.com");
        Account second = createAccount("second@devtrack.com");
        Account actor = reverse ? second : first;
        Account victim = reverse ? first : second;
        MockHttpSession session = login(actor.email(), csrf(null));
        Csrf csrf = csrf(session);
        DatabaseSnapshot before = snapshot();
        String block = "/study-blocks/" + victim.blockIds().getFirst();
        String problemList = block + "/problems";
        String problem = problemList + "/" + victim.problemIds().getFirst();
        String blockBody = "{\"title\":\"Intrusion\",\"active\":false}";
        String problemBody = "{\"title\":\"Intrusion\",\"difficulty\":\"HARD\",\"solved\":true}";

        mvc.perform(get("/study-blocks").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(actor.blockIds().size()))
                .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(actor.blockIds().stream()
                        .map(Long::intValue).toArray())));
        mvc.perform(get("/study-blocks/" + actor.blockIds().getFirst()).session(session))
                .andExpect(status().isOk());
        mvc.perform(get("/study-blocks/" + actor.blockIds().getFirst() + "/problems/"
                        + actor.problemIds().getFirst()).session(session)).andExpect(status().isOk());

        mvc.perform(get(block).session(session)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(put(block), csrf).content(blockBody)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(delete(block), csrf)).andExpect(status().isNotFound());
        mvc.perform(get(problemList).session(session)).andExpect(status().isNotFound());
        mvc.perform(get(problem).session(session)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(post(problemList), csrf).content(problemBody)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(put(problem), csrf).content(problemBody)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(delete(problem), csrf)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(delete(problemList + "/clear"), csrf)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(post(problemList + "/bulk-delete"), csrf)
                        .content(mapper.writeValueAsString(Map.of("problemIds", victim.problemIds()))))
                .andExpect(status().isNotFound());

        // Using an owned block in the URL must not grant access to another user's problem ID.
        String ownList = "/study-blocks/" + actor.blockIds().getFirst() + "/problems";
        String mixedProblem = ownList + "/" + victim.problemIds().getFirst();
        mvc.perform(get(mixedProblem).session(session)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(put(mixedProblem), csrf).content(problemBody)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(delete(mixedProblem), csrf)).andExpect(status().isNotFound());
        mvc.perform(withCsrf(post(ownList + "/bulk-delete"), csrf)
                        .content(mapper.writeValueAsString(Map.of("problemIds",
                                new Long[]{actor.problemIds().getFirst(), victim.problemIds().getFirst()}))))
                .andExpect(status().isNotFound());
        assertThat(snapshot()).isEqualTo(before);
    }

    private MockHttpSession login(String email, Csrf csrf) throws Exception {
        MvcResult result = mvc.perform(withCsrf(post("/auth/login"), csrf)
                        .content(credentials(email, PASSWORD)))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private Csrf csrf(MockHttpSession session) throws Exception {
        MockHttpServletRequestBuilder request = get("/auth/csrf");
        if (session != null) request.session(session);
        MvcResult result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.path("token").asText()).isNotBlank();
        return new Csrf((MockHttpSession) result.getRequest().getSession(false),
                body.path("headerName").asText(), body.path("token").asText());
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request, Csrf csrf) {
        return request.session(csrf.session()).header(csrf.header(), csrf.token()).contentType(APPLICATION_JSON);
    }

    private String credentials(String email, String password) throws Exception {
        return mapper.writeValueAsString(Map.of("email", email, "password", password));
    }

    private record Csrf(MockHttpSession session, String header, String token) {}
}

package com.devtrack.backend.controller;

import com.devtrack.backend.config.SecurityConfig;
import com.devtrack.backend.repository.AppUserRepository;
import com.devtrack.backend.dto.RegisterRequest;
import com.devtrack.backend.exception.EmailAlreadyExistsException;
import com.devtrack.backend.exception.InvalidCredentialsException;
import com.devtrack.backend.model.AppUser;
import com.devtrack.backend.service.AuthService;
import com.devtrack.backend.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import com.devtrack.backend.security.DevTrackUserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.devtrack.backend.security.WithDevTrackUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MVC slice: authentication decisions are stubbed, while the production filters,
 * session context repository and CSRF repository remain real.
 * These tests do not verify password matching or database persistence.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String EMAIL = "alice@example.com";
    private static final String PASSWORD = "secret123";
    private static final String CONTEXT_KEY =
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AppUserRepository appUserRepository;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private AppUser savedUser;

    @BeforeEach
    void setUp() {
        when(appUserRepository.existsById(101L)).thenReturn(true);
        savedUser = new AppUser(101L, EMAIL, "stored-password-hash");
        ReflectionTestUtils.setField(savedUser, "createdAt",
                LocalDateTime.of(2026, 11, 2, 12, 0));
    }

    @Test
    void registerShouldTrimEmailAndReturnPublicUserWithoutAuthenticating() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(savedUser);
        CsrfSession csrfSession = fetchCsrf(null);

        expectPublicUser(mockMvc.perform(post("/auth/register")
                        .session(csrfSession.session())
                        .header(csrfSession.headerName(), csrfSession.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("  ALICE@EXAMPLE.COM  ", PASSWORD)))
                .andExpect(status().isCreated()))
                .andExpect(unauthenticated());

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).register(captor.capture());
        assertEquals("ALICE@EXAMPLE.COM", captor.getValue().getEmail());
        assertEquals(PASSWORD, captor.getValue().getPassword());
        assertNull(csrfSession.session().getAttribute(CONTEXT_KEY));
        expectError(mockMvc.perform(get("/auth/me").session(csrfSession.session())),
                401, "Unauthorized", "Authentication required", "/auth/me");
        verifyNoInteractions(authenticationManager, userDetailsService);
    }

    @ParameterizedTest
    @MethodSource("invalidRegistrations")
    void registerShouldReturnValidationErrors(String email, String password, String message) throws Exception {
        expectValidationError(post("/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credentials(email, password)),
                message, "/auth/register");
        verifyNoInteractions(authService, authenticationManager);
    }

    static Stream<Arguments> invalidRegistrations() {
        return Stream.of(
                Arguments.of("invalid", PASSWORD, "Email must be valid"),
                Arguments.of("   ", PASSWORD, "Email is required"),
                Arguments.of(null, PASSWORD, "Email is required"),
                Arguments.of(EMAIL, null, "Password is required"),
                Arguments.of(EMAIL, "        ", "Password is required"),
                Arguments.of(EMAIL, "1234567", "Password must be between 8 and 72 characters"),
                Arguments.of(EMAIL, "x".repeat(73), "Password must be between 8 and 72 characters")
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 72})
    void registerShouldAcceptPasswordLengthBoundaries(int length) throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(savedUser);

        mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(EMAIL, "x".repeat(length))))
                .andExpect(status().isCreated());

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).register(captor.capture());
        assertEquals("x".repeat(length), captor.getValue().getPassword());
    }

    @Test
    void registerShouldReturn409ForDuplicateEmail() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email is already registered"));

        expectError(mockMvc.perform(post("/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials(EMAIL, PASSWORD))),
                409, "Conflict", "Email is already registered", "/auth/register");
        verifyNoInteractions(authenticationManager);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/register", "/auth/login"})
    void authenticationEndpointsShouldRejectMalformedJson(String path) throws Exception {
        expectError(mockMvc.perform(post(path).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{")),
                400, "Bad Request", "Invalid request body", path);
        verifyNoInteractions(authService, authenticationManager);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "alice@example.com", "ALICE@EXAMPLE.COM",
            "  alice@example.com  ", "  ALICE@EXAMPLE.COM  "
    })
    void loginShouldSubmitNormalizedEmailAndUnchangedPasswordToAuthenticationManager(String email) throws Exception {
        givenSuccessfulAuthentication();

        expectPublicUser(mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, "  SeCrEt123  ")))
                .andExpect(status().isOk()))
                .andExpect(authenticated().withUsername(EMAIL));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals(EMAIL, captor.getValue().getName());
        assertEquals("  SeCrEt123  ", captor.getValue().getCredentials());
        assertFalse(captor.getValue().isAuthenticated());
        verify(authService).getUserByEmail(EMAIL);
        verifyNoInteractions(userDetailsService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void loginShouldReturnSame401ForWrongPasswordAndUnknownUser(boolean unknownUser) throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(unknownUser
                        ? new UsernameNotFoundException("Internal user lookup details")
                        : new BadCredentialsException("Internal password details"));
        CsrfSession csrfSession = fetchCsrf(null);

        expectError(mockMvc.perform(post("/auth/login")
                        .session(csrfSession.session())
                        .header(csrfSession.headerName(), csrfSession.token())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials(EMAIL, PASSWORD))),
                401, "Unauthorized", "Invalid email or password", "/auth/login")
                .andExpect(unauthenticated());

        assertNull(csrfSession.session().getAttribute(CONTEXT_KEY));
        expectError(mockMvc.perform(get("/auth/me").session(csrfSession.session())),
                401, "Unauthorized", "Authentication required", "/auth/me");
        verifyNoInteractions(authService, userDetailsService);
    }

    @ParameterizedTest
    @MethodSource("invalidLogins")
    void loginShouldValidateCredentialsBeforeCallingAuthenticationManager(
            String email, String password, String message) throws Exception {
        expectValidationError(post("/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(credentials(email, password)),
                message, "/auth/login");
        verifyNoInteractions(authService, authenticationManager);
    }

    static Stream<Arguments> invalidLogins() {
        return Stream.of(
                Arguments.of("invalid", PASSWORD, "Email must be valid"),
                Arguments.of("   ", PASSWORD, "Email is required"),
                Arguments.of(EMAIL, null, "Password is required"),
                Arguments.of(EMAIL, "   ", "Password is required")
        );
    }

    @Test
    @WithDevTrackUser
    void meShouldReturnPublicUserForSimulatedAuthenticatedPrincipal() throws Exception {
        when(authService.getUserByEmail(EMAIL)).thenReturn(savedUser);

        expectPublicUser(mockMvc.perform(get("/auth/me")).andExpect(status().isOk()));

        verify(authService).getUserByEmail(EMAIL);
        verifyNoInteractions(authenticationManager, userDetailsService);
    }

    @Test
    void meShouldReturn401WhenAnonymous() throws Exception {
        expectError(mockMvc.perform(get("/auth/me")),
                401, "Unauthorized", "Authentication required", "/auth/me");
        verifyNoInteractions(authService, authenticationManager);
    }

    @Test
    void logoutShouldReturn401WhenAnonymousWithValidCsrf() throws Exception {
        expectError(mockMvc.perform(post("/auth/logout").with(csrf())),
                401, "Unauthorized", "Authentication required", "/auth/logout");
        verifyNoInteractions(authService, authenticationManager);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/register", "/auth/login", "/auth/logout"})
    void writesShouldReturn403WithoutCsrf(String path) throws Exception {
        MockHttpServletRequestBuilder request = post(path)
                .contentType(MediaType.APPLICATION_JSON).content(credentials(EMAIL, PASSWORD));
        if (path.equals("/auth/logout")) {
            request.with(user(new DevTrackUserDetails(101L, EMAIL, "stored-password-hash")));
        }

        expectError(mockMvc.perform(request), 403, "Forbidden", "Access denied", path);

        verifyNoInteractions(authService, authenticationManager);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/register", "/auth/login", "/auth/logout"})
    void writesShouldReturn403WithInvalidCsrf(String path) throws Exception {
        MockHttpServletRequestBuilder request = post(path).with(csrf().useInvalidToken())
                .contentType(MediaType.APPLICATION_JSON).content(credentials(EMAIL, PASSWORD));
        if (path.equals("/auth/logout")) {
            request.with(user(EMAIL));
        }

        expectError(mockMvc.perform(request), 403, "Forbidden", "Access denied", path);

        verifyNoInteractions(authService, authenticationManager);
    }

    @Test
    void csrfShouldBePublicAndCreateSessionWithUsableToken() throws Exception {
        CsrfSession csrfSession = fetchCsrf(null);

        assertNotNull(csrfSession.session());
        assertEquals("X-CSRF-TOKEN", csrfSession.headerName());
        assertFalse(csrfSession.token().isBlank());
        assertNull(csrfSession.session().getAttribute(CONTEXT_KEY));
        when(authService.register(any(RegisterRequest.class))).thenReturn(savedUser);
        mockMvc.perform(post("/auth/register").session(csrfSession.session())
                        .header(csrfSession.headerName(), csrfSession.token())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials(EMAIL, PASSWORD)))
                .andExpect(status().isCreated());
    }

    @Test
    void csrfShouldRejectTokenFromAnotherSession() throws Exception {
        CsrfSession first = fetchCsrf(null);
        CsrfSession second = fetchCsrf(null);

        expectError(mockMvc.perform(post("/auth/login").session(second.session())
                        .header(first.headerName(), first.token())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials(EMAIL, PASSWORD))),
                403, "Forbidden", "Access denied", "/auth/login");
        verifyNoInteractions(authService, authenticationManager);
    }

    @Test
    void loginShouldPersistSecurityContextAndAllowMeInLaterRequest() throws Exception {
        CsrfSession loggedIn = loginWithStubbedAuthenticationManager();
        SecurityContext context = (SecurityContext) loggedIn.session().getAttribute(CONTEXT_KEY);

        assertNotNull(context);
        assertTrue(context.getAuthentication().isAuthenticated());
        assertEquals(EMAIL, context.getAuthentication().getName());
        expectPublicUser(mockMvc.perform(get("/auth/me").session(loggedIn.session()))
                .andExpect(status().isOk()))
                .andExpect(authenticated().withUsername(EMAIL));
        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(authService, times(2)).getUserByEmail(EMAIL);
    }

    @Test
    void loginShouldChangeExistingSessionId() throws Exception {
        givenSuccessfulAuthentication();
        CsrfSession before = fetchCsrf(null);
        String previousId = before.session().getId();

        MvcResult result = performLogin(before).andExpect(status().isOk()).andReturn();

        assertSame(before.session(), result.getRequest().getSession(false));
        assertNotEquals(previousId, before.session().getId());
    }

    @Test
    void loginShouldRejectPreLoginCsrfAndAcceptFreshToken() throws Exception {
        CsrfSession loggedIn = loginWithStubbedAuthenticationManager();

        expectError(mockMvc.perform(post("/auth/logout").session(loggedIn.session())
                        .header(loggedIn.headerName(), loggedIn.token())),
                403, "Forbidden", "Access denied", "/auth/logout");
        assertFalse(loggedIn.session().isInvalid());

        CsrfSession fresh = fetchCsrf(loggedIn.session());
        mockMvc.perform(post("/auth/logout").session(fresh.session())
                        .header(fresh.headerName(), fresh.token()))
                .andExpect(status().isNoContent());
        assertTrue(fresh.session().isInvalid());
    }

    @Test
    void logoutShouldInvalidateSessionAndExpireSessionCookie() throws Exception {
        CsrfSession loggedIn = loginWithStubbedAuthenticationManager();
        CsrfSession fresh = fetchCsrf(loggedIn.session());

        mockMvc.perform(post("/auth/logout").session(fresh.session())
                        .header(fresh.headerName(), fresh.token()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(cookie().maxAge("JSESSIONID", 0))
                .andExpect(cookie().value("JSESSIONID", emptyOrNullString()))
                .andExpect(unauthenticated());

        assertTrue(fresh.session().isInvalid());
        expectError(mockMvc.perform(get("/auth/me")),
                401, "Unauthorized", "Authentication required", "/auth/me");
    }

    @Test
    void logoutWithoutCsrfShouldPreserveAuthenticatedSession() throws Exception {
        CsrfSession loggedIn = loginWithStubbedAuthenticationManager();

        expectError(mockMvc.perform(post("/auth/logout").session(loggedIn.session())),
                403, "Forbidden", "Access denied", "/auth/logout");

        assertFalse(loggedIn.session().isInvalid());
        expectPublicUser(mockMvc.perform(get("/auth/me").session(loggedIn.session()))
                .andExpect(status().isOk()))
                .andExpect(authenticated().withUsername(EMAIL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"secret123", "  SeCrEt123  "})
    void deleteAccountShouldReturn204InvalidateCurrentSessionAndExpireCookie(String password) throws Exception {
        CsrfSession loggedIn = loginWithStubbedAuthenticationManager();
        CsrfSession fresh = fetchCsrf(loggedIn.session());

        mockMvc.perform(delete("/auth/me").session(fresh.session())
                        .header(fresh.headerName(), fresh.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                objectMapper.createObjectNode().put("password", password))))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(cookie().maxAge("JSESSIONID", 0))
                .andExpect(cookie().value("JSESSIONID", emptyOrNullString()))
                .andExpect(unauthenticated());

        verify(authService).deleteAccount(password);
        assertTrue(fresh.session().isInvalid());
        expectError(mockMvc.perform(get("/auth/me")),
                401, "Unauthorized", "Authentication required", "/auth/me");
    }

    @Test
    void deleteAccountShouldReturn401AndKeepSessionWhenPasswordIsWrong() throws Exception {
        CsrfSession loggedIn = loginWithStubbedAuthenticationManager();
        CsrfSession fresh = fetchCsrf(loggedIn.session());
        doThrow(new InvalidCredentialsException()).when(authService).deleteAccount("wrong-password");

        expectError(mockMvc.perform(delete("/auth/me").session(fresh.session())
                        .header(fresh.headerName(), fresh.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"wrong-password\"}")),
                401, "Unauthorized", "Invalid email or password", "/auth/me");

        assertFalse(fresh.session().isInvalid());
        expectPublicUser(mockMvc.perform(get("/auth/me").session(fresh.session()))
                .andExpect(status().isOk()))
                .andExpect(authenticated().withUsername(EMAIL));
        verify(authService).deleteAccount("wrong-password");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"password\":null}", "{\"password\":\"\"}", "{\"password\":\"   \"}"})
    @WithDevTrackUser
    void deleteAccountShouldValidateRequiredPassword(String body) throws Exception {
        expectValidationError(delete("/auth/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body),
                "Password is required", "/auth/me");

        verifyNoInteractions(authService, authenticationManager);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "null", "[]", ""})
    @WithDevTrackUser
    void deleteAccountShouldRejectMalformedOrMissingJsonBody(String body) throws Exception {
        expectError(mockMvc.perform(delete("/auth/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body)),
                400, "Bad Request", "Invalid request body", "/auth/me");

        verifyNoInteractions(authService, authenticationManager);
    }

    @Test
    void deleteAccountShouldRequireAuthenticationEvenWithValidCsrf() throws Exception {
        expectError(mockMvc.perform(delete("/auth/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"secret123\"}")),
                401, "Unauthorized", "Authentication required", "/auth/me");

        verifyNoInteractions(authService, authenticationManager);
    }

    @Test
    @WithDevTrackUser
    void deleteAccountShouldRequireCsrf() throws Exception {
        expectError(mockMvc.perform(delete("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"secret123\"}")),
                403, "Forbidden", "Access denied", "/auth/me");

        verifyNoInteractions(authService, authenticationManager);
    }

    @Test
    @WithDevTrackUser
    void deleteAccountShouldRejectInvalidCsrf() throws Exception {
        expectError(mockMvc.perform(delete("/auth/me").with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"secret123\"}")),
                403, "Forbidden", "Access denied", "/auth/me");

        verifyNoInteractions(authService, authenticationManager);
    }

    @Test
    void deleteAccountShouldRejectDeletedAccountBeforeCallingService() throws Exception {
        when(appUserRepository.existsById(101L)).thenReturn(false);
        MockHttpSession session = new MockHttpSession();

        expectError(mockMvc.perform(delete("/auth/me").session(session)
                        .with(user(new DevTrackUserDetails(101L, EMAIL, "stored-password-hash")))
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"secret123\"}")),
                401, "Unauthorized", "Authentication required", "/auth/me");

        assertTrue(session.isInvalid());
        verify(appUserRepository).existsById(101L);
        verifyNoInteractions(authService, authenticationManager);
    }

    @Test
    void deletedAccountShouldBeRejectedOnNextRequestFromAnotherSession() throws Exception {
        CsrfSession first = loginWithStubbedAuthenticationManager();
        CsrfSession second = loginWithStubbedAuthenticationManager();
        CsrfSession fresh = fetchCsrf(first.session());
        // Simulate persistence reporting the account as removed after the service returns.
        doAnswer(invocation -> {
            when(appUserRepository.existsById(101L)).thenReturn(false);
            return null;
        }).when(authService).deleteAccount(PASSWORD);

        mockMvc.perform(delete("/auth/me").session(fresh.session())
                        .header(fresh.headerName(), fresh.token())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"secret123\"}"))
                .andExpect(status().isNoContent());

        assertTrue(first.session().isInvalid());
        assertFalse(second.session().isInvalid());
        expectError(mockMvc.perform(get("/auth/me").session(second.session())),
                401, "Unauthorized", "Authentication required", "/auth/me");
        assertTrue(second.session().isInvalid());
        verify(authService).deleteAccount(PASSWORD);
        verify(authService, times(2)).getUserByEmail(EMAIL);
    }

    // Stub only the authentication decision. Do not inject a mock principal into these requests:
    // the controller must save the context itself and subsequent requests must reload the session.
    private void givenSuccessfulAuthentication() {
        var principal = new DevTrackUserDetails(101L, EMAIL, "stored-password-hash");
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authService.getUserByEmail(EMAIL)).thenReturn(savedUser);
    }

    private CsrfSession loginWithStubbedAuthenticationManager() throws Exception {
        givenSuccessfulAuthentication();
        CsrfSession before = fetchCsrf(null);
        performLogin(before).andExpect(status().isOk()).andExpect(authenticated().withUsername(EMAIL));
        return before;
    }

    private ResultActions performLogin(CsrfSession csrfSession) throws Exception {
        return mockMvc.perform(post("/auth/login").session(csrfSession.session())
                .header(csrfSession.headerName(), csrfSession.token())
                .contentType(MediaType.APPLICATION_JSON).content(credentials(EMAIL, PASSWORD)));
    }

    private CsrfSession fetchCsrf(MockHttpSession session) throws Exception {
        MockHttpServletRequestBuilder request = get("/auth/csrf");
        if (session != null) {
            request.session(session);
        }
        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.parameterName").value("_csrf"))
                .andReturn();
        JsonNode token = objectMapper.readTree(result.getResponse().getContentAsString());
        return new CsrfSession((MockHttpSession) result.getRequest().getSession(false),
                token.get("headerName").asText(), token.get("token").asText());
    }

    private String credentials(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(objectMapper.createObjectNode()
                .put("email", email).put("password", password));
    }

    private ResultActions expectPublicUser(ResultActions result) throws Exception {
        return result.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.createdAt").value("2026-11-02T12:00:00"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    private void expectValidationError(
            MockHttpServletRequestBuilder request, String message, String path) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString(message)))
                .andExpect(jsonPath("$.path").value(path));
    }

    private ResultActions expectError(
            ResultActions result, int status, String error, String message, String path) throws Exception {
        return result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.path").value(path));
    }

    private record CsrfSession(MockHttpSession session, String headerName, String token) {}
}

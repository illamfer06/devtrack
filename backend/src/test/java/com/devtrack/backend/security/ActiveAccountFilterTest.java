package com.devtrack.backend.security;

import com.devtrack.backend.repository.AppUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveAccountFilterTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private FilterChain filterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ActiveAccountFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new ActiveAccountFilter(appUserRepository, objectMapper);
        request = new MockHttpServletRequest("GET", "/study-blocks/7");
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void existingAccountShouldContinueWithAuthenticationAndSessionIntact() throws Exception {
        Authentication authentication = authenticate(new DevTrackUserDetails(101L, "alice@example.com", "hash"));
        MockHttpSession session = storeContextInSession();
        when(appUserRepository.existsById(101L)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoMoreInteractions(filterChain);
        verify(appUserRepository).existsById(101L);
        verifyNoMoreInteractions(appUserRepository);
        assertSame(authentication, SecurityContextHolder.getContext().getAuthentication());
        assertSame(session, request.getSession(false));
        assertFalse(session.isInvalid());
        assertNotNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        assertEquals(200, response.getStatus());
        assertEquals("", response.getContentAsString());
    }

    @Test
    void deletedAccountShouldReturnErrorInvalidateSessionAndNeverContinue() throws Exception {
        authenticate(new DevTrackUserDetails(101L, "alice@example.com", "hash"));
        MockHttpSession session = storeContextInSession();
        when(appUserRepository.existsById(101L)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertRejected();
        assertTrue(session.isInvalid());
        verify(appUserRepository).existsById(101L);
        verifyNoMoreInteractions(appUserRepository);
    }

    @Test
    void deletedAccountWithoutSessionShouldNotCreateOne() throws Exception {
        authenticate(new DevTrackUserDetails(101L, "alice@example.com", "hash"));
        when(appUserRepository.existsById(101L)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertRejected();
        assertNull(request.getSession(false));
        verify(appUserRepository).existsById(101L);
    }

    @Test
    void missingAuthenticationShouldContinueWithoutLookingUpAnAccount() throws Exception {
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(appUserRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void anonymousAuthenticationShouldContinueWithoutLookingUpAnAccount() throws Exception {
        var anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(appUserRepository);
        assertSame(anonymous, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void unauthenticatedTokenShouldContinueWithoutLookingUpAnAccount() throws Exception {
        var token = UsernamePasswordAuthenticationToken.unauthenticated("alice@example.com", "password");
        SecurityContextHolder.getContext().setAuthentication(token);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(appUserRepository);
        assertSame(token, SecurityContextHolder.getContext().getAuthentication());
    }

    @ParameterizedTest
    @ValueSource(strings = {"springUser", "string", "nullPrincipal", "missingId"})
    void unexpectedAuthenticatedIdentityShouldBeRejectedWithoutRepositoryLookup(String identity) throws Exception {
        Object principal = switch (identity) {
            case "springUser" -> User.withUsername("alice@example.com").password("hash").roles("USER").build();
            case "string" -> "alice@example.com";
            case "nullPrincipal" -> null;
            case "missingId" -> new DevTrackUserDetails(null, "alice@example.com", "hash");
            default -> throw new IllegalArgumentException(identity);
        };
        authenticate(principal);
        MockHttpSession session = storeContextInSession();

        filter.doFilter(request, response, filterChain);

        assertRejected();
        assertTrue(session.isInvalid());
        verifyNoInteractions(appUserRepository);
    }

    private Authentication authenticate(Object principal) {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private MockHttpSession storeContextInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
        request.setSession(session);
        return session;
    }

    private void assertRejected() throws Exception {
        assertEquals(401, response.getStatus());
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(
                MediaType.parseMediaType(response.getContentType())));
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals(objectMapper.readTree("""
                {"status":401,"error":"Unauthorized","message":"Authentication required","path":"/study-blocks/7"}
                """), objectMapper.readTree(response.getContentAsString()));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(filterChain);
    }
}

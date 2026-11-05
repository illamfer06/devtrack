package com.devtrack.backend.service;

import com.devtrack.backend.dto.RegisterRequest;
import com.devtrack.backend.exception.EmailAlreadyExistsException;
import com.devtrack.backend.model.AppUser;
import com.devtrack.backend.repository.AppUserRepository;
import com.devtrack.backend.repository.ProblemRepository;
import com.devtrack.backend.repository.StudyBlockRepository;
import com.devtrack.backend.security.DevTrackUserDetails;
import com.devtrack.backend.exception.InvalidCredentialsException;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private StudyBlockRepository studyBlockRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @InjectMocks
    private AuthService authService;

    @ParameterizedTest
    @ValueSource(strings = {
            "alice@example.com", "ALICE@EXAMPLE.COM",
            "  alice@example.com  ", "  ALICE@EXAMPLE.COM  "
    })
    void registerShouldNormalizeEmailAndSaveEncodedPassword(String email) {
        RegisterRequest request = registration(email, "secret123");
        AppUser savedUser = new AppUser(101L, "alice@example.com", "encoded-password");
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

        AppUser result = authService.register(request);

        assertSame(savedUser, result);
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).existsByEmail("alice@example.com");
        verify(passwordEncoder).encode("secret123");
        verify(appUserRepository).save(captor.capture());
        AppUser submittedUser = captor.getValue();
        assertNull(submittedUser.getId());
        assertEquals("alice@example.com", submittedUser.getEmail());
        assertEquals("encoded-password", submittedUser.getPasswordHash());
        assertNotEquals(request.getPassword(), submittedUser.getPasswordHash());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "alice@example.com", "ALICE@EXAMPLE.COM",
            "  alice@example.com  ", "  ALICE@EXAMPLE.COM  "
    })
    void registerShouldRejectDuplicateNormalizedEmailWithoutEncodingOrSaving(String email) {
        when(appUserRepository.existsByEmail("alice@example.com")).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(registration(email, "secret123")));

        assertEquals("Email is already registered", exception.getMessage());
        verify(appUserRepository).existsByEmail("alice@example.com");
        verifyNoMoreInteractions(appUserRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerShouldPassPasswordToEncoderWithoutTrimmingOrChangingCase() {
        RegisterRequest request = registration("alice@example.com", "  SeCrEt123  ");
        when(passwordEncoder.encode("  SeCrEt123  ")).thenReturn("encoded-with-spaces");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser result = authService.register(request);

        assertEquals("encoded-with-spaces", result.getPasswordHash());
        verify(passwordEncoder).encode("  SeCrEt123  ");
    }

    @Test
    void getUserByEmailShouldReturnExistingUser() {
        AppUser user = new AppUser(101L, "alice@example.com", "encoded-password");
        when(appUserRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertSame(user, authService.getUserByEmail("alice@example.com"));

        verify(appUserRepository).findByEmail("alice@example.com");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void getUserByEmailShouldRejectMissingUser() {
        when(appUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authService.getUserByEmail("missing@example.com"));

        assertEquals("User not found", exception.getMessage());
        verify(appUserRepository).findByEmail("missing@example.com");
        verifyNoInteractions(passwordEncoder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"secret123", "  SeCrEt123  "})
    void deleteAccountShouldCheckPasswordBeforeDeletingOnlyCurrentUserInOrder(String password) {
        givenAuthenticatedAccount();
        when(passwordEncoder.matches(password, "stored-password-hash")).thenReturn(true);

        authService.deleteAccount(password);

        var order = inOrder(appUserRepository, passwordEncoder, problemRepository, studyBlockRepository);
        order.verify(appUserRepository).findByEmail("alice@example.com");
        order.verify(passwordEncoder).matches(password, "stored-password-hash");
        order.verify(problemRepository).deleteAllByUserId(101L);
        order.verify(studyBlockRepository).deleteAllByUserId(101L);
        order.verify(appUserRepository).deleteById(101L);
        order.verifyNoMoreInteractions();
    }

    @Test
    void deleteAccountShouldNotDeleteAnythingWhenPasswordIsWrong() {
        givenAuthenticatedAccount();
        when(passwordEncoder.matches("wrong-password", "stored-password-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.deleteAccount("wrong-password"));

        verify(appUserRepository).findByEmail("alice@example.com");
        verifyNoMoreInteractions(appUserRepository);
        verifyNoInteractions(problemRepository, studyBlockRepository);
        verify(passwordEncoder).matches("wrong-password", "stored-password-hash");
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void deleteAccountShouldNotDeleteAnythingWhenCurrentUserDoesNotExist() {
        authenticate();
        when(appUserRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.deleteAccount("secret123"));

        verify(appUserRepository).findByEmail("alice@example.com");
        verifyNoMoreInteractions(appUserRepository);
        verifyNoInteractions(passwordEncoder, problemRepository, studyBlockRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"problems", "studyBlocks"})
    void deleteAccountShouldStopAndPropagateRepositoryFailure(String failingRepository) {
        givenAuthenticatedAccount();
        when(passwordEncoder.matches("secret123", "stored-password-hash")).thenReturn(true);
        RuntimeException failure = new IllegalStateException("Delete failed");
        if (failingRepository.equals("problems")) {
            when(problemRepository.deleteAllByUserId(101L)).thenThrow(failure);
        } else {
            when(studyBlockRepository.deleteAllByUserId(101L)).thenThrow(failure);
        }

        assertSame(failure, assertThrows(RuntimeException.class,
                () -> authService.deleteAccount("secret123")));

        var order = inOrder(appUserRepository, passwordEncoder, problemRepository, studyBlockRepository);
        order.verify(appUserRepository).findByEmail("alice@example.com");
        order.verify(passwordEncoder).matches("secret123", "stored-password-hash");
        order.verify(problemRepository).deleteAllByUserId(101L);
        if (failingRepository.equals("studyBlocks")) {
            order.verify(studyBlockRepository).deleteAllByUserId(101L);
        } else {
            verifyNoInteractions(studyBlockRepository);
        }
        order.verifyNoMoreInteractions();
        verify(appUserRepository, never()).deleteById(anyLong());
    }

    private void givenAuthenticatedAccount() {
        authenticate();
        when(appUserRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new AppUser(101L, "alice@example.com", "stored-password-hash")));
    }

    private void authenticate() {
        var principal = new DevTrackUserDetails(101L, "alice@example.com", "stored-password-hash");
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    private RegisterRequest registration(String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}

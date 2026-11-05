package com.devtrack.backend.service;

import com.devtrack.backend.model.AppUser;
import com.devtrack.backend.repository.AppUserRepository;
import com.devtrack.backend.security.DevTrackUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameShouldExposeStoredHashAndUserAuthority() {
        AppUser user = new AppUser(101L, "alice@example.com", "stored-password-hash");
        when(appUserRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("alice@example.com");

        DevTrackUserDetails principal = assertInstanceOf(DevTrackUserDetails.class, result);
        assertEquals(101L, principal.getId());
        assertEquals("alice@example.com", result.getUsername());
        assertEquals("stored-password-hash", result.getPassword());
        assertEquals(List.of("ROLE_USER"),
                result.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        verify(appUserRepository).findByEmail("alice@example.com");
    }

    @Test
    void loadUserByUsernameShouldRejectMissingUser() {
        when(appUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@example.com"));

        assertEquals("User not found", exception.getMessage());
        verify(appUserRepository).findByEmail("missing@example.com");
        verifyNoMoreInteractions(appUserRepository);
    }
}

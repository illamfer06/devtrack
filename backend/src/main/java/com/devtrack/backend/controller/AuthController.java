package com.devtrack.backend.controller;

import com.devtrack.backend.dto.DeleteAccountRequest;
import com.devtrack.backend.dto.LoginRequest;
import com.devtrack.backend.dto.RegisterRequest;
import com.devtrack.backend.dto.UserResponse;
import com.devtrack.backend.exception.InvalidCredentialsException;
import com.devtrack.backend.model.AppUser;
import com.devtrack.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AppUser user = authService.register(request);

        UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
                email,
                request.getPassword());

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(credentials);
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        ChangeSessionIdAuthenticationStrategy sessionStrategy =
                new ChangeSessionIdAuthenticationStrategy();

        sessionStrategy.onAuthentication(
                authentication,
                httpRequest,
                httpResponse);

        CsrfAuthenticationStrategy csrfStrategy =
                new CsrfAuthenticationStrategy(csrfTokenRepository);

        csrfStrategy.onAuthentication(
                authentication,
                httpRequest,
                httpResponse);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        securityContextRepository.saveContext(
                securityContext,
                httpRequest,
                httpResponse);

        AppUser appUser = authService.getUserByEmail(authentication.getName());

        UserResponse response = new UserResponse(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();

        AppUser user = authService.getUserByEmail(email);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

        logoutHandler.logout(
                httpRequest,
                httpResponse,
                authentication);

        CookieClearingLogoutHandler cookieHandler =
                new CookieClearingLogoutHandler("JSESSIONID");

        cookieHandler.logout(
                httpRequest,
                httpResponse,
                authentication);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
            ) {
        authService.deleteAccount(request.getPassword());

        new SecurityContextLogoutHandler()
                .logout(httpRequest,
                        httpResponse,
                        SecurityContextHolder.getContext().getAuthentication());

        new CookieClearingLogoutHandler("JSESSIONID")
                .logout(httpRequest, httpResponse, null);

        return ResponseEntity.noContent().build();
    }
}

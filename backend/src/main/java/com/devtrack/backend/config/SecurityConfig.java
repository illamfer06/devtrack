package com.devtrack.backend.config;

import com.devtrack.backend.dto.ErrorResponse;
import com.devtrack.backend.repository.AppUserRepository;
import com.devtrack.backend.security.ActiveAccountFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.access.intercept.AuthorizationFilter;


import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            CsrfTokenRepository csrfTokenRepository,
            ObjectMapper objectMapper,
            AppUserRepository appUserRepository) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/auth/register",
                        "/auth/login",
                        "/auth/csrf",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
        );

        return http.securityContext(
                context -> context
                        .securityContextRepository(securityContextRepository))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                (request, response, authException) ->  {
                            writeErrorResponse(
                                    request,
                                    response,
                                    HttpStatus.UNAUTHORIZED,
                                    "Authentication required",
                                    objectMapper
                            );})
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {
                                writeErrorResponse(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Access denied",
                                        objectMapper);}))
                .logout(logout -> logout.disable())
                .addFilterBefore(
                        new ActiveAccountFilter(appUserRepository, objectMapper),
                        AuthorizationFilter.class
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {

        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {

        return new HttpSessionCsrfTokenRepository();
    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message,
            ObjectMapper objectMapper) throws IOException {

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}

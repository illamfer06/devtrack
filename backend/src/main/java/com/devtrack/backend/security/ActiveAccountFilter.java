package com.devtrack.backend.security;

import com.devtrack.backend.dto.ErrorResponse;
import com.devtrack.backend.repository.AppUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ActiveAccountFilter extends OncePerRequestFilter {

    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;

    public ActiveAccountFilter(AppUserRepository appUserRepository,
                               ObjectMapper objectMapper) {
        this.appUserRepository = appUserRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {
            boolean validAccount =
                    authentication.getPrincipal() instanceof DevTrackUserDetails userDetails
                            && userDetails.getId() != null
                            && appUserRepository.existsById(userDetails.getId());
            if (!validAccount) {
                new SecurityContextLogoutHandler()
                        .logout(request, response, authentication);

                writeErrorResponse(
                        request,
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "Authentication required"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message) throws IOException {

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        this.objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}

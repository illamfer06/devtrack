package com.devtrack.backend.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Supplies the principal type required by the real ActiveAccountFilter. */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithDevTrackUser.Factory.class)
public @interface WithDevTrackUser {
    long id() default 101L;
    String email() default "alice@example.com";

    class Factory implements WithSecurityContextFactory<WithDevTrackUser> {
        @Override
        public SecurityContext createSecurityContext(WithDevTrackUser annotation) {
            var principal = new DevTrackUserDetails(annotation.id(), annotation.email(), "stored-password-hash");
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    principal, null, principal.getAuthorities()));
            return context;
        }
    }
}

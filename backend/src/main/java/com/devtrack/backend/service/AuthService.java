package com.devtrack.backend.service;

import com.devtrack.backend.dto.RegisterRequest;
import com.devtrack.backend.exception.EmailAlreadyExistsException;
import com.devtrack.backend.exception.InvalidCredentialsException;
import com.devtrack.backend.model.AppUser;
import com.devtrack.backend.repository.AppUserRepository;
import com.devtrack.backend.repository.ProblemRepository;
import com.devtrack.backend.repository.StudyBlockRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProblemRepository problemRepository;
    private final StudyBlockRepository studyBlockRepository;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            ProblemRepository problemRepository,
            StudyBlockRepository studyBlockRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.problemRepository = problemRepository;
        this.studyBlockRepository = studyBlockRepository;
    }

    public AppUser register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (appUserRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is already registered");}

        String passwordHash = passwordEncoder.encode(request.getPassword());

        AppUser user = new AppUser(
                email,
                passwordHash
        );

        return appUserRepository.save(user);
    }

    public AppUser getUserByEmail(String email) {

        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public void deleteAccount(String rawPassword) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        AppUser user = getUserByEmail(email);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        Long userId = user.getId();
        problemRepository.deleteAllByUserId(userId);
        studyBlockRepository.deleteAllByUserId(userId);
        appUserRepository.deleteById(userId);

    }
}

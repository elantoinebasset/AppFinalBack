package com.timescheduler.service;

import com.timescheduler.dto.*;
import com.timescheduler.entity.User;
import com.timescheduler.repository.UserRepository;
import com.timescheduler.security.GoogleTokenVerifier;
import com.timescheduler.security.JwtService;
import com.timescheduler.security.PasswordService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    JwtService jwtService;

    @Inject
    GoogleTokenVerifier googleTokenVerifier;

    @Inject
    EmailService emailService;

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordService.hashPassword(request.getPassword()))
                .isActive(true)
                .emailVerified(false)
                .build();

        userRepository.persist(user);

        sendVerificationEmail(user);

        // Pas de token retourne : l'utilisateur doit d'abord verifier son email avant de se connecter
        return AuthResponse.builder()
                .user(mapToDTO(user))
                .build();
    }

    private void sendVerificationEmail(User user) {
        try {
            String token = jwtService.generateEmailVerificationToken(user.getEmail());
            emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), token);
        } catch (Exception e) {
            // On ne bloque pas l'inscription si l'envoi echoue
            Log.warnf("Echec de l'envoi de l'email de verification a %s : %s", user.getEmail(), e.getMessage());
        }
    }

    public AuthResponse login(AuthLoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Username and password are required");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("User account is inactive");
        }

        if (!passwordService.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException(
                    "Adresse email non vérifiée. Vérifiez votre boîte mail pour activer votre compte.");
        }

        String token = jwtService.generateToken(user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .user(mapToDTO(user))
                .build();
    }

    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        if (request == null || isBlank(request.getCredential())) {
            throw new IllegalArgumentException("Missing Google credential");
        }

        GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(request.getCredential());

        User user = userRepository.findByEmail(googleUser.email())
                .orElseGet(() -> createUserFromGoogle(googleUser));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("User account is inactive");
        }

        String token = jwtService.generateToken(user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .user(mapToDTO(user))
                .build();
    }

    private User createUserFromGoogle(GoogleTokenVerifier.GoogleUserInfo googleUser) {
        String firstName = isBlank(googleUser.firstName()) ? "Google" : googleUser.firstName();
        String lastName = isBlank(googleUser.lastName()) ? "User" : googleUser.lastName();

        User user = User.builder()
                .username(generateUniqueUsername(googleUser.email()))
                .email(googleUser.email())
                .firstName(firstName)
                .lastName(lastName)
                .password(passwordService.hashPassword(UUID.randomUUID().toString()))
                .isActive(true)
                .emailVerified(true)
                .build();

        userRepository.persist(user);
        return user;
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "");
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    public UserDTO getCurrentUser(String token) {
        if (isBlank(token)) {
            throw new IllegalArgumentException("Missing bearer token");
        }

        String username = jwtService.validateAndExtractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return mapToDTO(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        if (isBlank(token)) {
            throw new IllegalArgumentException("Missing verification token");
        }

        String email = jwtService.validateEmailVerificationToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEmailVerified(true);
    }

    private void validateRegisterRequest(AuthRegisterRequest request) {
        if (request == null
                || isBlank(request.getUsername())
                || isBlank(request.getEmail())
                || isBlank(request.getFirstName())
                || isBlank(request.getLastName())
                || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("All fields are required for registration");
        }

        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

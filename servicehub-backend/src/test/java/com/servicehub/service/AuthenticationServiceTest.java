package com.servicehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.servicehub.dto.LoginRequest;
import com.servicehub.dto.LoginResponse;
import com.servicehub.entity.Role;
import com.servicehub.entity.User;
import com.servicehub.exception.InvalidCredentialsException;
import com.servicehub.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userRepository, passwordEncoder);
    }

    @Test
    void acceptsCorrectCredentialsAndNormalizesEmail() {
        LoginRequest request = request("  ALICE@EXAMPLE.COM ", "correct-password");
        User user = new User("Alice", "alice@example.com", "stored-hash", Role.CLIENT);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "stored-hash")).thenReturn(true);

        LoginResponse response = authenticationService.verifyCredentials(request);

        assertEquals("Alice", response.getName());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals(Role.CLIENT, response.getRole());
        verify(passwordEncoder).matches("correct-password", "stored-hash");
    }

    @Test
    void rejectsUnknownEmail() {
        LoginRequest request = request("missing@example.com", "any-password");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.verifyCredentials(request));
    }

    @Test
    void rejectsWrongPassword() {
        LoginRequest request = request("alice@example.com", "wrong-password");
        User user = new User("Alice", "alice@example.com", "stored-hash", Role.CLIENT);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.verifyCredentials(request));
    }

    private LoginRequest request(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}

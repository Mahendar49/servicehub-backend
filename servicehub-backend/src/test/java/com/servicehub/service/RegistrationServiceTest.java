package com.servicehub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.servicehub.dto.RegisterRequest;
import com.servicehub.entity.Role;
import com.servicehub.entity.User;
import com.servicehub.exception.DuplicateEmailException;
import com.servicehub.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    private RegistrationService registrationService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        registrationService = new RegistrationService(userRepository, passwordEncoder);
    }

    @Test
    void registerClientNormalizesEmailHashesPasswordAndAssignsClientRole() {
        RegisterRequest request = new RegisterRequest();
        request.setName("  Alice  ");
        request.setEmail("  ALICE@Example.COM  ");
        request.setPassword("StrongPass123");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        registrationService.registerClient(request);

        verify(userRepository).existsByEmail("alice@example.com");
        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());

        User user = savedUser.getValue();
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals(Role.CLIENT, user.getRole());
        assertFalse("StrongPass123".equals(user.getPassword()));
        assertTrue(new BCryptPasswordEncoder().matches("StrongPass123", user.getPassword()));
    }

    @Test
    void registerClientRejectsDuplicateEmailWithoutSaving() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("ALICE@example.com");
        request.setPassword("StrongPass123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
                () -> registrationService.registerClient(request));

        assertEquals("Email is already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}

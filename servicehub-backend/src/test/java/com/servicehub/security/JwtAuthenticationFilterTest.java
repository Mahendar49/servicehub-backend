package com.servicehub.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.servicehub.entity.Role;
import com.servicehub.entity.User;
import com.servicehub.repository.UserRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain chain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenLoadsRoleFromDatabase() throws Exception {
        when(jwtService.validateAndGetUserId("valid-token")).thenReturn(1L);
        User user = new User("Alice", "alice@example.com", "hash", Role.CLIENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        new JwtAuthenticationFilter(jwtService, userRepository)
                .doFilter(request, new MockHttpServletResponse(), chain);

        assertEquals("1", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals("ROLE_CLIENT", SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void invalidTokenDoesNotAuthenticate() throws Exception {
        when(jwtService.validateAndGetUserId("invalid-token"))
                .thenThrow(new JwtException("Invalid signature"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        new JwtAuthenticationFilter(jwtService, userRepository)
                .doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void missingTokenDoesNotAuthenticate() throws Exception {
        new JwtAuthenticationFilter(jwtService, userRepository)
                .doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

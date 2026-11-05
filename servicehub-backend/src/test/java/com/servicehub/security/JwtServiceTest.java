package com.servicehub.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.servicehub.entity.Role;
import com.servicehub.entity.User;

import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-bytes-long";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-04T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void generatedTokenCanBeValidated() {
        JwtService service = new JwtService(SECRET, CLOCK);
        User user = new User("Alice", "alice@example.com", "hash", Role.CLIENT);
        ReflectionTestUtils.setField(user, "id", 42L);

        String token = service.generateAccessToken(user);

        assertEquals(42L, service.validateAndGetUserId(token));
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = new JwtService(SECRET, CLOCK);
        User user = new User("Alice", "alice@example.com", "hash", Role.CLIENT);
        ReflectionTestUtils.setField(user, "id", 42L);
        String token = service.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertThrows(JwtException.class, () -> service.validateAndGetUserId(tampered));
    }

    @Test
    void rejectsWeakSecret() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", CLOCK));
    }
}

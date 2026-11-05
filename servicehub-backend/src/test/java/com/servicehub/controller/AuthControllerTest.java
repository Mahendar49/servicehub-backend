package com.servicehub.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.servicehub.dto.RegisterRequest;
import com.servicehub.exception.DuplicateEmailException;
import com.servicehub.exception.GlobalExceptionHandler;
import com.servicehub.service.RegistrationService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RegistrationService registrationService;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(registrationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void registerReturnsCreatedForValidClient() throws Exception {
        when(registrationService.registerClient(any(RegisterRequest.class))).thenReturn(42L);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.message").value("Client registered successfully"));

        verify(registrationService).registerClient(any(RegisterRequest.class));
    }

    @Test
    void registerReturnsConflictForDuplicateEmail() throws Exception {
        when(registrationService.registerClient(any(RegisterRequest.class)))
                .thenThrow(new DuplicateEmailException());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email is already registered"));
    }

    @Test
    void registerRejectsInvalidEmail() throws Exception {
        assertBadRequest("""
                {"name":"Alice","email":"not-an-email","password":"StrongPass123"}
                """);
    }

    @Test
    void registerRejectsShortPassword() throws Exception {
        assertBadRequest("""
                {"name":"Alice","email":"alice@example.com","password":"123"}
                """);
    }

    @Test
    void registerRejectsBlankName() throws Exception {
        assertBadRequest("""
                {"name":"   ","email":"alice@example.com","password":"StrongPass123"}
                """);
    }

    private void assertBadRequest(String json) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid registration details"));

        verifyNoInteractions(registrationService);
    }

    private String validRequest() {
        return """
                {"name":"Alice","email":"alice@example.com","password":"StrongPass123"}
                """;
    }
}

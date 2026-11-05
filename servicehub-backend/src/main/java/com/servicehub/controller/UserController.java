package com.servicehub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicehub.entity.User;
import com.servicehub.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        Long id = Long.valueOf(authentication.getName());
        User user = userRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(new UserProfileResponse(user.getId(), user.getName(),
                user.getEmail(), user.getRole().name()));
    }

    public record UserProfileResponse(Long id, String name, String email, String role) {
    }
}

package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.repository.UserRepository;
import com.nourishnet.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.requireRole(authHeader, UserRole.ADMIN);
        List<User> users = (role != null)
                ? userRepository.findByRole(role)
                : userRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved", users));
    }
}

package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.dto.LoginRequest;
import com.nourishnet.dto.LoginResponse;
import com.nourishnet.dto.RegisterRequest;
import com.nourishnet.model.User;
import com.nourishnet.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireUser(authHeader);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/demo-accounts")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getDemoAccounts() {
        List<Map<String, String>> demoAccounts = List.of(
                Map.of("role", "ADMIN", "email", "admin@nourishnet.org", "password", "admin123", "label", "System Administrator"),
                Map.of("role", "DONOR", "email", "greenbites@restaurant.com", "password", "donor123", "label", "Green Bites Bistro & Cafe"),
                Map.of("role", "DONOR", "email", "grandhotel@catering.com", "password", "donor123", "label", "The Grand Horizon Hotel"),
                Map.of("role", "DONOR", "email", "campusdining@univ.edu", "password", "donor123", "label", "Metro University Dining Hall"),
                Map.of("role", "RECIPIENT", "email", "hope.shelter@ngo.org", "password", "recip123", "label", "Hope Haven Shelter for Homeless"),
                Map.of("role", "RECIPIENT", "email", "shanti.ashram@charity.org", "password", "recip123", "label", "Shanti Senior Citizen Ashram"),
                Map.of("role", "VOLUNTEER", "email", "alex.driver@volunteer.org", "password", "volun123", "label", "Alex Rivera (Cargo Van 150kg)"),
                Map.of("role", "VOLUNTEER", "email", "priya.driver@volunteer.org", "password", "volun123", "label", "Priya Sharma (Electric Auto 75kg)")
        );
        return ResponseEntity.ok(ApiResponse.ok("Demo accounts retrieved", demoAccounts));
    }
}

package com.nourishnet.service;

import com.nourishnet.dto.LoginRequest;
import com.nourishnet.dto.LoginResponse;
import com.nourishnet.dto.RegisterRequest;
import com.nourishnet.exception.UnauthorizedException;
import com.nourishnet.exception.ValidationException;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.model.Vehicle;
import com.nourishnet.model.VehicleType;
import com.nourishnet.model.VolunteerDriver;
import com.nourishnet.repository.UserRepository;
import com.nourishnet.repository.VolunteerDriverRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication service with BCrypt password hashing and token management.
 * Designed to seamlessly migrate to Supabase Auth / PostgreSQL in future phases.
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final VolunteerDriverRepository volunteerDriverRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Temporary token-to-user session store
    private final Map<String, User> activeTokens = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, 
                       VolunteerDriverRepository volunteerDriverRepository) {
        this.userRepository = userRepository;
        this.volunteerDriverRepository = volunteerDriverRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new ValidationException("Email and password cannot be null");
        }

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password credentials");
        }

        String token = "demo_token_" + UUID.randomUUID();
        activeTokens.put(token, user);

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getOrganization(),
                user.getPhone(),
                user.getAddress()
        );
    }

    public LoginResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("An account with email " + email + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                UUID.randomUUID().toString(),
                request.getName().trim(),
                email,
                hashedPassword,
                request.getRole(),
                request.getPhone(),
                request.getOrganization(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );

        user = userRepository.save(user);

        // If registered as a volunteer, initialize a default driver profile
        if (user.getRole() == UserRole.VOLUNTEER) {
            Vehicle defaultVehicle = new Vehicle(
                    VehicleType.CAR_SEDAN,
                    "DEMO-REG-" + (100 + (int)(Math.random() * 900)),
                    50.0,
                    "Registered volunteer vehicle"
            );
            VolunteerDriver driver = new VolunteerDriver(
                    UUID.randomUUID().toString(),
                    user.getId(),
                    user.getName(),
                    user.getPhone(),
                    defaultVehicle,
                    true,
                    user.getAddress() != null ? user.getAddress() : "City Center",
                    user.getLatitude() != null ? user.getLatitude() : 13.0827,
                    user.getLongitude() != null ? user.getLongitude() : 80.2707,
                    15.0,
                    "North, Central, South Districts"
            );
            volunteerDriverRepository.save(driver);
        }

        String token = "demo_token_" + UUID.randomUUID();
        activeTokens.put(token, user);

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getOrganization(),
                user.getPhone(),
                user.getAddress()
        );
    }

    public Optional<User> authenticateToken(String tokenHeader) {
        if (tokenHeader == null || tokenHeader.trim().isEmpty()) {
            return Optional.empty();
        }
        String token = tokenHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        User user = activeTokens.get(token);
        if (user != null) {
            return userRepository.findById(user.getId());
        }
        return Optional.empty();
    }

    public User requireUser(String authHeader) {
        return authenticateToken(authHeader)
                .orElseThrow(() -> new UnauthorizedException("Valid authentication token required"));
    }

    public User requireRole(String authHeader, UserRole requiredRole) {
        User user = requireUser(authHeader);
        if (user.getRole() != requiredRole && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Access denied. Required role: " + requiredRole + ", current role: " + user.getRole());
        }
        return user;
    }
}

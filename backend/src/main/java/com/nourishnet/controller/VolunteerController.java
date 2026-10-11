package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.dto.VolunteerRegisterRequest;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.model.VolunteerDriver;
import com.nourishnet.service.AuthService;
import com.nourishnet.service.DeliveryService;
import com.nourishnet.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {
    private final VolunteerService volunteerService;
    private final DeliveryService deliveryService;
    private final AuthService authService;

    public VolunteerController(VolunteerService volunteerService,
                               DeliveryService deliveryService,
                               AuthService authService) {
        this.volunteerService = volunteerService;
        this.deliveryService = deliveryService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VolunteerDriver>> registerVehicle(
            @Valid @RequestBody VolunteerRegisterRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireRole(authHeader, UserRole.VOLUNTEER);
        VolunteerDriver driver = volunteerService.registerOrUpdateVehicle(request, user);
        return ResponseEntity.ok(ApiResponse.ok("Volunteer vehicle profile saved", driver));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VolunteerDriver>>> getAllVolunteers() {
        List<VolunteerDriver> drivers = volunteerService.getAllDrivers();
        return ResponseEntity.ok(ApiResponse.ok("Volunteers retrieved", drivers));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<VolunteerDriver>> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireRole(authHeader, UserRole.VOLUNTEER);
        VolunteerDriver driver = volunteerService.getDriverByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Volunteer profile retrieved", driver));
    }

    @GetMapping("/eligible")
    public ResponseEntity<ApiResponse<List<VolunteerDriver>>> getEligibleVolunteers(
            @RequestParam(required = false) String deliveryId) {
        List<VolunteerDriver> drivers = deliveryId != null
                ? deliveryService.getEligibleVolunteersForDelivery(deliveryId)
                : volunteerService.getAvailableDrivers();
        return ResponseEntity.ok(ApiResponse.ok("Eligible volunteer drivers", drivers));
    }

    @PatchMapping("/availability")
    public ResponseEntity<ApiResponse<VolunteerDriver>> toggleAvailability(
            @RequestBody Map<String, Boolean> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireRole(authHeader, UserRole.VOLUNTEER);
        boolean available = body.getOrDefault("available", true);
        VolunteerDriver driver = volunteerService.setAvailability(available, user);
        return ResponseEntity.ok(ApiResponse.ok("Availability status updated", driver));
    }
}

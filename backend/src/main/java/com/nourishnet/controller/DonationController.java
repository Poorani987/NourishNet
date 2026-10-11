package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.dto.DonationCreateRequest;
import com.nourishnet.model.FoodBatch;
import com.nourishnet.model.FoodCategory;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.service.AuthService;
import com.nourishnet.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    private final DonationService donationService;
    private final AuthService authService;

    public DonationController(DonationService donationService, AuthService authService) {
        this.donationService = donationService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FoodBatch>> createDonation(
            @Valid @RequestBody DonationCreateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User donor = authService.requireRole(authHeader, UserRole.DONOR);
        FoodBatch created = donationService.createDonation(request, donor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Food donation registered successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FoodBatch>>> getDonations(
            @RequestParam(required = false) FoodCategory category,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {
        List<FoodBatch> results = availableOnly
                ? donationService.getAvailableDonations(category, vegetarian, search)
                : donationService.getAllDonations();
        return ResponseEntity.ok(ApiResponse.ok("Donations retrieved", results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodBatch>> getDonationById(@PathVariable String id) {
        FoodBatch batch = donationService.getDonationById(id);
        return ResponseEntity.ok(ApiResponse.ok(batch));
    }

    @GetMapping("/donor/{donorId}")
    public ResponseEntity<ApiResponse<List<FoodBatch>>> getDonationsByDonor(@PathVariable String donorId) {
        List<FoodBatch> batches = donationService.getDonationsByDonor(donorId);
        return ResponseEntity.ok(ApiResponse.ok(batches));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<FoodBatch>> cancelDonation(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireUser(authHeader);
        FoodBatch cancelled = donationService.cancelDonation(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Donation successfully cancelled", cancelled));
    }
}

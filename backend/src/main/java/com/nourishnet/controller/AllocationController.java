package com.nourishnet.controller;

import com.nourishnet.dto.AllocationRequest;
import com.nourishnet.dto.ApiResponse;
import com.nourishnet.model.Allocation;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.service.AllocationService;
import com.nourishnet.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
public class AllocationController {
    private final AllocationService allocationService;
    private final AuthService authService;

    public AllocationController(AllocationService allocationService, AuthService authService) {
        this.allocationService = allocationService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Allocation>> claimDonation(
            @Valid @RequestBody AllocationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User recipient = authService.requireRole(authHeader, UserRole.RECIPIENT);
        Allocation allocation = allocationService.claimDonation(request, recipient);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Donation allocation confirmed successfully", allocation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Allocation>>> getAllocations(
            @RequestParam(required = false) String recipientId,
            @RequestParam(required = false) String donorId) {
        List<Allocation> list;
        if (recipientId != null) {
            list = allocationService.getAllocationsByRecipient(recipientId);
        } else if (donorId != null) {
            list = allocationService.getAllocationsByDonor(donorId);
        } else {
            list = allocationService.getAllAllocations();
        }
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Allocation>> getAllocationById(@PathVariable String id) {
        Allocation allocation = allocationService.getAllocationById(id);
        return ResponseEntity.ok(ApiResponse.ok(allocation));
    }
}

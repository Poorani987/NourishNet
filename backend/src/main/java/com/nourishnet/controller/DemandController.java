package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.dto.DemandCreateRequest;
import com.nourishnet.model.DemandRequest;
import com.nourishnet.model.DemandStatus;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.service.AuthService;
import com.nourishnet.service.DemandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demands")
public class DemandController {
    private final DemandService demandService;
    private final AuthService authService;

    public DemandController(DemandService demandService, AuthService authService) {
        this.demandService = demandService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DemandRequest>> createDemand(
            @Valid @RequestBody DemandCreateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User recipient = authService.requireRole(authHeader, UserRole.RECIPIENT);
        DemandRequest created = demandService.createDemand(request, recipient);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Demand request published successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DemandRequest>>> getAllDemands(
            @RequestParam(required = false) DemandStatus status) {
        List<DemandRequest> demands = demandService.getAllDemands(status);
        return ResponseEntity.ok(ApiResponse.ok("Demands retrieved", demands));
    }

    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<ApiResponse<List<DemandRequest>>> getDemandsByRecipient(@PathVariable String recipientId) {
        List<DemandRequest> demands = demandService.getDemandsByRecipient(recipientId);
        return ResponseEntity.ok(ApiResponse.ok(demands));
    }

    @PatchMapping("/{id}/fulfill")
    public ResponseEntity<ApiResponse<DemandRequest>> fulfillDemand(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireUser(authHeader);
        DemandRequest fulfilled = demandService.fulfillDemand(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Demand request fulfilled", fulfilled));
    }
}

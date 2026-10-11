package com.nourishnet.controller;

import com.nourishnet.dto.ApiResponse;
import com.nourishnet.dto.DeliveryStatusUpdateRequest;
import com.nourishnet.dto.OtpVerifyRequest;
import com.nourishnet.dto.RoutingResultDto;
import com.nourishnet.model.Delivery;
import com.nourishnet.model.User;
import com.nourishnet.model.UserRole;
import com.nourishnet.service.AuthService;
import com.nourishnet.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final AuthService authService;

    public DeliveryController(DeliveryService deliveryService, AuthService authService) {
        this.deliveryService = deliveryService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Delivery>>> getAllDeliveries(
            @RequestParam(required = false) String volunteerId,
            @RequestParam(required = false) String recipientId,
            @RequestParam(required = false, defaultValue = "false") boolean pendingOnly) {
        List<Delivery> list;
        if (pendingOnly) {
            list = deliveryService.getPendingDeliveries();
        } else if (volunteerId != null) {
            list = deliveryService.getDeliveriesByVolunteer(volunteerId);
        } else if (recipientId != null) {
            list = deliveryService.getDeliveriesByRecipient(recipientId);
        } else {
            list = deliveryService.getAllDeliveries();
        }
        return ResponseEntity.ok(ApiResponse.ok("Deliveries retrieved", list));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<Delivery>>> getPendingDeliveries() {
        List<Delivery> pending = deliveryService.getPendingDeliveries();
        return ResponseEntity.ok(ApiResponse.ok("Pending deliveries", pending));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Delivery>> getDeliveryById(@PathVariable String id) {
        Delivery delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(ApiResponse.ok(delivery));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<Delivery>> acceptDelivery(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User volunteer = authService.requireRole(authHeader, UserRole.VOLUNTEER);
        Delivery accepted = deliveryService.acceptDelivery(id, volunteer);
        return ResponseEntity.ok(ApiResponse.ok("Delivery accepted and route calculated", accepted));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Delivery>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody DeliveryStatusUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireUser(authHeader);
        Delivery updated = deliveryService.updateStatus(id, request.getStatus(), user);
        return ResponseEntity.ok(ApiResponse.ok("Delivery status updated to " + request.getStatus(), updated));
    }

    @GetMapping("/{id}/route")
    public ResponseEntity<ApiResponse<RoutingResultDto>> getDeliveryRoute(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireUser(authHeader);
        RoutingResultDto route = deliveryService.getDeliveryRoute(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Shortest delivery route calculated with Dijkstra's algorithm", route));
    }

    @PostMapping("/{id}/verify-otp")
    public ResponseEntity<ApiResponse<Delivery>> verifyHandoverOtp(
            @PathVariable String id,
            @Valid @RequestBody OtpVerifyRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authService.requireUser(authHeader);
        Delivery completed = deliveryService.verifyHandoverOtp(id, request, user);
        return ResponseEntity.ok(ApiResponse.ok("Handover verified successfully! Food delivery completed.", completed));
    }
}

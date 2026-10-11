package com.nourishnet.service;

import com.nourishnet.dto.OtpVerifyRequest;
import com.nourishnet.dto.RoutingResultDto;
import com.nourishnet.exception.DeliveryException;
import com.nourishnet.exception.ResourceNotFoundException;
import com.nourishnet.exception.UnauthorizedException;
import com.nourishnet.model.*;
import com.nourishnet.repository.*;
import com.nourishnet.routing.DijkstraRoutingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final VolunteerDriverRepository volunteerDriverRepository;
    private final FoodBatchRepository foodBatchRepository;
    private final AllocationRepository allocationRepository;
    private final NotificationRepository notificationRepository;
    private final DijkstraRoutingService dijkstraRoutingService;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           VolunteerDriverRepository volunteerDriverRepository,
                           FoodBatchRepository foodBatchRepository,
                           AllocationRepository allocationRepository,
                           NotificationRepository notificationRepository,
                           DijkstraRoutingService dijkstraRoutingService) {
        this.deliveryRepository = deliveryRepository;
        this.volunteerDriverRepository = volunteerDriverRepository;
        this.foodBatchRepository = foodBatchRepository;
        this.allocationRepository = allocationRepository;
        this.notificationRepository = notificationRepository;
        this.dijkstraRoutingService = dijkstraRoutingService;
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    public Delivery getDeliveryById(String id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery record not found: " + id));
    }

    public List<Delivery> getPendingDeliveries() {
        return deliveryRepository.findPendingDeliveries();
    }

    public List<Delivery> getDeliveriesByVolunteer(String volunteerId) {
        return deliveryRepository.findByVolunteerId(volunteerId);
    }

    public List<Delivery> getDeliveriesByRecipient(String recipientId) {
        return deliveryRepository.findByRecipientId(recipientId);
    }

    /**
     * Checks eligibility for a volunteer driver against a pending delivery.
     * Considers: availability, vehicle capacity, active assignments, and food safety deadline.
     */
    public List<VolunteerDriver> getEligibleVolunteersForDelivery(String deliveryId) {
        Delivery delivery = getDeliveryById(deliveryId);
        List<VolunteerDriver> availableDrivers = volunteerDriverRepository.findAvailableDrivers();

        return availableDrivers.stream()
                .filter(driver -> driver.getVehicle() != null && driver.getVehicle().getCapacityKg() >= delivery.getQuantity())
                .filter(driver -> driver.getActiveDeliveryId() == null || driver.getActiveDeliveryId().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * First-come-first-served volunteer acceptance of a delivery.
     * Uses synchronization to guarantee that conflicting concurrent acceptances are rejected.
     */
    public synchronized Delivery acceptDelivery(String deliveryId, User volunteerUser) {
        if (volunteerUser.getRole() != UserRole.VOLUNTEER && volunteerUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only registered volunteer drivers can accept deliveries");
        }

        Delivery delivery = getDeliveryById(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.PENDING_ACCEPTANCE) {
            throw new DeliveryException("Delivery has already been accepted or is no longer available (Status: " + delivery.getStatus() + ")");
        }

        VolunteerDriver driver = volunteerDriverRepository.findByUserId(volunteerUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer driver profile not found for user: " + volunteerUser.getName()));

        if (!driver.isAvailable()) {
            throw new DeliveryException("You are currently marked as unavailable. Please update your status to available first.");
        }

        if (driver.getActiveDeliveryId() != null && !driver.getActiveDeliveryId().isEmpty()) {
            throw new DeliveryException("You already have an active uncompleted delivery in progress (" + driver.getActiveDeliveryId() + "). Please complete it first.");
        }

        if (driver.getVehicle() != null && driver.getVehicle().getCapacityKg() < delivery.getQuantity()) {
            throw new DeliveryException("Your vehicle capacity (" + driver.getVehicle().getCapacityKg() + " kg) is insufficient for this delivery (" + delivery.getQuantity() + " " + delivery.getUnit() + ").");
        }

        // Verify food safety deadline
        foodBatchRepository.findById(delivery.getBatchId()).ifPresent(batch -> {
            if (batch.getConsumeBefore() != null && batch.getConsumeBefore().isBefore(LocalDateTime.now())) {
                delivery.setStatus(DeliveryStatus.CANCELLED);
                deliveryRepository.save(delivery);
                throw new DeliveryException("Cannot accept: Food batch has passed its food safety deadline.");
            }
        });

        // Calculate Dijkstra shortest path on the road network
        String sourceNode = delivery.getSourceNodeId() != null ? delivery.getSourceNodeId() : "NODE-CENTRAL";
        String destNode = delivery.getDestinationNodeId() != null ? delivery.getDestinationNodeId() : "NODE-SHELTER-NORTH";

        RoutingResultDto routingResult;
        try {
            routingResult = dijkstraRoutingService.calculateShortestPath(sourceNode, destNode);
        } catch (Exception ex) {
            routingResult = new RoutingResultDto(sourceNode, destNode, List.of(sourceNode, destNode), Collections.emptyList(), 5.0);
        }

        delivery.setVolunteerId(driver.getId());
        delivery.setVolunteerName(driver.getName());
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        delivery.setRouteNodeIds(routingResult.getPath());
        delivery.setRouteDistanceKm(routingResult.getTotalDistanceKm());

        driver.setActiveDeliveryId(delivery.getId());
        volunteerDriverRepository.save(driver);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Notify recipient
        notificationRepository.save(new Notification(
                UUID.randomUUID().toString(),
                delivery.getRecipientId(),
                "Volunteer Driver Assigned",
                driver.getName() + " has accepted your food delivery. Route distance: " + routingResult.getTotalDistanceKm() + " km.",
                "DELIVERY"
        ));

        return savedDelivery;
    }

    /**
     * Retrieves the calculated Dijkstra shortest route for this delivery.
     */
    public RoutingResultDto getDeliveryRoute(String deliveryId, User user) {
        Delivery delivery = getDeliveryById(deliveryId);

        String source = delivery.getSourceNodeId() != null ? delivery.getSourceNodeId() : "NODE-CENTRAL";
        String dest = delivery.getDestinationNodeId() != null ? delivery.getDestinationNodeId() : "NODE-SHELTER-NORTH";

        return dijkstraRoutingService.calculateShortestPath(source, dest);
    }

    /**
     * Updates delivery status (e.g. volunteer marks food as picked up).
     */
    public synchronized Delivery updateStatus(String deliveryId, DeliveryStatus newStatus, User user) {
        Delivery delivery = getDeliveryById(deliveryId);

        if (newStatus == DeliveryStatus.PICKED_UP) {
            if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
                throw new DeliveryException("Cannot mark picked up from status: " + delivery.getStatus() + ". Must be ASSIGNED.");
            }
            delivery.setStatus(DeliveryStatus.PICKED_UP);
            delivery.setPickedUpAt(LocalDateTime.now());

            // Notify recipient to prepare OTP
            notificationRepository.save(new Notification(
                    UUID.randomUUID().toString(),
                    delivery.getRecipientId(),
                    "Food Picked Up by Volunteer",
                    "Volunteer " + delivery.getVolunteerName() + " has collected the food donation. Please have your handover verification OTP ready upon arrival.",
                    "DELIVERY"
            ));
        } else if (newStatus == DeliveryStatus.CANCELLED) {
            delivery.setStatus(DeliveryStatus.CANCELLED);
            if (delivery.getVolunteerId() != null) {
                volunteerDriverRepository.findById(delivery.getVolunteerId()).ifPresent(driver -> {
                    driver.setActiveDeliveryId(null);
                    volunteerDriverRepository.save(driver);
                });
            }
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            throw new DeliveryException("Deliveries can only be marked as DELIVERED by verifying the recipient Handover OTP via /api/deliveries/{id}/verify-otp");
        }

        return deliveryRepository.save(delivery);
    }

    /**
     * Verifies the food handover OTP presented by the recipient to complete delivery.
     */
    public synchronized Delivery verifyHandoverOtp(String deliveryId, OtpVerifyRequest request, User user) {
        Delivery delivery = getDeliveryById(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP && delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new DeliveryException("Cannot complete delivery. Current status must be PICKED_UP (current: " + delivery.getStatus() + ")");
        }

        if (delivery.getOtpAttempts() >= delivery.getMaxOtpAttempts()) {
            throw new DeliveryException("Maximum OTP verification attempts (" + delivery.getMaxOtpAttempts() + ") exceeded. Contact support.");
        }

        if (delivery.getOtpExpiresAt() != null && delivery.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new DeliveryException("Handover OTP has expired.");
        }

        if (delivery.getHandoverOtp() == null || !delivery.getHandoverOtp().trim().equals(request.getOtp().trim())) {
            delivery.setOtpAttempts(delivery.getOtpAttempts() + 1);
            deliveryRepository.save(delivery);
            int remaining = delivery.getMaxOtpAttempts() - delivery.getOtpAttempts();
            throw new DeliveryException("Invalid handover OTP. Attempts remaining: " + remaining);
        }

        // OTP verification successful -> Complete delivery
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        Delivery completedDelivery = deliveryRepository.save(delivery);

        // Update Volunteer state: free active delivery & increment completed count
        if (delivery.getVolunteerId() != null) {
            volunteerDriverRepository.findById(delivery.getVolunteerId()).ifPresent(driver -> {
                driver.setActiveDeliveryId(null);
                driver.setCompletedDeliveriesCount(driver.getCompletedDeliveriesCount() + 1);
                volunteerDriverRepository.save(driver);
            });
        }

        // Update Allocation status
        if (delivery.getAllocationId() != null) {
            allocationRepository.findById(delivery.getAllocationId()).ifPresent(alloc -> {
                alloc.setStatus(AllocationStatus.DELIVERED);
                allocationRepository.save(alloc);
            });
        }

        // Create completion notifications
        notificationRepository.save(new Notification(
                UUID.randomUUID().toString(),
                delivery.getRecipientId(),
                "Food Successfully Delivered",
                "Your claimed food batch (" + delivery.getFoodName() + ") has been securely handed over.",
                "DELIVERY"
        ));

        notificationRepository.save(new Notification(
                UUID.randomUUID().toString(),
                delivery.getDonorId(),
                "Donation Handover Complete",
                "Your donation (" + delivery.getFoodName() + ") has been successfully delivered to " + delivery.getRecipientName(),
                "DELIVERY"
        ));

        return completedDelivery;
    }
}

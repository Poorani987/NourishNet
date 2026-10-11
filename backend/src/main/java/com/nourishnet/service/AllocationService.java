package com.nourishnet.service;

import com.nourishnet.dto.AllocationRequest;
import com.nourishnet.exception.AllocationException;
import com.nourishnet.exception.ResourceNotFoundException;
import com.nourishnet.exception.UnauthorizedException;
import com.nourishnet.model.*;
import com.nourishnet.repository.*;
import com.nourishnet.routing.DijkstraRoutingService;
import com.nourishnet.util.GeoUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Service managing food batch claims and partial allocations.
 * Uses synchronized critical sections on in-memory operations to ensure
 * race-free concurrent claims prior to future database transaction support.
 */
@Service
public class AllocationService {
    private final FoodBatchRepository foodBatchRepository;
    private final AllocationRepository allocationRepository;
    private final DeliveryRepository deliveryRepository;
    private final NotificationRepository notificationRepository;
    private final DijkstraRoutingService dijkstraRoutingService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AllocationService(FoodBatchRepository foodBatchRepository,
                             AllocationRepository allocationRepository,
                             DeliveryRepository deliveryRepository,
                             NotificationRepository notificationRepository,
                             DijkstraRoutingService dijkstraRoutingService) {
        this.foodBatchRepository = foodBatchRepository;
        this.allocationRepository = allocationRepository;
        this.deliveryRepository = deliveryRepository;
        this.notificationRepository = notificationRepository;
        this.dijkstraRoutingService = dijkstraRoutingService;
    }

    /**
     * Atomically claims available or partially available food batches.
     * Prevents race conditions and double-allocations using backend synchronization.
     */
    public synchronized Allocation claimDonation(AllocationRequest request, User recipient) {
        if (recipient.getRole() != UserRole.RECIPIENT && recipient.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only registered recipients (NGOs, shelters, ashrams) can claim food donations");
        }

        if (request.getQuantity() <= 0) {
            throw new AllocationException("Claimed quantity must be greater than zero");
        }

        FoodBatch batch = foodBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation batch not found: " + request.getBatchId()));

        // Food safety revalidation at moment of allocation
        LocalDateTime now = LocalDateTime.now();
        if (batch.getConsumeBefore() != null && batch.getConsumeBefore().isBefore(now)) {
            batch.setStatus(BatchStatus.EXPIRED);
            foodBatchRepository.save(batch);
            throw new AllocationException("Food safety alert: This food batch has passed its consume-before deadline and cannot be allocated.");
        }

        if (batch.getStatus() != BatchStatus.AVAILABLE && batch.getStatus() != BatchStatus.PARTIALLY_ALLOCATED) {
            throw new AllocationException("This food donation is no longer available (Status: " + batch.getStatus() + ")");
        }

        if (batch.getRemainingQuantity() < request.getQuantity()) {
            throw new AllocationException(
                    "Insufficient remaining food. Requested: " + request.getQuantity() + " " + batch.getUnit() +
                    ", but only " + batch.getRemainingQuantity() + " " + batch.getUnit() + " is currently available."
            );
        }

        // Atomically deduct claimed quantity
        double newRemaining = Math.round((batch.getRemainingQuantity() - request.getQuantity()) * 100.0) / 100.0;
        batch.setRemainingQuantity(newRemaining);
        if (newRemaining == 0.0) {
            batch.setStatus(BatchStatus.FULLY_ALLOCATED);
        } else {
            batch.setStatus(BatchStatus.PARTIALLY_ALLOCATED);
        }
        foodBatchRepository.save(batch);

        // Distance and transport policy calculation
        Double recipLat = request.getRecipientLatitude() != null ? request.getRecipientLatitude() : recipient.getLatitude();
        Double recipLon = request.getRecipientLongitude() != null ? request.getRecipientLongitude() : recipient.getLongitude();
        double haversineDistance = GeoUtils.calculateHaversineDistanceKm(
                batch.getPickupLatitude(), batch.getPickupLongitude(),
                recipLat, recipLon
        );
        String policy = GeoUtils.evaluateTransportPolicy(haversineDistance);

        // Resolve destination road network node
        String destNodeId = request.getDestinationNodeId();
        if (destNodeId == null || destNodeId.trim().isEmpty() || !dijkstraRoutingService.getGraph().hasNode(destNodeId)) {
            destNodeId = findClosestRoadNode(recipLat, recipLon);
        }

        String recipientDisplayName = recipient.getOrganization() != null && !recipient.getOrganization().isEmpty()
                ? recipient.getOrganization() + " (" + recipient.getName() + ")"
                : recipient.getName();

        Allocation allocation = new Allocation(
                UUID.randomUUID().toString(),
                batch.getId(),
                batch.getFoodName(),
                batch.getDonorId(),
                batch.getDonorName(),
                recipient.getId(),
                recipientDisplayName,
                request.getQuantity(),
                batch.getUnit(),
                request.getDeliveryAddress(),
                recipLat,
                recipLon,
                destNodeId,
                haversineDistance,
                policy
        );
        Allocation savedAllocation = allocationRepository.save(allocation);

        // Generate 6-digit Handover OTP for food safety delivery verification
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        // Create pending delivery for volunteer coordination
        Delivery delivery = new Delivery(
                UUID.randomUUID().toString(),
                savedAllocation.getId(),
                batch.getId(),
                batch.getFoodName(),
                savedAllocation.getQuantity(),
                savedAllocation.getUnit(),
                batch.getDonorId(),
                batch.getDonorName(),
                batch.getPickupAddress(),
                recipient.getId(),
                recipientDisplayName,
                savedAllocation.getDeliveryAddress(),
                batch.getNearestNodeId(),
                destNodeId,
                otp
        );
        deliveryRepository.save(delivery);

        // Notify donor and volunteer drivers
        notificationRepository.save(new Notification(
                UUID.randomUUID().toString(),
                batch.getDonorId(),
                "Donation Batch Claimed",
                savedAllocation.getQuantity() + " " + savedAllocation.getUnit() + " of " + batch.getFoodName() + " was claimed by " + recipientDisplayName,
                "ALLOCATION"
        ));

        notificationRepository.save(new Notification(
                UUID.randomUUID().toString(),
                "VOLUNTEER",
                "New Delivery Opportunity Available",
                "New delivery waiting for pickup: " + batch.getFoodName() + " (" + savedAllocation.getQuantity() + " " + savedAllocation.getUnit() + ")",
                "DELIVERY"
        ));

        return savedAllocation;
    }

    public List<Allocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    public Allocation getAllocationById(String id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation record not found: " + id));
    }

    public List<Allocation> getAllocationsByRecipient(String recipientId) {
        return allocationRepository.findByRecipientId(recipientId);
    }

    public List<Allocation> getAllocationsByDonor(String donorId) {
        return allocationRepository.findByDonorId(donorId);
    }

    private String findClosestRoadNode(Double lat, Double lon) {
        var nodes = dijkstraRoutingService.getGraph().getNodes();
        if (nodes.isEmpty()) return "NODE-CENTRAL";
        if (lat == null || lon == null) return nodes.keySet().iterator().next();

        return nodes.values().stream()
                .min(Comparator.comparingDouble(n -> GeoUtils.calculateHaversineDistanceKm(lat, lon, n.getLatitude(), n.getLongitude())))
                .map(RoadNode::getId)
                .orElse("NODE-CENTRAL");
    }
}

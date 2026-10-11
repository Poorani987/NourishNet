package com.nourishnet.service;

import com.nourishnet.dto.DonationCreateRequest;
import com.nourishnet.exception.ResourceNotFoundException;
import com.nourishnet.exception.UnauthorizedException;
import com.nourishnet.exception.ValidationException;
import com.nourishnet.model.*;
import com.nourishnet.repository.FoodBatchRepository;
import com.nourishnet.repository.FoodWasteRepository;
import com.nourishnet.repository.NotificationRepository;
import com.nourishnet.routing.DijkstraRoutingService;
import com.nourishnet.util.GeoUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DonationService {
    private final FoodBatchRepository foodBatchRepository;
    private final FoodWasteRepository foodWasteRepository;
    private final NotificationRepository notificationRepository;
    private final DijkstraRoutingService dijkstraRoutingService;

    public DonationService(FoodBatchRepository foodBatchRepository,
                           FoodWasteRepository foodWasteRepository,
                           NotificationRepository notificationRepository,
                           DijkstraRoutingService dijkstraRoutingService) {
        this.foodBatchRepository = foodBatchRepository;
        this.foodWasteRepository = foodWasteRepository;
        this.notificationRepository = notificationRepository;
        this.dijkstraRoutingService = dijkstraRoutingService;
    }

    public FoodBatch createDonation(DonationCreateRequest request, User donor) {
        if (donor.getRole() != UserRole.DONOR && donor.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only registered food donors can publish donation batches");
        }

        if (request.getQuantity() <= 0) {
            throw new ValidationException("Donation quantity must be greater than zero");
        }

        if (request.getConsumeBefore() == null || request.getConsumeBefore().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Consume-before food safety deadline must be in the future");
        }

        // Determine nearest road network node for Dijkstra routing
        String assignedNodeId = request.getNearestNodeId();
        if (assignedNodeId == null || assignedNodeId.trim().isEmpty() || !dijkstraRoutingService.getGraph().hasNode(assignedNodeId)) {
            assignedNodeId = findClosestRoadNode(request.getPickupLatitude(), request.getPickupLongitude());
        }

        FoodBatch batch = new FoodBatch(
                UUID.randomUUID().toString(),
                donor.getId(),
                donor.getName() + (donor.getOrganization() != null ? " (" + donor.getOrganization() + ")" : ""),
                request.getFoodName().trim(),
                request.getCategory(),
                request.getQuantity(),
                request.getUnit().trim(),
                request.isVegetarian(),
                request.isCookedFood(),
                request.getPreparationTime() != null ? request.getPreparationTime() : LocalDateTime.now().minusHours(1),
                request.getConsumeBefore(),
                request.getAllergenInfo(),
                request.getStorageRequirements(),
                request.getPickupAddress().trim(),
                request.getPickupLatitude() != null ? request.getPickupLatitude() : donor.getLatitude(),
                request.getPickupLongitude() != null ? request.getPickupLongitude() : donor.getLongitude(),
                assignedNodeId,
                request.getAdditionalInstructions()
        );

        FoodBatch saved = foodBatchRepository.save(batch);

        // System notification
        notificationRepository.save(new Notification(
                UUID.randomUUID().toString(),
                "ALL",
                "New Food Donation Available",
                saved.getFoodName() + " (" + saved.getOriginalQuantity() + " " + saved.getUnit() + ") donated by " + saved.getDonorName(),
                "DONATION"
        ));

        return saved;
    }

    public List<FoodBatch> getAllDonations() {
        checkAndExpireBatches();
        return foodBatchRepository.findAll();
    }

    public FoodBatch getDonationById(String id) {
        checkAndExpireBatches();
        return foodBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food batch not found with identifier: " + id));
    }

    public List<FoodBatch> getDonationsByDonor(String donorId) {
        checkAndExpireBatches();
        return foodBatchRepository.findByDonorId(donorId);
    }

    public List<FoodBatch> getAvailableDonations(FoodCategory category, Boolean vegetarian, String search) {
        checkAndExpireBatches();
        List<FoodBatch> available = foodBatchRepository.findAvailableBatches();

        return available.stream()
                .filter(b -> category == null || b.getCategory() == category)
                .filter(b -> vegetarian == null || b.isVegetarian() == vegetarian)
                .filter(b -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String s = search.toLowerCase();
                    return b.getFoodName().toLowerCase().contains(s) ||
                           b.getPickupAddress().toLowerCase().contains(s) ||
                           b.getDonorName().toLowerCase().contains(s);
                })
                .collect(Collectors.toList());
    }

    public FoodBatch cancelDonation(String id, User user) {
        FoodBatch batch = getDonationById(id);

        if (!batch.getDonorId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You do not have permission to cancel this donation");
        }

        if (batch.getStatus() != BatchStatus.AVAILABLE) {
            throw new ValidationException("Cannot cancel donation in status: " + batch.getStatus() + 
                    ". Only fully unallocated donations can be cancelled.");
        }

        batch.setStatus(BatchStatus.CANCELLED);
        return foodBatchRepository.save(batch);
    }

    /**
     * Food Safety Lifecycle Check:
     * Scans and marks any expired batches as EXPIRED and logs them into food waste.
     */
    public synchronized void checkAndExpireBatches() {
        LocalDateTime now = LocalDateTime.now();
        List<FoodBatch> all = foodBatchRepository.findAll();

        for (FoodBatch batch : all) {
            if ((batch.getStatus() == BatchStatus.AVAILABLE || batch.getStatus() == BatchStatus.PARTIALLY_ALLOCATED) &&
                    batch.getConsumeBefore() != null && batch.getConsumeBefore().isBefore(now)) {
                
                batch.setStatus(BatchStatus.EXPIRED);
                foodBatchRepository.save(batch);

                // Record food waste
                if (batch.getRemainingQuantity() > 0) {
                    FoodWasteRecord waste = new FoodWasteRecord(
                            UUID.randomUUID().toString(),
                            batch.getId(),
                            batch.getFoodName(),
                            batch.getRemainingQuantity(),
                            batch.getUnit(),
                            "EXPIRED: Past safe human consumption deadline (" + batch.getConsumeBefore() + ")",
                            FoodWasteCategory.COMPOSTING
                    );
                    foodWasteRepository.save(waste);
                }
            }
        }
    }

    private String findClosestRoadNode(Double lat, Double lon) {
        var nodes = dijkstraRoutingService.getGraph().getNodes();
        if (nodes.isEmpty()) {
            return "NODE-CENTRAL";
        }
        if (lat == null || lon == null) {
            return nodes.keySet().iterator().next();
        }

        return nodes.values().stream()
                .min(Comparator.comparingDouble(n -> GeoUtils.calculateHaversineDistanceKm(lat, lon, n.getLatitude(), n.getLongitude())))
                .map(RoadNode::getId)
                .orElse("NODE-CENTRAL");
    }
}

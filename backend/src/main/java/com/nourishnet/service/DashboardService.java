package com.nourishnet.service;

import com.nourishnet.dto.DashboardSummaryDto;
import com.nourishnet.model.*;
import com.nourishnet.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final FoodBatchRepository foodBatchRepository;
    private final AllocationRepository allocationRepository;
    private final DeliveryRepository deliveryRepository;
    private final VolunteerDriverRepository volunteerDriverRepository;
    private final FoodWasteRepository foodWasteRepository;

    public DashboardService(UserRepository userRepository,
                            FoodBatchRepository foodBatchRepository,
                            AllocationRepository allocationRepository,
                            DeliveryRepository deliveryRepository,
                            VolunteerDriverRepository volunteerDriverRepository,
                            FoodWasteRepository foodWasteRepository) {
        this.userRepository = userRepository;
        this.foodBatchRepository = foodBatchRepository;
        this.allocationRepository = allocationRepository;
        this.deliveryRepository = deliveryRepository;
        this.volunteerDriverRepository = volunteerDriverRepository;
        this.foodWasteRepository = foodWasteRepository;
    }

    public DashboardSummaryDto getSummary() {
        DashboardSummaryDto dto = new DashboardSummaryDto();

        dto.setTotalDonors(userRepository.findByRole(UserRole.DONOR).size());
        dto.setTotalRecipients(userRepository.findByRole(UserRole.RECIPIENT).size());
        dto.setRegisteredVolunteers(volunteerDriverRepository.count());

        List<FoodBatch> batches = foodBatchRepository.findAll();
        dto.setAvailableFoodBatches(foodBatchRepository.findAvailableBatches().size());
        dto.setExpiredBatches(batches.stream().filter(b -> b.getStatus() == BatchStatus.EXPIRED).count());

        List<Allocation> allocations = allocationRepository.findAll();
        dto.setAllocatedDonations(allocations.size());

        List<Delivery> deliveries = deliveryRepository.findAll();
        dto.setCompletedDeliveries(deliveries.stream().filter(d -> d.getStatus() == DeliveryStatus.DELIVERED).count());

        dto.setWasteRecordsCount(foodWasteRepository.count());

        // Sum quantities (approx in kg)
        double totalDonated = batches.stream().mapToDouble(FoodBatch::getOriginalQuantity).sum();
        double totalDelivered = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .mapToDouble(Delivery::getQuantity)
                .sum();
        double totalWaste = foodWasteRepository.getTotalWasteQuantity();

        dto.setTotalFoodDonatedKg(Math.round(totalDonated * 10.0) / 10.0);
        dto.setTotalFoodRedistributedKg(Math.round(totalDelivered * 10.0) / 10.0);
        dto.setTotalFoodWasteKg(Math.round(totalWaste * 10.0) / 10.0);

        // Generate activity timeline from real objects
        List<Map<String, Object>> activities = new ArrayList<>();

        for (Delivery d : deliveries) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "DELIVERY");
            item.put("title", "Delivery Status: " + d.getStatus());
            item.put("description", d.getFoodName() + " (" + d.getQuantity() + " " + d.getUnit() + ") from " + d.getDonorName() + " to " + d.getRecipientName());
            item.put("time", d.getDeliveredAt() != null ? d.getDeliveredAt() : d.getCreatedAt());
            activities.add(item);
        }

        for (FoodBatch b : batches) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "DONATION");
            item.put("title", "Donation: " + b.getFoodName());
            item.put("description", b.getOriginalQuantity() + " " + b.getUnit() + " listed by " + b.getDonorName() + " [" + b.getStatus() + "]");
            item.put("time", b.getCreatedAt());
            activities.add(item);
        }

        activities.sort((a, b) -> {
            LocalDateTime tA = (LocalDateTime) a.get("time");
            LocalDateTime tB = (LocalDateTime) b.get("time");
            if (tA == null && tB == null) return 0;
            if (tA == null) return 1;
            if (tB == null) return -1;
            return tB.compareTo(tA);
        });

        if (activities.size() > 10) {
            activities = activities.subList(0, 10);
        }

        dto.setRecentActivities(activities);
        return dto;
    }
}

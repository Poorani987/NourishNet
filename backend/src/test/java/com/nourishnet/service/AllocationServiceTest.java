package com.nourishnet.service;

import com.nourishnet.dto.AllocationRequest;
import com.nourishnet.exception.AllocationException;
import com.nourishnet.model.*;
import com.nourishnet.repository.*;
import com.nourishnet.routing.DijkstraRoutingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AllocationServiceTest {

    private FoodBatchRepository foodBatchRepository;
    private AllocationRepository allocationRepository;
    private DeliveryRepository deliveryRepository;
    private NotificationRepository notificationRepository;
    private DijkstraRoutingService routingService;
    private AllocationService allocationService;

    private User recipientUser;
    private FoodBatch foodBatch;

    @BeforeEach
    void setUp() {
        foodBatchRepository = new InMemoryFoodBatchRepository();
        allocationRepository = new InMemoryAllocationRepository();
        deliveryRepository = new InMemoryDeliveryRepository();
        notificationRepository = new InMemoryNotificationRepository();
        routingService = new DijkstraRoutingService();

        allocationService = new AllocationService(
                foodBatchRepository,
                allocationRepository,
                deliveryRepository,
                notificationRepository,
                routingService
        );

        recipientUser = new User(
                "RECIP-1", "Shelter Manager", "shelter@test.org", "pass",
                UserRole.RECIPIENT, "9876543210", "City Care Shelter",
                "10 Northern Way", 13.08, 80.27
        );

        foodBatch = new FoodBatch(
                "BATCH-1", "DONOR-1", "Fresh Cafe", "Veg Curry & Rice",
                FoodCategory.COOKED_MEALS, 50.0, "kg", true, true,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(5),
                "None", "Warm", "1 Central St", 13.07, 80.26, "NODE-CENTRAL", "Gate A"
        );
        foodBatchRepository.save(foodBatch);
    }

    @Test
    @DisplayName("Claiming partial quantity updates batch remaining quantity and sets status PARTIALLY_ALLOCATED")
    void testPartialAllocation() {
        AllocationRequest req = new AllocationRequest();
        req.setBatchId("BATCH-1");
        req.setQuantity(20.0);
        req.setDeliveryAddress("10 Northern Way");
        req.setRecipientLatitude(13.08);
        req.setRecipientLongitude(80.27);

        Allocation alloc = allocationService.claimDonation(req, recipientUser);

        assertNotNull(alloc);
        assertEquals(20.0, alloc.getQuantity());
        assertEquals("BATCH-1", alloc.getBatchId());

        FoodBatch updatedBatch = foodBatchRepository.findById("BATCH-1").orElseThrow();
        assertEquals(30.0, updatedBatch.getRemainingQuantity());
        assertEquals(BatchStatus.PARTIALLY_ALLOCATED, updatedBatch.getStatus());

        // Delivery was generated with a 6-digit OTP
        var deliveries = deliveryRepository.findAll();
        assertEquals(1, deliveries.size());
        Delivery d = deliveries.get(0);
        assertNotNull(d.getHandoverOtp());
        assertEquals(6, d.getHandoverOtp().length());
    }

    @Test
    @DisplayName("Claiming full remaining quantity sets status to FULLY_ALLOCATED")
    void testFullAllocation() {
        AllocationRequest req = new AllocationRequest();
        req.setBatchId("BATCH-1");
        req.setQuantity(50.0);
        req.setDeliveryAddress("10 Northern Way");

        allocationService.claimDonation(req, recipientUser);

        FoodBatch updatedBatch = foodBatchRepository.findById("BATCH-1").orElseThrow();
        assertEquals(0.0, updatedBatch.getRemainingQuantity());
        assertEquals(BatchStatus.FULLY_ALLOCATED, updatedBatch.getStatus());
    }

    @Test
    @DisplayName("Claiming more than available quantity throws AllocationException")
    void testExceededQuantityThrows() {
        AllocationRequest req = new AllocationRequest();
        req.setBatchId("BATCH-1");
        req.setQuantity(55.0);
        req.setDeliveryAddress("10 Northern Way");

        assertThrows(AllocationException.class, () -> allocationService.claimDonation(req, recipientUser));
    }

    @Test
    @DisplayName("Claiming expired batch throws AllocationException and marks batch EXPIRED")
    void testExpiredBatchThrows() {
        foodBatch.setConsumeBefore(LocalDateTime.now().minusMinutes(5));
        foodBatchRepository.save(foodBatch);

        AllocationRequest req = new AllocationRequest();
        req.setBatchId("BATCH-1");
        req.setQuantity(10.0);
        req.setDeliveryAddress("10 Northern Way");

        assertThrows(AllocationException.class, () -> allocationService.claimDonation(req, recipientUser));

        FoodBatch updatedBatch = foodBatchRepository.findById("BATCH-1").orElseThrow();
        assertEquals(BatchStatus.EXPIRED, updatedBatch.getStatus());
    }
}

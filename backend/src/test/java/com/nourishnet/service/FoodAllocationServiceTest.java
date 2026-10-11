package com.nourishnet.service;

import com.nourishnet.dto.AllocationRequest;
import com.nourishnet.dto.DonationCreateRequest;
import com.nourishnet.exception.AllocationException;
import com.nourishnet.exception.ValidationException;
import com.nourishnet.model.*;
import com.nourishnet.repository.*;
import com.nourishnet.routing.DijkstraRoutingService;
import com.nourishnet.routing.WeightedGraph;
import com.nourishnet.util.GeoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Food Donation and Allocation Service Business Logic Tests")
class FoodAllocationServiceTest {

    private FoodBatchRepository foodBatchRepository;
    private AllocationRepository allocationRepository;
    private DeliveryRepository deliveryRepository;
    private NotificationRepository notificationRepository;
    private FoodWasteRepository foodWasteRepository;
    private DijkstraRoutingService routingService;
    private DonationService donationService;
    private AllocationService allocationService;

    private User donorUser;
    private User recipientUser;

    @BeforeEach
    void setUp() {
        foodBatchRepository = new InMemoryFoodBatchRepository();
        allocationRepository = new InMemoryAllocationRepository();
        deliveryRepository = new InMemoryDeliveryRepository();
        notificationRepository = new InMemoryNotificationRepository();
        foodWasteRepository = new InMemoryFoodWasteRepository();

        WeightedGraph graph = new WeightedGraph();
        graph.addNode(new RoadNode("NODE-CENTRAL", "Central Hub", 13.0827, 80.2707, "Hub"));
        graph.addNode(new RoadNode("NODE-NORTH", "North Shelter", 13.1120, 80.2680, "North"));
        graph.addEdge(new RoadEdge("NODE-CENTRAL", "NODE-NORTH", 3.5, true));
        routingService = new DijkstraRoutingService(graph);

        donationService = new DonationService(foodBatchRepository, foodWasteRepository, notificationRepository, routingService);
        allocationService = new AllocationService(foodBatchRepository, allocationRepository, deliveryRepository, notificationRepository, routingService);

        donorUser = new User("DONOR-1", "Chef Rao", "chef@bistro.com", "hash", UserRole.DONOR, "123", "Bistro", "Address", 13.08, 80.27);
        recipientUser = new User("RECIP-1", "Care Home", "care@home.org", "hash", UserRole.RECIPIENT, "456", "Hope Shelter", "Address", 13.11, 80.26);
    }

    @Test
    @DisplayName("Valid donation should be successfully created with AVAILABLE status")
    void testCreateValidDonation() {
        DonationCreateRequest req = new DonationCreateRequest();
        req.setFoodName("Vegetable Curry & Rice");
        req.setCategory(FoodCategory.COOKED_MEALS);
        req.setQuantity(50.0);
        req.setUnit("kg");
        req.setVegetarian(true);
        req.setCookedFood(true);
        req.setConsumeBefore(LocalDateTime.now().plusHours(12));
        req.setPickupAddress("Bistro Kitchen Door");
        req.setNearestNodeId("NODE-CENTRAL");

        FoodBatch batch = donationService.createDonation(req, donorUser);

        assertNotNull(batch);
        assertNotNull(batch.getId());
        assertEquals("Vegetable Curry & Rice", batch.getFoodName());
        assertEquals(50.0, batch.getOriginalQuantity());
        assertEquals(50.0, batch.getRemainingQuantity());
        assertEquals(BatchStatus.AVAILABLE, batch.getStatus());
    }

    @Test
    @DisplayName("Donation with non-positive quantity or past deadline must be rejected")
    void testInvalidDonationRejection() {
        DonationCreateRequest zeroQty = new DonationCreateRequest();
        zeroQty.setFoodName("Bread");
        zeroQty.setCategory(FoodCategory.BAKERY_ITEMS);
        zeroQty.setQuantity(0.0);
        zeroQty.setUnit("kg");
        zeroQty.setConsumeBefore(LocalDateTime.now().plusHours(5));
        zeroQty.setPickupAddress("Bakery");

        assertThrows(ValidationException.class, () -> donationService.createDonation(zeroQty, donorUser));

        DonationCreateRequest pastDeadline = new DonationCreateRequest();
        pastDeadline.setFoodName("Bread");
        pastDeadline.setCategory(FoodCategory.BAKERY_ITEMS);
        pastDeadline.setQuantity(10.0);
        pastDeadline.setUnit("kg");
        pastDeadline.setConsumeBefore(LocalDateTime.now().minusHours(2));
        pastDeadline.setPickupAddress("Bakery");

        assertThrows(ValidationException.class, () -> donationService.createDonation(pastDeadline, donorUser));
    }

    @Test
    @DisplayName("Partial allocation should correctly update remaining quantity and status")
    void testPartialAllocation() {
        FoodBatch batch = new FoodBatch(
                "BATCH-100", donorUser.getId(), donorUser.getName(), "Fresh Rice Bags",
                FoodCategory.GRAINS_AND_STAPLES, 100.0, "kg", true, false,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(2),
                "None", "Dry storage", "Donor Depot", 13.08, 80.27, "NODE-CENTRAL", "None"
        );
        foodBatchRepository.save(batch);

        // Recipient claims 40 kg out of 100 kg
        AllocationRequest req = new AllocationRequest("BATCH-100", 40.0, "Shelter Address", 13.11, 80.26, "NODE-NORTH");
        Allocation allocation = allocationService.claimDonation(req, recipientUser);

        assertNotNull(allocation);
        assertEquals(40.0, allocation.getQuantity());

        // Check updated batch status and remaining quantity
        FoodBatch updatedBatch = foodBatchRepository.findById("BATCH-100").orElseThrow();
        assertEquals(60.0, updatedBatch.getRemainingQuantity());
        assertEquals(BatchStatus.PARTIALLY_ALLOCATED, updatedBatch.getStatus());

        // Now claim the remaining 60 kg
        AllocationRequest req2 = new AllocationRequest("BATCH-100", 60.0, "Shelter Address", 13.11, 80.26, "NODE-NORTH");
        Allocation allocation2 = allocationService.claimDonation(req2, recipientUser);

        assertNotNull(allocation2);
        FoodBatch fullyAllocatedBatch = foodBatchRepository.findById("BATCH-100").orElseThrow();
        assertEquals(0.0, fullyAllocatedBatch.getRemainingQuantity());
        assertEquals(BatchStatus.FULLY_ALLOCATED, fullyAllocatedBatch.getStatus());
    }

    @Test
    @DisplayName("Claiming more than remaining quantity must be rejected")
    void testExcessiveAllocationRejection() {
        FoodBatch batch = new FoodBatch(
                "BATCH-101", donorUser.getId(), donorUser.getName(), "Soup",
                FoodCategory.COOKED_MEALS, 20.0, "kg", true, true,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(4),
                "None", "Warm", "Donor Depot", 13.08, 80.27, "NODE-CENTRAL", "None"
        );
        foodBatchRepository.save(batch);

        AllocationRequest req = new AllocationRequest("BATCH-101", 25.0, "Shelter", 13.11, 80.26, "NODE-NORTH");
        assertThrows(AllocationException.class, () -> allocationService.claimDonation(req, recipientUser));
    }

    @Test
    @DisplayName("Claiming expired food donation must be rejected")
    void testExpiredFoodAllocationRejection() {
        FoodBatch expiredBatch = new FoodBatch(
                "BATCH-EXPIRED", donorUser.getId(), donorUser.getName(), "Past Food",
                FoodCategory.COOKED_MEALS, 20.0, "kg", true, true,
                LocalDateTime.now().minusHours(10), LocalDateTime.now().minusHours(1),
                "None", "Cool", "Donor Depot", 13.08, 80.27, "NODE-CENTRAL", "None"
        );
        foodBatchRepository.save(expiredBatch);

        AllocationRequest req = new AllocationRequest("BATCH-EXPIRED", 10.0, "Shelter", 13.11, 80.26, "NODE-NORTH");
        assertThrows(AllocationException.class, () -> allocationService.claimDonation(req, recipientUser));

        FoodBatch refreshed = foodBatchRepository.findById("BATCH-EXPIRED").orElseThrow();
        assertEquals(BatchStatus.EXPIRED, refreshed.getStatus());
    }

    @Test
    @DisplayName("Transport policy business rule: <=15km local volunteer, >15km recipient responsible")
    void testTransportPolicyRule() {
        // Distance 10 km
        String localPolicy = GeoUtils.evaluateTransportPolicy(10.0);
        assertTrue(localPolicy.contains("<= 15 km"));
        assertTrue(localPolicy.contains("donor or suitable volunteer"));

        // Distance 25 km
        String extendedPolicy = GeoUtils.evaluateTransportPolicy(25.0);
        assertTrue(extendedPolicy.contains("> 15 km"));
        assertTrue(extendedPolicy.contains("Recipient is responsible"));
    }
}

package com.nourishnet.service;

import com.nourishnet.dto.OtpVerifyRequest;
import com.nourishnet.dto.RoutingResultDto;
import com.nourishnet.exception.DeliveryException;
import com.nourishnet.model.*;
import com.nourishnet.repository.*;
import com.nourishnet.routing.DijkstraRoutingService;
import com.nourishnet.routing.WeightedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Delivery Coordination, Dijkstra Route Assignment, and OTP Verification Tests")
class DeliveryServiceTest {

    private DeliveryRepository deliveryRepository;
    private VolunteerDriverRepository volunteerDriverRepository;
    private FoodBatchRepository foodBatchRepository;
    private AllocationRepository allocationRepository;
    private NotificationRepository notificationRepository;
    private DijkstraRoutingService routingService;
    private DeliveryService deliveryService;

    private User volunteerUser;
    private VolunteerDriver driver;
    private Delivery pendingDelivery;

    @BeforeEach
    void setUp() {
        deliveryRepository = new InMemoryDeliveryRepository();
        volunteerDriverRepository = new InMemoryVolunteerDriverRepository();
        foodBatchRepository = new InMemoryFoodBatchRepository();
        allocationRepository = new InMemoryAllocationRepository();
        notificationRepository = new InMemoryNotificationRepository();

        WeightedGraph graph = new WeightedGraph();
        graph.addNode(new RoadNode("NODE-HUB", "Hub", 13.0, 80.0, "Hub"));
        graph.addNode(new RoadNode("NODE-MID", "Mid Junction", 13.1, 80.1, "Mid"));
        graph.addNode(new RoadNode("NODE-DROP", "Dropoff Shelter", 13.2, 80.2, "Drop"));
        graph.addEdge(new RoadEdge("NODE-HUB", "NODE-MID", 3.0, true));
        graph.addEdge(new RoadEdge("NODE-MID", "NODE-DROP", 2.0, true));
        routingService = new DijkstraRoutingService(graph);

        deliveryService = new DeliveryService(
                deliveryRepository, volunteerDriverRepository, foodBatchRepository,
                allocationRepository, notificationRepository, routingService
        );

        volunteerUser = new User("VOL-USER-1", "John Driver", "john@driver.org", "hash", UserRole.VOLUNTEER, "111", "Crew", "Depot", 13.0, 80.0);
        driver = new VolunteerDriver(
                "VOL-1", volunteerUser.getId(), volunteerUser.getName(), volunteerUser.getPhone(),
                new Vehicle(VehicleType.CARGO_VAN, "TN-01-V-1234", 100.0, "Van"),
                true, "Central Depot", 13.0, 80.0, 15.0, "All"
        );
        volunteerDriverRepository.save(driver);

        FoodBatch batch = new FoodBatch(
                "BATCH-500", "DONOR-1", "Donor Org", "Meals", FoodCategory.COOKED_MEALS,
                30.0, "kg", true, true, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(8),
                "None", "Warm", "Donor Pickup", 13.0, 80.0, "NODE-HUB", "None"
        );
        foodBatchRepository.save(batch);

        pendingDelivery = new Delivery(
                "DELIV-500", "ALLOC-500", batch.getId(), batch.getFoodName(), 30.0, "kg",
                "DONOR-1", "Donor Org", "Pickup Address",
                "RECIP-1", "Recipient Org", "Dropoff Address",
                "NODE-HUB", "NODE-DROP", "123456"
        );
        deliveryRepository.save(pendingDelivery);
    }

    @Test
    @DisplayName("Eligible volunteer driver can accept delivery and route is automatically calculated")
    void testAcceptDeliveryAndRouteCalculation() {
        Delivery accepted = deliveryService.acceptDelivery(pendingDelivery.getId(), volunteerUser);

        assertNotNull(accepted);
        assertEquals(DeliveryStatus.ASSIGNED, accepted.getStatus());
        assertEquals(driver.getId(), accepted.getVolunteerId());
        assertEquals(5.0, accepted.getRouteDistanceKm());
        assertEquals(List.of("NODE-HUB", "NODE-MID", "NODE-DROP"), accepted.getRouteNodeIds());

        // Volunteer driver is now marked with active delivery
        VolunteerDriver refreshedDriver = volunteerDriverRepository.findById(driver.getId()).orElseThrow();
        assertEquals(pendingDelivery.getId(), refreshedDriver.getActiveDeliveryId());
    }

    @Test
    @DisplayName("Driver with insufficient vehicle capacity cannot accept heavy delivery")
    void testInsufficientCapacityRejection() {
        // Driver capacity is 100 kg; create delivery of 150 kg
        Delivery heavyDelivery = new Delivery(
                "DELIV-HEAVY", "ALLOC-H", "BATCH-500", "Heavy Grains", 150.0, "kg",
                "DONOR-1", "Donor", "Pickup", "RECIP-1", "Recipient", "Drop",
                "NODE-HUB", "NODE-DROP", "999888"
        );
        deliveryRepository.save(heavyDelivery);

        assertThrows(DeliveryException.class, () ->
                deliveryService.acceptDelivery(heavyDelivery.getId(), volunteerUser)
        );
    }

    @Test
    @DisplayName("Prevent accepting delivery if driver already has an active delivery in progress")
    void testConflictingAssignmentsPrevented() {
        deliveryService.acceptDelivery(pendingDelivery.getId(), volunteerUser);

        // Create second delivery
        Delivery secondDelivery = new Delivery(
                "DELIV-501", "ALLOC-501", "BATCH-500", "Bread", 10.0, "kg",
                "DONOR-1", "Donor", "Pickup", "RECIP-1", "Recipient", "Drop",
                "NODE-HUB", "NODE-DROP", "654321"
        );
        deliveryRepository.save(secondDelivery);

        assertThrows(DeliveryException.class, () ->
                deliveryService.acceptDelivery(secondDelivery.getId(), volunteerUser)
        );
    }

    @Test
    @DisplayName("Delivery completion requires exact Handover OTP verification")
    void testDeliveryOtpVerificationLifecycle() {
        deliveryService.acceptDelivery(pendingDelivery.getId(), volunteerUser);

        // Mark picked up
        Delivery pickedUp = deliveryService.updateStatus(pendingDelivery.getId(), DeliveryStatus.PICKED_UP, volunteerUser);
        assertEquals(DeliveryStatus.PICKED_UP, pickedUp.getStatus());

        // Attempting wrong OTP fails and increments attempt counter
        OtpVerifyRequest wrongOtp = new OtpVerifyRequest("000000");
        assertThrows(DeliveryException.class, () ->
                deliveryService.verifyHandoverOtp(pendingDelivery.getId(), wrongOtp, volunteerUser)
        );

        Delivery withFailedAttempt = deliveryRepository.findById(pendingDelivery.getId()).orElseThrow();
        assertEquals(1, withFailedAttempt.getOtpAttempts());

        // Correct OTP succeeds
        OtpVerifyRequest correctOtp = new OtpVerifyRequest("123456");
        Delivery completed = deliveryService.verifyHandoverOtp(pendingDelivery.getId(), correctOtp, volunteerUser);

        assertEquals(DeliveryStatus.DELIVERED, completed.getStatus());
        assertNotNull(completed.getDeliveredAt());

        // Volunteer driver is now freed
        VolunteerDriver freeDriver = volunteerDriverRepository.findById(driver.getId()).orElseThrow();
        assertNull(freeDriver.getActiveDeliveryId());
        assertEquals(1, freeDriver.getCompletedDeliveriesCount());
    }
}

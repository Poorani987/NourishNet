package com.nourishnet.service;

import com.nourishnet.model.*;
import com.nourishnet.repository.*;
import com.nourishnet.routing.DijkstraRoutingService;
import com.nourishnet.routing.WeightedGraph;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Startup data initializer providing realistic demo accounts, sample road network graph,
 * initial food batches, volunteer drivers, demands, allocations and deliveries.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final FoodBatchRepository foodBatchRepository;
    private final AllocationRepository allocationRepository;
    private final DeliveryRepository deliveryRepository;
    private final VolunteerDriverRepository volunteerDriverRepository;
    private final FoodWasteRepository foodWasteRepository;
    private final DemandRepository demandRepository;
    private final NotificationRepository notificationRepository;
    private final DijkstraRoutingService dijkstraRoutingService;
    private final AuthService authService;

    public DataInitializer(UserRepository userRepository,
                           FoodBatchRepository foodBatchRepository,
                           AllocationRepository allocationRepository,
                           DeliveryRepository deliveryRepository,
                           VolunteerDriverRepository volunteerDriverRepository,
                           FoodWasteRepository foodWasteRepository,
                           DemandRepository demandRepository,
                           NotificationRepository notificationRepository,
                           DijkstraRoutingService dijkstraRoutingService,
                           AuthService authService) {
        this.userRepository = userRepository;
        this.foodBatchRepository = foodBatchRepository;
        this.allocationRepository = allocationRepository;
        this.deliveryRepository = deliveryRepository;
        this.volunteerDriverRepository = volunteerDriverRepository;
        this.foodWasteRepository = foodWasteRepository;
        this.demandRepository = demandRepository;
        this.notificationRepository = notificationRepository;
        this.dijkstraRoutingService = dijkstraRoutingService;
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        // Prevent duplicate initialization
        if (userRepository.count() > 0) {
            return;
        }

        BCryptPasswordEncoder encoder = authService.getPasswordEncoder();

        // 1. Initialize Sample Road Network Graph for Dijkstra Routing
        initializeRoadGraph();

        // 2. Seed Admin User
        User admin = new User(
                "USER-ADMIN-1",
                "System Administrator",
                "admin@nourishnet.org",
                encoder.encode("admin123"),
                UserRole.ADMIN,
                "+91 98765 00001",
                "NourishNet Central Operations",
                "100 Civic Centre Boulevard",
                13.0827, 80.2707
        );
        userRepository.save(admin);

        // 3. Seed 5 Donors
        User donor1 = new User(
                "USER-DONOR-1", "Chef Vikram", "greenbites@restaurant.com",
                encoder.encode("donor123"), UserRole.DONOR, "+91 98765 11001",
                "Green Bites Bistro & Cafe", "42 Green Valley High Street",
                13.0910, 80.2640
        );
        User donor2 = new User(
                "USER-DONOR-2", "Chef Marcus", "grandhotel@catering.com",
                encoder.encode("donor123"), UserRole.DONOR, "+91 98765 11002",
                "The Grand Horizon Hotel", "15 Luxury Promenade",
                13.0827, 80.2707
        );
        User donor3 = new User(
                "USER-DONOR-3", "Prof. Sunita Rao", "campusdining@univ.edu",
                encoder.encode("donor123"), UserRole.DONOR, "+91 98765 11003",
                "Metro University Campus Dining", "University East Enclave",
                13.0860, 80.2910
        );
        User donor4 = new User(
                "USER-DONOR-4", "Pierre Laurent", "artisanbakery@fresh.org",
                encoder.encode("donor123"), UserRole.DONOR, "+91 98765 11004",
                "Daily Bread Artisan Bakery", "8 Baker's Square, West Market",
                13.0780, 80.2520
        );
        User donor5 = new User(
                "USER-DONOR-5", "Ramesh Kumar", "harvestmarket@produce.com",
                encoder.encode("donor123"), UserRole.DONOR, "+91 98765 11005",
                "Harvest Fresh Supermarket", "77 Crossroads Commercial Hub",
                13.0750, 80.2750
        );

        userRepository.save(donor1);
        userRepository.save(donor2);
        userRepository.save(donor3);
        userRepository.save(donor4);
        userRepository.save(donor5);

        // 4. Seed 4 Recipients (Shelters, NGOs, Ashrams)
        User recip1 = new User(
                "USER-RECIP-1", "Sister Teresa", "hope.shelter@ngo.org",
                encoder.encode("recip123"), UserRole.RECIPIENT, "+91 98765 22001",
                "Hope Haven Shelter for Homeless", "24 Northern Ring Road",
                13.1120, 80.2680
        );
        User recip2 = new User(
                "USER-RECIP-2", "Swami Dayanand", "shanti.ashram@charity.org",
                encoder.encode("recip123"), UserRole.RECIPIENT, "+91 98765 22002",
                "Shanti Senior Citizen Ashram", "108 Serenity Lane, South District",
                13.0510, 80.2590
        );
        User recip3 = new User(
                "USER-RECIP-3", "Ananya Deshmukh", "sunrise.kids@foundation.org",
                encoder.encode("recip123"), UserRole.RECIPIENT, "+91 98765 22003",
                "Sunrise Children's Foundation", "12 Sunshine Garden, West Area",
                13.0650, 80.2450
        );
        User recip4 = new User(
                "USER-RECIP-4", "David Chen", "communitypantry@care.org",
                encoder.encode("recip123"), UserRole.RECIPIENT, "+91 98765 22004",
                "Metro Community Food Pantry", "50 Civic Center Avenue",
                13.0700, 80.2650
        );

        userRepository.save(recip1);
        userRepository.save(recip2);
        userRepository.save(recip3);
        userRepository.save(recip4);

        // 5. Seed 3 Volunteer Drivers with different vehicles and capacities
        User volUser1 = new User(
                "USER-VOL-1", "Alex Rivera", "alex.driver@volunteer.org",
                encoder.encode("volun123"), UserRole.VOLUNTEER, "+91 98765 33001",
                "Independent Volunteer", "Central Transport Yard",
                13.0827, 80.2707
        );
        User volUser2 = new User(
                "USER-VOL-2", "Priya Sharma", "priya.driver@volunteer.org",
                encoder.encode("volun123"), UserRole.VOLUNTEER, "+91 98765 33002",
                "Green Wheels Volunteer Crew", "West Market Depot",
                13.0780, 80.2520
        );
        User volUser3 = new User(
                "USER-VOL-3", "Kiran Patel", "kiran.driver@volunteer.org",
                encoder.encode("volun123"), UserRole.VOLUNTEER, "+91 98765 33003",
                "Community Swift Couriers", "University East Junction",
                13.0860, 80.2910
        );

        userRepository.save(volUser1);
        userRepository.save(volUser2);
        userRepository.save(volUser3);

        VolunteerDriver driver1 = new VolunteerDriver(
                "VOL-DRIVER-1", volUser1.getId(), volUser1.getName(), volUser1.getPhone(),
                new Vehicle(VehicleType.CARGO_VAN, "TN-01-AB-1024", 150.0, "Insulated cargo van with temperature control crates"),
                true, "Central District", 13.0827, 80.2707, 20.0, "All Districts"
        );
        VolunteerDriver driver2 = new VolunteerDriver(
                "VOL-DRIVER-2", volUser2.getId(), volUser2.getName(), volUser2.getPhone(),
                new Vehicle(VehicleType.ELECTRIC_AUTO, "TN-02-EV-4412", 75.0, "Zero-emission electric delivery cargo auto"),
                true, "West & Central Areas", 13.0780, 80.2520, 12.0, "West, Central, South"
        );
        VolunteerDriver driver3 = new VolunteerDriver(
                "VOL-DRIVER-3", volUser3.getId(), volUser3.getName(), volUser3.getPhone(),
                new Vehicle(VehicleType.MOTORCYCLE, "TN-05-MC-8833", 25.0, "Motorcycle with dual food grade insulated saddle containers"),
                true, "East & North Areas", 13.0860, 80.2910, 10.0, "North, East, Central"
        );

        volunteerDriverRepository.save(driver1);
        volunteerDriverRepository.save(driver2);
        volunteerDriverRepository.save(driver3);

        // 6. Seed 5 Food Batches with various categories, quantities and deadlines
        LocalDateTime now = LocalDateTime.now();

        // Batch 1: Cooked Meals (Freshly prepared dinner trays)
        FoodBatch batch1 = new FoodBatch(
                "BATCH-001", donor1.getId(), donor1.getOrganization(),
                "Nutritious Mixed Vegetable Biryani & Dal", FoodCategory.COOKED_MEALS,
                45.0, "kg", true, true,
                now.minusHours(2), now.plusHours(8),
                "Contains nuts (cashews). Gluten-free.",
                "Keep covered and warm until pickup.",
                donor1.getAddress(), donor1.getLatitude(), donor1.getLongitude(),
                "NODE-RESTAURANT-ROW", "Use side kitchen service entrance."
        );

        // Batch 2: Fresh Bakery Loaves & Buns
        FoodBatch batch2 = new FoodBatch(
                "BATCH-002", donor4.getId(), donor4.getOrganization(),
                "Whole Wheat Sandwich Bread & Brioche Buns", FoodCategory.BAKERY_ITEMS,
                30.0, "kg", true, false,
                now.minusHours(4), now.plusHours(36),
                "Contains gluten, wheat, milk.",
                "Store in dry, ambient room temperature.",
                donor4.getAddress(), donor4.getLatitude(), donor4.getLongitude(),
                "NODE-MARKET-WEST", "Packed in crates ready at bakery counter."
        );

        // Batch 3: Campus Dining Packed Meals
        FoodBatch batch3 = new FoodBatch(
                "BATCH-003", donor3.getId(), donor3.getOrganization(),
                "Steamed Rice, Paneer Curry & Fresh Salad", FoodCategory.COOKED_MEALS,
                60.0, "kg", true, true,
                now.minusHours(1), now.plusHours(6),
                "Contains dairy (paneer).",
                "Stored in insulated stainless steel vessels.",
                donor3.getAddress(), donor3.getLatitude(), donor3.getLongitude(),
                "NODE-CAMPUS-EAST", "Loading dock behind Student Activity Hall."
        );

        // Batch 4: Fresh Produce & Seasonal Apples/Bananas
        FoodBatch batch4 = new FoodBatch(
                "BATCH-004", donor5.getId(), donor5.getOrganization(),
                "Fresh Fruit Crates (Bananas, Apples, Oranges)", FoodCategory.FRESH_PRODUCE,
                80.0, "kg", true, false,
                now.minusHours(5), now.plusDays(4),
                "None (Natural raw fruit).",
                "Keep well-ventilated.",
                donor5.getAddress(), donor5.getLatitude(), donor5.getLongitude(),
                "NODE-JUNCTION-A", "Warehouse Bay 3 loading ramp."
        );

        // Batch 5: Fully Allocated / Completed Delivery Example
        FoodBatch batch5 = new FoodBatch(
                "BATCH-005", donor2.getId(), donor2.getOrganization(),
                "Continental Breakfast Trays & Fruit Pastries", FoodCategory.COOKED_MEALS,
                25.0, "kg", true, true,
                now.minusHours(18), now.minusHours(4), // Note: finished delivery earlier today
                "Contains dairy and gluten.",
                "Refrigerated until pickup.",
                donor2.getAddress(), donor2.getLatitude(), donor2.getLongitude(),
                "NODE-CENTRAL", "Hotel banquet service ramp."
        );
        batch5.setRemainingQuantity(0.0);
        batch5.setStatus(BatchStatus.FULLY_ALLOCATED);

        foodBatchRepository.save(batch1);
        foodBatchRepository.save(batch2);
        foodBatchRepository.save(batch3);
        foodBatchRepository.save(batch4);
        foodBatchRepository.save(batch5);

        // 7. Seed Sample Allocation & Completed Delivery for Batch 5
        Allocation allocation1 = new Allocation(
                "ALLOC-001", batch5.getId(), batch5.getFoodName(),
                donor2.getId(), donor2.getOrganization(),
                recip1.getId(), recip1.getOrganization(),
                25.0, "kg", recip1.getAddress(),
                recip1.getLatitude(), recip1.getLongitude(),
                "NODE-SHELTER-NORTH", 4.2,
                "LOCAL_TRANSPORT_ELIGIBLE (<= 15 km): Transport may be arranged by donor or suitable volunteer driver."
        );
        allocation1.setStatus(AllocationStatus.DELIVERED);
        allocationRepository.save(allocation1);

        Delivery delivery1 = new Delivery(
                "DELIV-001", allocation1.getId(), batch5.getId(), batch5.getFoodName(),
                25.0, "kg", donor2.getId(), donor2.getOrganization(), donor2.getAddress(),
                recip1.getId(), recip1.getOrganization(), recip1.getAddress(),
                "NODE-CENTRAL", "NODE-SHELTER-NORTH", "482910"
        );
        delivery1.setStatus(DeliveryStatus.DELIVERED);
        delivery1.setVolunteerId(driver1.getId());
        delivery1.setVolunteerName(driver1.getName());
        delivery1.setAssignedAt(now.minusHours(5));
        delivery1.setPickedUpAt(now.minusHours(4).minusMinutes(30));
        delivery1.setDeliveredAt(now.minusHours(4));
        delivery1.setRouteNodeIds(java.util.List.of("NODE-CENTRAL", "NODE-RESTAURANT-ROW", "NODE-SHELTER-NORTH"));
        delivery1.setRouteDistanceKm(5.4);
        deliveryRepository.save(delivery1);

        driver1.setCompletedDeliveriesCount(1);
        volunteerDriverRepository.save(driver1);

        // 8. Seed Sample Recipient Demands
        DemandRequest demand1 = new DemandRequest(
                "DEMAND-001", recip1.getId(), recip1.getOrganization(),
                FoodCategory.COOKED_MEALS, 50.0, "servings",
                recip1.getAddress(), DemandUrgency.HIGH,
                "Need hot evening meals for 50 shelter residents."
        );
        DemandRequest demand2 = new DemandRequest(
                "DEMAND-002", recip2.getId(), recip2.getOrganization(),
                FoodCategory.FRESH_PRODUCE, 30.0, "kg",
                recip2.getAddress(), DemandUrgency.MEDIUM,
                "Weekly seasonal fruits and fresh vegetables for elderly residents."
        );
        DemandRequest demand3 = new DemandRequest(
                "DEMAND-003", recip3.getId(), recip3.getOrganization(),
                FoodCategory.BAKERY_ITEMS, 40.0, "packets",
                recip3.getAddress(), DemandUrgency.CRITICAL,
                "Urgent breakfast bread or biscuits for children's morning meal."
        );

        demandRepository.save(demand1);
        demandRepository.save(demand2);
        demandRepository.save(demand3);

        // 9. Seed Sample Food Waste Records (demonstrating safe recovery like composting & biogas)
        FoodWasteRecord waste1 = new FoodWasteRecord(
                "WASTE-001", "HIST-BATCH-EXPIRED", "Overripe Papayas & Tomatoes",
                18.5, "kg", "EXPIRED: Softened past safe transport tolerance",
                FoodWasteCategory.COMPOSTING
        );
        FoodWasteRecord waste2 = new FoodWasteRecord(
                "WASTE-002", "HIST-BATCH-SPOILED", "Cooked Lentils (Ambient Expiry)",
                12.0, "kg", "SPOILED: Left uncovered beyond safe temperature window",
                FoodWasteCategory.BIOGAS_PROCESSING
        );
        foodWasteRepository.save(waste1);
        foodWasteRepository.save(waste2);

        // 10. Seed Initial Notifications
        notificationRepository.save(new Notification(
                UUID.randomUUID().toString(),
                "ALL",
                "Welcome to NourishNet",
                "Smart Food Redistribution & Waste Management System initialized in demo mode.",
                "SYSTEM"
        ));
    }

    private void initializeRoadGraph() {
        WeightedGraph graph = dijkstraRoutingService.getGraph();
        graph.clear();

        // 10 Road Network Junction Nodes
        graph.addNode(new RoadNode("NODE-CENTRAL", "Central Food Bank Hub", 13.0827, 80.2707, "Major municipal distribution intersection"));
        graph.addNode(new RoadNode("NODE-MARKET-WEST", "West Market & Produce Center", 13.0780, 80.2520, "Wholesale market ring road"));
        graph.addNode(new RoadNode("NODE-RESTAURANT-ROW", "Green Valley Dining Strip", 13.0910, 80.2640, "Restaurant corridor & commercial lane"));
        graph.addNode(new RoadNode("NODE-SHELTER-NORTH", "North Community Care Shelter", 13.1120, 80.2680, "Northern relief and shelter center"));
        graph.addNode(new RoadNode("NODE-CAMPUS-EAST", "East University Campus Hub", 13.0860, 80.2910, "University avenue & educational district"));
        graph.addNode(new RoadNode("NODE-ASHRAM-SOUTH", "South Shanti Ashram & Care Home", 13.0510, 80.2590, "Southern welfare residence corridor"));
        graph.addNode(new RoadNode("NODE-JUNCTION-A", "Crossroads Highway Junction A", 13.0750, 80.2750, "Central-South expressway interchange"));
        graph.addNode(new RoadNode("NODE-JUNCTION-B", "Riverside Ring Road Junction B", 13.0980, 80.2820, "North-East bypass crossing"));
        graph.addNode(new RoadNode("NODE-CHILDRENS-HOME", "Sunrise Children Foundation", 13.0650, 80.2450, "South-West residential area junction"));
        graph.addNode(new RoadNode("NODE-CIVIC-CENTER", "Civic Plaza & Distribution Point", 13.0700, 80.2650, "Administrative square and depot"));

        // Non-negative weighted road edges (distances in kilometers)
        graph.addEdge(new RoadEdge("NODE-CENTRAL", "NODE-RESTAURANT-ROW", 2.4, true));
        graph.addEdge(new RoadEdge("NODE-CENTRAL", "NODE-MARKET-WEST", 3.1, true));
        graph.addEdge(new RoadEdge("NODE-CENTRAL", "NODE-JUNCTION-A", 1.8, true));
        graph.addEdge(new RoadEdge("NODE-CENTRAL", "NODE-CAMPUS-EAST", 3.5, true));
        graph.addEdge(new RoadEdge("NODE-RESTAURANT-ROW", "NODE-JUNCTION-B", 2.2, true));
        graph.addEdge(new RoadEdge("NODE-RESTAURANT-ROW", "NODE-SHELTER-NORTH", 3.0, true));
        graph.addEdge(new RoadEdge("NODE-SHELTER-NORTH", "NODE-JUNCTION-B", 2.6, true));
        graph.addEdge(new RoadEdge("NODE-CAMPUS-EAST", "NODE-JUNCTION-B", 2.9, true));
        graph.addEdge(new RoadEdge("NODE-MARKET-WEST", "NODE-CIVIC-CENTER", 2.1, true));
        graph.addEdge(new RoadEdge("NODE-MARKET-WEST", "NODE-CHILDRENS-HOME", 2.5, true));
        graph.addEdge(new RoadEdge("NODE-CIVIC-CENTER", "NODE-JUNCTION-A", 1.5, true));
        graph.addEdge(new RoadEdge("NODE-CIVIC-CENTER", "NODE-ASHRAM-SOUTH", 2.8, true));
        graph.addEdge(new RoadEdge("NODE-JUNCTION-A", "NODE-ASHRAM-SOUTH", 3.2, true));
        graph.addEdge(new RoadEdge("NODE-CHILDRENS-HOME", "NODE-ASHRAM-SOUTH", 2.3, true));
    }
}

# 🌱 NourishNet — Smart Food Redistribution and Waste Management System

> **A Complete, Functional, Professional Full-Stack Web Application**  
> Connecting food donors (restaurants, hotels, universities) with recipient shelters and volunteer drivers using Dijkstra shortest-path road routing, Haversine transport policy evaluation, and Handover OTP verification.

---

## 📌 Project Overview

**NourishNet** is designed to reduce urban food waste by intelligently matching surplus perishable meals and groceries from commercial food establishments with verified charities, ashrams, and community pantries before food safety deadlines expire.

Key operating principles:
1. **Voluntary Transport Coordination:** Volunteer drivers register their own vehicles (bike, electric auto, van) and capacity; the system matches suitable volunteers rather than treating them as hired employees.
2. **Algorithmic Road Routing:** Route distances are computed using **Dijkstra's shortest-path algorithm** over a weighted road network graph, not merely straight-line geographic distances.
3. **Distance-Based Transport Policy:** If donor-recipient Haversine distance is $\le 15\text{ km}$, local volunteers or donors arrange transport; if $> 15\text{ km}$, recipients coordinate dedicated transport.
4. **Food Safety & Handover Verification:** Strict expiry checks prevent allocation of spoiled food; completed deliveries require the driver to verify the recipient's authenticated 6-digit Handover OTP.
5. **Circular Waste Recovery:** Expired or damaged batches are logged and systematically diverted to composting and biogas processing.

---

## 🛠️ Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Frontend** | HTML5, CSS3, Vanilla JavaScript | Responsive nature-inspired pastel green, soft mint, and cream aesthetic; Google Fonts (Outfit & Inter); Fetch API |
| **Backend** | Java (21 / 25), Spring Boot 3.3.4 | Spring Web (REST Controllers), Spring Validation, Spring Security Crypto (BCrypt), Jackson |
| **Persistence (Phase 1)** | In-Memory Concurrent Collections | Thread-safe repositories (`ConcurrentHashMap`, atomic state operations) behind clean repository interfaces |
| **Future Database** | Supabase PostgreSQL | Fully prepared schema in `schema-supabase.sql` ready to swap for in-memory repositories |
| **Algorithm** | Dijkstra Shortest-Path | `PriorityQueue<NodeDistance>` Min-Heap on `WeightedGraph` ($O((V+E)\log V)$) |
| **Build & Test** | Maven 3.9+, JUnit 5 | 25 automated unit and integration tests |

---

## 📂 Project Structure

```
NourishNet/
├── frontend/                               # Complete frontend website
│   ├── index.html                          # Public landing page (Hero, features, live stats)
│   ├── login.html                          # Dedicated sign-in page with 1-click demo accounts
│   ├── register.html                       # Account registration with role selection
│   ├── donor-dashboard.html                # Food donor management portal
│   ├── recipient-dashboard.html            # Shelter/NGO claimed food & OTP portal
│   ├── volunteer-dashboard.html            # Volunteer driver hub & delivery execution
│   ├── admin-dashboard.html                # System metrics, users, waste records
│   ├── donations.html                      # Public food discovery marketplace
│   ├── deliveries.html                     # Live delivery coordination & tracking
│   ├── route.html                          # Interactive Dijkstra route calculator & visualizer
│   ├── css/
│   │   ├── style.css                       # Nature-inspired design system & colors
│   │   ├── dashboard.css                   # KPI cards, data tables, modals, OTP cards
│   │   └── responsive.css                  # Mobile & tablet adaptive breakpoints
│   └── js/
│       ├── api.js                          # Central REST API client communicating with backend
│       ├── auth.js                         # Session manager, demo accounts quick switch
│       ├── donations.js                    # Marketplace filters and partial claim modal
│       ├── deliveries.js                   # Delivery state machine & transport policies
│       ├── route.js                        # Dijkstra algorithm runner & 2D canvas graph
│       ├── donor.js                        # Donor batch registration helpers
│       ├── recipient.js                    # Recipient claim helpers
│       ├── volunteer.js                    # Volunteer acceptance and OTP verification
│       └── admin.js                        # Administrative analytics
│
├── backend/                                # Spring Boot Java backend
│   ├── pom.xml                             # Maven build and dependency configuration
│   ├── mvnw.cmd                            # Windows Maven wrapper
│   └── src/
│       ├── main/
│       │   ├── java/com/nourishnet/
│       │   │   ├── NourishNetApplication.java  # Main application entry point
│       │   │   ├── config/WebConfig.java       # CORS & static frontend file mapping
│       │   │   ├── controller/                 # REST API endpoints (/api/**)
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── DonationController.java
│       │   │   │   ├── AllocationController.java
│       │   │   │   ├── DeliveryController.java
│       │   │   │   ├── VolunteerController.java
│       │   │   │   ├── RoutingController.java
│       │   │   │   ├── DemandController.java
│       │   │   │   ├── FoodWasteController.java
│       │   │   │   └── DashboardController.java
│       │   │   ├── service/                    # Business logic & thread synchronization
│       │   │   ├── model/                      # Domain entities (FoodBatch, Delivery, etc.)
│       │   │   ├── dto/                        # Data transfer objects & validation schemas
│       │   │   ├── repository/                 # Repository interfaces & in-memory stores
│       │   │   ├── exception/                  # Custom exceptions & GlobalExceptionHandler
│       │   │   ├── routing/                    # DijkstraRoutingService & WeightedGraph
│       │   │   └── util/GeoUtils.java          # Haversine formula & transport policy rules
│       │   └── resources/
│       │       ├── application.properties      # Server port & static resource locations
│       │       └── schema-supabase.sql         # Production PostgreSQL schema for Supabase
│       └── test/java/com/nourishnet/           # Automated JUnit 5 unit & integration tests
│           ├── NourishNetApplicationTests.java
│           ├── routing/DijkstraRoutingTest.java
│           └── service/
│               ├── FoodAllocationServiceTest.java
│               └── DeliveryServiceTest.java
│
├── README.md                               # Project documentation & run guide
└── .gitignore                              # Git exclusion rules
```

---

## 🚀 How to Run the Application

### Prerequisites
1. **Java Development Kit (JDK):** Version 17, 21, or 25 installed. Check with:
   ```powershell
   java -version
   ```
2. **Maven:** Installed or using the included wrapper/path `apache-maven-3.9.9`. Check with:
   ```powershell
   mvn -version
   ```

### Step 1: Start the Spring Boot Backend & Frontend
The backend is configured to **automatically serve the frontend website files directly** on `http://localhost:8080/`. You only need to run a single command:

```powershell
cd "c:\Users\Poorani R\OneDrive\Documents\NourishNet\backend"
mvn spring-boot:run
```
*(Or use `.\mvnw.cmd spring-boot:run`)*

### Step 2: Open in Web Browser
Navigate to:
👉 **`http://localhost:8080/`** (or `http://localhost:8080/index.html`)

The frontend communicates with the backend REST APIs at `http://localhost:8080/api`.

> **Alternative Frontend Server (Optional):**  
> If you prefer using VS Code Live Server or Python HTTP Server (`python -m http.server 5500`), CORS is pre-configured on the backend to allow requests from any local origin seamlessly.

---

## 🔑 Predefined Demo Accounts

The application automatically seeds realistic demo data on startup. You can test each role using the **1-Click Demo Buttons** at the top of every page, or manually sign in:

| Role | Organization / Name | Email | Password | Details |
|---|---|---|---|---|
| **ADMIN** | NourishNet Central Operations | `admin@nourishnet.org` | `admin123` | Full administrative visibility |
| **DONOR** | Green Bites Bistro & Cafe | `greenbites@restaurant.com` | `donor123` | Restaurant donor (Cooked Meals) |
| **DONOR** | The Grand Horizon Hotel | `grandhotel@catering.com` | `donor123` | Banquet catering donor |
| **DONOR** | Metro University Campus Dining | `campusdining@univ.edu` | `donor123` | Large scale dining donor |
| **RECIPIENT** | Hope Haven Shelter for Homeless | `hope.shelter@ngo.org` | `recip123` | Homeless shelter recipient |
| **RECIPIENT** | Shanti Senior Citizen Ashram | `shanti.ashram@charity.org` | `recip123` | Senior care home recipient |
| **VOLUNTEER** | Alex Rivera | `alex.driver@volunteer.org` | `volun123` | Cargo Van (150 kg capacity) |
| **VOLUNTEER** | Priya Sharma | `priya.driver@volunteer.org` | `volun123` | Electric Auto (75 kg capacity) |
| **VOLUNTEER** | Kiran Patel | `kiran.driver@volunteer.org` | `volun123` | Motorcycle (25 kg capacity) |

---

## 🗺️ Dijkstra Shortest-Path Road Network Routing

Dijkstra's shortest-path algorithm is implemented in [`DijkstraRoutingService.java`](file:///c:/Users/Poorani%20R/OneDrive/Documents/NourishNet/backend/src/main/java/com/nourishnet/routing/DijkstraRoutingService.java) using a weighted adjacency-list graph and a Java `PriorityQueue` min-heap.

### Configured Sample Road Junctions
* `NODE-CENTRAL`: Central Food Bank Hub (Lat: 13.0827, Lon: 80.2707)
* `NODE-RESTAURANT-ROW`: Green Valley Dining Strip (Lat: 13.0910, Lon: 80.2640)
* `NODE-MARKET-WEST`: West Market & Produce Center (Lat: 13.0780, Lon: 80.2520)
* `NODE-SHELTER-NORTH`: North Community Care Shelter (Lat: 13.1120, Lon: 80.2680)
* `NODE-CAMPUS-EAST`: East University Campus Hub (Lat: 13.0860, Lon: 80.2910)
* `NODE-ASHRAM-SOUTH`: South Shanti Ashram & Care Home (Lat: 13.0510, Lon: 80.2590)
* `NODE-JUNCTION-A`: Crossroads Highway Junction A (Lat: 13.0750, Lon: 80.2750)
* `NODE-JUNCTION-B`: Riverside Ring Road Junction B (Lat: 13.0980, Lon: 80.2820)
* `NODE-CHILDRENS-HOME`: Sunrise Children Foundation (Lat: 13.0650, Lon: 80.2450)
* `NODE-CIVIC-CENTER`: Civic Plaza & Distribution Point (Lat: 13.0700, Lon: 80.2650)

### Testing Dijkstra Routing via API
Send a `GET` request:
```http
GET http://localhost:8080/api/routes/shortest-path?source=NODE-CENTRAL&destination=NODE-SHELTER-NORTH
```
**Example Response:**
```json
{
  "success": true,
  "message": "Dijkstra shortest path calculated",
  "data": {
    "source": "NODE-CENTRAL",
    "destination": "NODE-SHELTER-NORTH",
    "path": [
      "NODE-CENTRAL",
      "NODE-RESTAURANT-ROW",
      "NODE-SHELTER-NORTH"
    ],
    "totalDistanceKm": 5.4,
    "dataType": "SAMPLE_ROAD_NETWORK"
  }
}
```

---

## 🔄 Step-by-Step Workflow Test Guide

Follow this walkthrough to experience the entire lifecycle from donation to delivery:

1. **List Food (Donor):**
   * Sign in as `greenbites@restaurant.com` (or click `🍲 Donor` on the demo bar).
   * Go to `donor-dashboard.html`. Click **"+ Register Surplus Donation"**.
   * Enter: "Fresh Tomato Soup & Rolls", Category: `COOKED_MEALS`, Quantity: `30 kg`, Consume-before: `10 hours`. Click **Publish**.
2. **Claim Food (Recipient):**
   * Click `🤝 Recipient` on the demo bar.
   * Go to `donations.html`. Find "Fresh Tomato Soup & Rolls".
   * Click **"Claim Food 🤝"**. Enter a partial claim of `15 kg`. Enter your delivery address.
   * Notice that the remaining quantity updates to `15 kg` atomically.
3. **Inspect Handover OTP:**
   * Go to `recipient-dashboard.html`.
   * Under **"Inbound Deliveries & Handover Verification OTPs"**, note the 6-digit OTP code (e.g., `482910`).
4. **Accept Delivery & Inspect Dijkstra Route (Volunteer):**
   * Click `🚚 Volunteer` on the demo bar.
   * Go to `volunteer-dashboard.html`.
   * Under **"Available Delivery Requests"**, click **"Accept Delivery 🚚"**.
   * The system evaluates vehicle capacity and assigns the delivery.
   * The Dijkstra shortest road route and total kilometers appear on your dashboard! Click **"Mark Food Picked Up"**.
5. **Complete Handover Verification:**
   * Click **"Complete Delivery with Recipient OTP"**.
   * Enter the 6-digit OTP noted in Step 3.
   * The backend validates the token, frees the volunteer driver, and marks the delivery as **`DELIVERED`**!

---

## 🌐 Complete REST API Endpoint Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user and return JWT/demo token |
| `POST` | `/api/auth/register` | Register new user account with role selection |
| `GET` | `/api/auth/me` | Fetch authenticated user profile |
| `GET` | `/api/auth/demo-accounts` | List predefined demo credentials |
| `POST` | `/api/donations` | Register surplus food batch (Donor) |
| `GET` | `/api/donations` | List donations with filters (`availableOnly`, `category`, `search`) |
| `GET` | `/api/donations/{id}` | Retrieve specific food batch |
| `GET` | `/api/donations/donor/{donorId}` | List food batches published by a donor |
| `PATCH` | `/api/donations/{id}/cancel` | Cancel an unallocated donation |
| `POST` | `/api/allocations` | Claim a donation batch (supports partial allocation) |
| `GET` | `/api/allocations` | List confirmed food claims |
| `GET` | `/api/deliveries` | List all redistribution deliveries |
| `GET` | `/api/deliveries/pending` | List deliveries waiting for volunteer drivers |
| `POST` | `/api/deliveries/{id}/accept` | Volunteer driver accepts delivery |
| `PATCH` | `/api/deliveries/{id}/status` | Update delivery state (e.g. `PICKED_UP`) |
| `GET` | `/api/deliveries/{id}/route` | Retrieve calculated Dijkstra route for delivery |
| `POST` | `/api/deliveries/{id}/verify-otp` | Verify 6-digit Handover OTP to complete delivery |
| `POST` | `/api/volunteers` | Register or update volunteer vehicle |
| `GET` | `/api/volunteers/me` | Retrieve volunteer driver profile |
| `PATCH` | `/api/volunteers/availability` | Toggle driver availability (`true`/`false`) |
| `GET` | `/api/routes/shortest-path` | Run Dijkstra shortest-path between road nodes |
| `GET` | `/api/routes/nodes` | List all weighted road graph junctions |
| `GET` | `/api/routes/edges` | List all weighted road graph connections |
| `POST` | `/api/demands` | Recipient publishes food demand |
| `GET` | `/api/demands` | List active demands on public demand board |
| `POST` | `/api/waste` | Log expired/damaged batch for composting/biogas |
| `GET` | `/api/waste` | List organic waste recovery records |
| `GET` | `/api/dashboard/summary` | Aggregate platform KPIs and statistics |

---

## 🧪 Automated Testing

To run the backend test suite:
```powershell
cd "c:\Users\Poorani R\OneDrive\Documents\NourishNet\backend"
mvn test
```

### Verified Test Suite (25 Tests Passing):
* **`DijkstraRoutingTest`**: Multi-alternative graph optimality, source == destination, unreachable nodes, invalid node IDs, negative edge weight rejection.
* **`FoodAllocationServiceTest`**: Valid donation creation, negative quantity rejection, past deadline rejection, partial quantity claiming, remaining quantity updates, over-allocation prevention, expired food claim rejection, Haversine 15-km transport policy rule.
* **`DeliveryServiceTest`**: Volunteer driver capacity eligibility, conflicting active delivery prevention, Dijkstra route assignment, state transitions, wrong OTP rejection and attempt counter, successful OTP verification and volunteer release.
* **`NourishNetApplicationTests`**: Spring Boot application context boot verification.

---

## ⚠️ Current Phase Architecture & Limitations

* **Temporary In-Memory Storage:** For development and college laboratory demonstration, all data is maintained in thread-safe memory collections (`ConcurrentHashMap`). When the Spring Boot server stops or restarts, temporary data resets to the initial demo state.
* **Thread-Safe Synchronization:** Concurrency protections (synchronized sections) prevent race conditions during partial allocation claims.
* **Production Password Hashing:** Passwords are fully hashed with BCrypt.

---

## 🐘 Planned Supabase PostgreSQL Migration

NourishNet was designed from the ground up to allow connecting a Supabase PostgreSQL database without refactoring controllers or business logic.

A complete PostgreSQL schema is provided at [`schema-supabase.sql`](file:///c:/Users/Poorani%20R/OneDrive/Documents/NourishNet/backend/src/main/resources/schema-supabase.sql).

### Steps to Connect Supabase Later:
1. Create a project at [supabase.com](https://supabase.com).
2. Run the SQL script from `backend/src/main/resources/schema-supabase.sql` in Supabase's SQL Editor.
3. In `backend/pom.xml`, include the PostgreSQL JDBC driver:
   ```xml
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <scope>runtime</scope>
   </dependency>
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>
   ```
4. In `application.properties`, configure the Supabase connection string:
   ```properties
   spring.datasource.url=jdbc:postgresql://db.YOUR_PROJECT.supabase.co:5432/postgres
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_PASSWORD
   ```
5. Replace the `InMemory*Repository` implementations with Spring Data JPA repository interfaces (`extends JpaRepository<Entity, String>`). The service classes and controllers require zero changes.

---

## 🎓 Academic Viva & Presentation Notes

When presenting this project for your 2nd-year B.E. Computer Science examination:
* **Algorithm:** Highlight Dijkstra's algorithm implemented in Java using `PriorityQueue` with time complexity $O((V+E)\log V)$ on a weighted graph.
* **Distance Policy:** Explain why Haversine spherical distance is used for the transport policy boundary (15 km rule) whereas Dijkstra's algorithm is used for road network navigation.
* **Concurrency:** Explain how synchronized blocks prevent double-claiming when two recipients attempt to allocate the same remaining batch simultaneously.
* **Software Architecture:** Explain how the Separation of Concerns (Controller ➔ Service ➔ Repository) allows swapping in-memory storage for Supabase PostgreSQL without rewriting business logic.

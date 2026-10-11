-- =========================================================================
-- NourishNet — Smart Food Redistribution and Waste Management System
-- Proposed Production Database Schema for Supabase PostgreSQL Integration
-- =========================================================================
-- Note: This schema is prepared for the upcoming Supabase phase.
-- In the current phase, NourishNet operates with thread-safe in-memory Java
-- repositories adhering to clean interface abstractions.
-- =========================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. USERS & ROLES TABLE
CREATE TABLE users (
    id VARCHAR(64) PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL CHECK (role IN ('DONOR', 'RECIPIENT', 'VOLUNTEER', 'ADMIN')),
    phone VARCHAR(32),
    organization VARCHAR(160),
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_email ON users(email);

-- 2. ROAD NETWORK NODES TABLE (For Dijkstra Graph Persistence)
CREATE TABLE road_nodes (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    description TEXT
);

-- 3. ROAD NETWORK EDGES TABLE (Weighted graph connections)
CREATE TABLE road_edges (
    id BIGSERIAL PRIMARY KEY,
    from_node_id VARCHAR(64) NOT NULL REFERENCES road_nodes(id) ON DELETE CASCADE,
    to_node_id VARCHAR(64) NOT NULL REFERENCES road_nodes(id) ON DELETE CASCADE,
    distance_km DOUBLE PRECISION NOT NULL CHECK (distance_km >= 0),
    bidirectional BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_road_edges_from ON road_edges(from_node_id);
CREATE INDEX idx_road_edges_to ON road_edges(to_node_id);

-- 4. FOOD BATCHES TABLE (Donations)
CREATE TABLE food_batches (
    id VARCHAR(64) PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    donor_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    donor_name VARCHAR(160) NOT NULL,
    food_name VARCHAR(160) NOT NULL,
    category VARCHAR(64) NOT NULL CHECK (category IN (
        'COOKED_MEALS', 'PACKAGED_GOODS', 'FRESH_PRODUCE',
        'BAKERY_ITEMS', 'DAIRY_PRODUCTS', 'BEVERAGES',
        'GRAINS_AND_STAPLES', 'OTHER'
    )),
    original_quantity DOUBLE PRECISION NOT NULL CHECK (original_quantity > 0),
    remaining_quantity DOUBLE PRECISION NOT NULL CHECK (remaining_quantity >= 0),
    unit VARCHAR(32) NOT NULL,
    vegetarian BOOLEAN NOT NULL DEFAULT TRUE,
    cooked_food BOOLEAN NOT NULL DEFAULT TRUE,
    preparation_time TIMESTAMP WITH TIME ZONE,
    consume_before TIMESTAMP WITH TIME ZONE NOT NULL,
    allergen_info TEXT,
    storage_requirements TEXT,
    pickup_address TEXT NOT NULL,
    pickup_latitude DOUBLE PRECISION,
    pickup_longitude DOUBLE PRECISION,
    nearest_node_id VARCHAR(64) REFERENCES road_nodes(id),
    additional_instructions TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN (
        'AVAILABLE', 'PARTIALLY_ALLOCATED', 'FULLY_ALLOCATED', 'EXPIRED', 'CANCELLED'
    )),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_food_batches_status ON food_batches(status);
CREATE INDEX idx_food_batches_donor ON food_batches(donor_id);
CREATE INDEX idx_food_batches_deadline ON food_batches(consume_before);

-- 5. VOLUNTEER DRIVERS & VEHICLES TABLE
CREATE TABLE volunteer_drivers (
    id VARCHAR(64) PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    user_id VARCHAR(64) NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(32),
    vehicle_type VARCHAR(64) NOT NULL CHECK (vehicle_type IN (
        'BICYCLE', 'MOTORCYCLE', 'ELECTRIC_AUTO', 'CAR_SEDAN',
        'CAR_SUV', 'CARGO_VAN', 'MINI_TRUCK'
    )),
    vehicle_registration VARCHAR(64) NOT NULL,
    vehicle_capacity_kg DOUBLE PRECISION NOT NULL CHECK (vehicle_capacity_kg > 0),
    vehicle_notes TEXT,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    current_operating_location VARCHAR(160) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    operating_radius_km DOUBLE PRECISION NOT NULL DEFAULT 10.0,
    preferred_areas TEXT,
    completed_deliveries_count INTEGER NOT NULL DEFAULT 0,
    active_delivery_id VARCHAR(64),
    registered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_volunteers_available ON volunteer_drivers(available);

-- 6. ALLOCATIONS TABLE (Multiple partial claims per food batch)
CREATE TABLE allocations (
    id VARCHAR(64) PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    batch_id VARCHAR(64) NOT NULL REFERENCES food_batches(id) ON DELETE RESTRICT,
    food_name VARCHAR(160) NOT NULL,
    donor_id VARCHAR(64) NOT NULL REFERENCES users(id),
    donor_name VARCHAR(160) NOT NULL,
    recipient_id VARCHAR(64) NOT NULL REFERENCES users(id),
    recipient_name VARCHAR(160) NOT NULL,
    quantity DOUBLE PRECISION NOT NULL CHECK (quantity > 0),
    unit VARCHAR(32) NOT NULL,
    delivery_address TEXT NOT NULL,
    recipient_latitude DOUBLE PRECISION,
    recipient_longitude DOUBLE PRECISION,
    destination_node_id VARCHAR(64) REFERENCES road_nodes(id),
    haversine_distance_km DOUBLE PRECISION,
    transport_policy TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN (
        'CONFIRMED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'
    )),
    allocated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_allocations_batch ON allocations(batch_id);
CREATE INDEX idx_allocations_recipient ON allocations(recipient_id);

-- 7. DELIVERIES TABLE (With Dijkstra calculated route and OTP verification)
CREATE TABLE deliveries (
    id VARCHAR(64) PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    allocation_id VARCHAR(64) NOT NULL REFERENCES allocations(id) ON DELETE CASCADE,
    batch_id VARCHAR(64) NOT NULL REFERENCES food_batches(id),
    food_name VARCHAR(160) NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(32) NOT NULL,
    donor_id VARCHAR(64) NOT NULL REFERENCES users(id),
    donor_name VARCHAR(160) NOT NULL,
    pickup_address TEXT NOT NULL,
    recipient_id VARCHAR(64) NOT NULL REFERENCES users(id),
    recipient_name VARCHAR(160) NOT NULL,
    dropoff_address TEXT NOT NULL,
    volunteer_id VARCHAR(64) REFERENCES volunteer_drivers(id),
    volunteer_name VARCHAR(120),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_ACCEPTANCE' CHECK (status IN (
        'PENDING_ACCEPTANCE', 'ASSIGNED', 'PICKED_UP', 'DELIVERED', 'CANCELLED'
    )),
    source_node_id VARCHAR(64) REFERENCES road_nodes(id),
    destination_node_id VARCHAR(64) REFERENCES road_nodes(id),
    route_node_ids TEXT[], -- Ordered Dijkstra shortest-path sequence
    route_distance_km DOUBLE PRECISION,
    handover_otp VARCHAR(16) NOT NULL,
    otp_attempts INTEGER NOT NULL DEFAULT 0,
    max_otp_attempts INTEGER NOT NULL DEFAULT 5,
    otp_expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    assigned_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_volunteer ON deliveries(volunteer_id);

-- 8. DEMAND REQUESTS TABLE (Recipient demand board)
CREATE TABLE demand_requests (
    id VARCHAR(64) PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    recipient_id VARCHAR(64) NOT NULL REFERENCES users(id),
    recipient_name VARCHAR(160) NOT NULL,
    category VARCHAR(64) NOT NULL,
    required_quantity DOUBLE PRECISION NOT NULL CHECK (required_quantity > 0),
    unit VARCHAR(32) NOT NULL,
    location VARCHAR(160) NOT NULL,
    urgency VARCHAR(32) NOT NULL DEFAULT 'MEDIUM' CHECK (urgency IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')),
    notes TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'FULFILLED', 'CANCELLED')),
    requested_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. FOOD WASTE RECORDS TABLE
CREATE TABLE food_waste_records (
    id VARCHAR(64) PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    batch_id VARCHAR(64),
    food_name VARCHAR(160) NOT NULL,
    quantity DOUBLE PRECISION NOT NULL CHECK (quantity > 0),
    unit VARCHAR(32) NOT NULL,
    reason TEXT NOT NULL,
    disposal_category VARCHAR(64) NOT NULL CHECK (disposal_category IN (
        'COMPOSTING', 'BIOGAS_PROCESSING', 'ORGANIC_RECYCLING', 'NON_HUMAN_RECOVERY', 'SAFE_DISPOSAL'
    )),
    status VARCHAR(32) NOT NULL DEFAULT 'RECORDED',
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. NOTIFICATIONS TABLE
CREATE TABLE notifications (
    id VARCHAR(64) PRIMARY KEY DEFAULT uuid_generate_v4()::TEXT,
    recipient_user_id VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(32) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

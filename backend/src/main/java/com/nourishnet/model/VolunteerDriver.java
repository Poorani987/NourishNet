package com.nourishnet.model;

import java.time.LocalDateTime;

public class VolunteerDriver {
    private String id;
    private String userId;
    private String name;
    private String phone;
    private Vehicle vehicle;
    private boolean available;
    private String currentOperatingLocation;
    private Double latitude;
    private Double longitude;
    private double operatingRadiusKm;
    private String preferredAreas;
    private int completedDeliveriesCount;
    private String activeDeliveryId;
    private LocalDateTime registeredAt;

    public VolunteerDriver() {
        this.registeredAt = LocalDateTime.now();
        this.available = true;
        this.completedDeliveriesCount = 0;
    }

    public VolunteerDriver(String id, String userId, String name, String phone, Vehicle vehicle, 
                           boolean available, String currentOperatingLocation, Double latitude, 
                           Double longitude, double operatingRadiusKm, String preferredAreas) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.vehicle = vehicle;
        this.available = available;
        this.currentOperatingLocation = currentOperatingLocation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.operatingRadiusKm = operatingRadiusKm;
        this.preferredAreas = preferredAreas;
        this.completedDeliveriesCount = 0;
        this.activeDeliveryId = null;
        this.registeredAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getCurrentOperatingLocation() { return currentOperatingLocation; }
    public void setCurrentOperatingLocation(String currentOperatingLocation) { this.currentOperatingLocation = currentOperatingLocation; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public double getOperatingRadiusKm() { return operatingRadiusKm; }
    public void setOperatingRadiusKm(double operatingRadiusKm) { this.operatingRadiusKm = operatingRadiusKm; }

    public String getPreferredAreas() { return preferredAreas; }
    public void setPreferredAreas(String preferredAreas) { this.preferredAreas = preferredAreas; }

    public int getCompletedDeliveriesCount() { return completedDeliveriesCount; }
    public void setCompletedDeliveriesCount(int completedDeliveriesCount) { this.completedDeliveriesCount = completedDeliveriesCount; }

    public String getActiveDeliveryId() { return activeDeliveryId; }
    public void setActiveDeliveryId(String activeDeliveryId) { this.activeDeliveryId = activeDeliveryId; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}

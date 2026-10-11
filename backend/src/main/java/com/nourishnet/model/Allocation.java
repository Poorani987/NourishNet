package com.nourishnet.model;

import java.time.LocalDateTime;

public class Allocation {
    private String id;
    private String batchId;
    private String foodName;
    private String donorId;
    private String donorName;
    private String recipientId;
    private String recipientName;
    private double quantity;
    private String unit;
    private String deliveryAddress;
    private Double recipientLatitude;
    private Double recipientLongitude;
    private String destinationNodeId;
    private double haversineDistanceKm;
    private String transportPolicy;
    private AllocationStatus status;
    private LocalDateTime allocatedAt;

    public Allocation() {
        this.allocatedAt = LocalDateTime.now();
        this.status = AllocationStatus.CONFIRMED;
    }

    public Allocation(String id, String batchId, String foodName, String donorId, String donorName,
                      String recipientId, String recipientName, double quantity, String unit,
                      String deliveryAddress, Double recipientLatitude, Double recipientLongitude,
                      String destinationNodeId, double haversineDistanceKm, String transportPolicy) {
        this.id = id;
        this.batchId = batchId;
        this.foodName = foodName;
        this.donorId = donorId;
        this.donorName = donorName;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.quantity = quantity;
        this.unit = unit;
        this.deliveryAddress = deliveryAddress;
        this.recipientLatitude = recipientLatitude;
        this.recipientLongitude = recipientLongitude;
        this.destinationNodeId = destinationNodeId;
        this.haversineDistanceKm = haversineDistanceKm;
        this.transportPolicy = transportPolicy;
        this.status = AllocationStatus.CONFIRMED;
        this.allocatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public Double getRecipientLatitude() { return recipientLatitude; }
    public void setRecipientLatitude(Double recipientLatitude) { this.recipientLatitude = recipientLatitude; }

    public Double getRecipientLongitude() { return recipientLongitude; }
    public void setRecipientLongitude(Double recipientLongitude) { this.recipientLongitude = recipientLongitude; }

    public String getDestinationNodeId() { return destinationNodeId; }
    public void setDestinationNodeId(String destinationNodeId) { this.destinationNodeId = destinationNodeId; }

    public double getHaversineDistanceKm() { return haversineDistanceKm; }
    public void setHaversineDistanceKm(double haversineDistanceKm) { this.haversineDistanceKm = haversineDistanceKm; }

    public String getTransportPolicy() { return transportPolicy; }
    public void setTransportPolicy(String transportPolicy) { this.transportPolicy = transportPolicy; }

    public AllocationStatus getStatus() { return status; }
    public void setStatus(AllocationStatus status) { this.status = status; }

    public LocalDateTime getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(LocalDateTime allocatedAt) { this.allocatedAt = allocatedAt; }
}

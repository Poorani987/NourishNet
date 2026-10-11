package com.nourishnet.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Delivery {
    private String id;
    private String allocationId;
    private String batchId;
    private String foodName;
    private double quantity;
    private String unit;
    private String donorId;
    private String donorName;
    private String pickupAddress;
    private String recipientId;
    private String recipientName;
    private String dropoffAddress;
    private String volunteerId;
    private String volunteerName;
    private DeliveryStatus status;
    private String sourceNodeId;
    private String destinationNodeId;
    private List<String> routeNodeIds = new ArrayList<>();
    private double routeDistanceKm;
    private String handoverOtp;
    private int otpAttempts;
    private int maxOtpAttempts = 5;
    private LocalDateTime otpExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    public Delivery() {
        this.createdAt = LocalDateTime.now();
        this.status = DeliveryStatus.PENDING_ACCEPTANCE;
        this.otpAttempts = 0;
        this.maxOtpAttempts = 5;
    }

    public Delivery(String id, String allocationId, String batchId, String foodName, double quantity,
                    String unit, String donorId, String donorName, String pickupAddress,
                    String recipientId, String recipientName, String dropoffAddress,
                    String sourceNodeId, String destinationNodeId, String handoverOtp) {
        this.id = id;
        this.allocationId = allocationId;
        this.batchId = batchId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.unit = unit;
        this.donorId = donorId;
        this.donorName = donorName;
        this.pickupAddress = pickupAddress;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.dropoffAddress = dropoffAddress;
        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
        this.handoverOtp = handoverOtp;
        this.status = DeliveryStatus.PENDING_ACCEPTANCE;
        this.otpAttempts = 0;
        this.maxOtpAttempts = 5;
        this.otpExpiresAt = LocalDateTime.now().plusHours(24);
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAllocationId() { return allocationId; }
    public void setAllocationId(String allocationId) { this.allocationId = allocationId; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public String getVolunteerId() { return volunteerId; }
    public void setVolunteerId(String volunteerId) { this.volunteerId = volunteerId; }

    public String getVolunteerName() { return volunteerName; }
    public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public String getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }

    public String getDestinationNodeId() { return destinationNodeId; }
    public void setDestinationNodeId(String destinationNodeId) { this.destinationNodeId = destinationNodeId; }

    public List<String> getRouteNodeIds() { return routeNodeIds; }
    public void setRouteNodeIds(List<String> routeNodeIds) { this.routeNodeIds = routeNodeIds; }

    public double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }

    public String getHandoverOtp() { return handoverOtp; }
    public void setHandoverOtp(String handoverOtp) { this.handoverOtp = handoverOtp; }

    public int getOtpAttempts() { return otpAttempts; }
    public void setOtpAttempts(int otpAttempts) { this.otpAttempts = otpAttempts; }

    public int getMaxOtpAttempts() { return maxOtpAttempts; }
    public void setMaxOtpAttempts(int maxOtpAttempts) { this.maxOtpAttempts = maxOtpAttempts; }

    public LocalDateTime getOtpExpiresAt() { return otpExpiresAt; }
    public void setOtpExpiresAt(LocalDateTime otpExpiresAt) { this.otpExpiresAt = otpExpiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}

package com.nourishnet.model;

import java.time.LocalDateTime;

public class DemandRequest {
    private String id;
    private String recipientId;
    private String recipientName;
    private FoodCategory category;
    private double requiredQuantity;
    private String unit;
    private String location;
    private DemandUrgency urgency;
    private String notes;
    private DemandStatus status;
    private LocalDateTime requestedAt;

    public DemandRequest() {
        this.requestedAt = LocalDateTime.now();
        this.status = DemandStatus.OPEN;
    }

    public DemandRequest(String id, String recipientId, String recipientName, FoodCategory category,
                         double requiredQuantity, String unit, String location, DemandUrgency urgency,
                         String notes) {
        this.id = id;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.category = category;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
        this.location = location;
        this.urgency = urgency;
        this.notes = notes;
        this.status = DemandStatus.OPEN;
        this.requestedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) { this.category = category; }

    public double getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(double requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public DemandUrgency getUrgency() { return urgency; }
    public void setUrgency(DemandUrgency urgency) { this.urgency = urgency; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public DemandStatus getStatus() { return status; }
    public void setStatus(DemandStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
}

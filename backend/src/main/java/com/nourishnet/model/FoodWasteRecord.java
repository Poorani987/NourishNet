package com.nourishnet.model;

import java.time.LocalDateTime;

public class FoodWasteRecord {
    private String id;
    private String batchId;
    private String foodName;
    private double quantity;
    private String unit;
    private String reason;
    private FoodWasteCategory disposalCategory;
    private String status;
    private LocalDateTime recordedAt;

    public FoodWasteRecord() {
        this.recordedAt = LocalDateTime.now();
        this.status = "RECORDED";
    }

    public FoodWasteRecord(String id, String batchId, String foodName, double quantity, String unit,
                           String reason, FoodWasteCategory disposalCategory) {
        this.id = id;
        this.batchId = batchId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.unit = unit;
        this.reason = reason;
        this.disposalCategory = disposalCategory;
        this.status = "RECORDED";
        this.recordedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public FoodWasteCategory getDisposalCategory() { return disposalCategory; }
    public void setDisposalCategory(FoodWasteCategory disposalCategory) { this.disposalCategory = disposalCategory; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}

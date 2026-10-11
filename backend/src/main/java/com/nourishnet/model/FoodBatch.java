package com.nourishnet.model;

import java.time.LocalDateTime;

public class FoodBatch {
    private String id;
    private String donorId;
    private String donorName;
    private String foodName;
    private FoodCategory category;
    private double originalQuantity;
    private double remainingQuantity;
    private String unit;
    private boolean vegetarian;
    private boolean cookedFood;
    private LocalDateTime preparationTime;
    private LocalDateTime consumeBefore;
    private String allergenInfo;
    private String storageRequirements;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String nearestNodeId;
    private String additionalInstructions;
    private BatchStatus status;
    private LocalDateTime createdAt;

    public FoodBatch() {
        this.createdAt = LocalDateTime.now();
        this.status = BatchStatus.AVAILABLE;
    }

    public FoodBatch(String id, String donorId, String donorName, String foodName, FoodCategory category,
                     double quantity, String unit, boolean vegetarian, boolean cookedFood,
                     LocalDateTime preparationTime, LocalDateTime consumeBefore, String allergenInfo,
                     String storageRequirements, String pickupAddress, Double pickupLatitude,
                     Double pickupLongitude, String nearestNodeId, String additionalInstructions) {
        this.id = id;
        this.donorId = donorId;
        this.donorName = donorName;
        this.foodName = foodName;
        this.category = category;
        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;
        this.unit = unit;
        this.vegetarian = vegetarian;
        this.cookedFood = cookedFood;
        this.preparationTime = preparationTime;
        this.consumeBefore = consumeBefore;
        this.allergenInfo = allergenInfo;
        this.storageRequirements = storageRequirements;
        this.pickupAddress = pickupAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.nearestNodeId = nearestNodeId;
        this.additionalInstructions = additionalInstructions;
        this.status = BatchStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) { this.category = category; }

    public double getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(double originalQuantity) { this.originalQuantity = originalQuantity; }

    public double getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(double remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isVegetarian() { return vegetarian; }
    public void setVegetarian(boolean vegetarian) { this.vegetarian = vegetarian; }

    public boolean isCookedFood() { return cookedFood; }
    public void setCookedFood(boolean cookedFood) { this.cookedFood = cookedFood; }

    public LocalDateTime getPreparationTime() { return preparationTime; }
    public void setPreparationTime(LocalDateTime preparationTime) { this.preparationTime = preparationTime; }

    public LocalDateTime getConsumeBefore() { return consumeBefore; }
    public void setConsumeBefore(LocalDateTime consumeBefore) { this.consumeBefore = consumeBefore; }

    public String getAllergenInfo() { return allergenInfo; }
    public void setAllergenInfo(String allergenInfo) { this.allergenInfo = allergenInfo; }

    public String getStorageRequirements() { return storageRequirements; }
    public void setStorageRequirements(String storageRequirements) { this.storageRequirements = storageRequirements; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public Double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(Double pickupLatitude) { this.pickupLatitude = pickupLatitude; }

    public Double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(Double pickupLongitude) { this.pickupLongitude = pickupLongitude; }

    public String getNearestNodeId() { return nearestNodeId; }
    public void setNearestNodeId(String nearestNodeId) { this.nearestNodeId = nearestNodeId; }

    public String getAdditionalInstructions() { return additionalInstructions; }
    public void setAdditionalInstructions(String additionalInstructions) { this.additionalInstructions = additionalInstructions; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

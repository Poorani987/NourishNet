package com.nourishnet.dto;

import com.nourishnet.model.FoodCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class DonationCreateRequest {
    @NotBlank(message = "Food name is required")
    private String foodName;

    @NotNull(message = "Food category is required")
    private FoodCategory category;

    @Positive(message = "Quantity must be strictly positive")
    private double quantity;

    @NotBlank(message = "Quantity unit is required")
    private String unit;

    private boolean vegetarian;
    private boolean cookedFood;
    private LocalDateTime preparationTime;

    @NotNull(message = "Consume-before food safety deadline is required")
    @Future(message = "Consume-before deadline must be in the future")
    private LocalDateTime consumeBefore;

    private String allergenInfo;
    private String storageRequirements;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    private Double pickupLatitude;
    private Double pickupLongitude;
    private String nearestNodeId;
    private String additionalInstructions;

    public DonationCreateRequest() {}

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public FoodCategory getCategory() { return category; }
    public void setCategory(FoodCategory category) { this.category = category; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

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
}

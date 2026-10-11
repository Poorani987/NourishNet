package com.nourishnet.dto;

import com.nourishnet.model.FoodWasteCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FoodWasteCreateRequest {
    private String batchId;

    @NotBlank(message = "Food name is required")
    private String foodName;

    @Positive(message = "Waste quantity must be positive")
    private double quantity;

    @NotBlank(message = "Quantity unit is required")
    private String unit;

    @NotBlank(message = "Disposal or spoilage reason is required")
    private String reason;

    @NotNull(message = "Disposal category is required")
    private FoodWasteCategory disposalCategory;

    public FoodWasteCreateRequest() {}

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
}

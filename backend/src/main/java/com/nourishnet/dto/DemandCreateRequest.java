package com.nourishnet.dto;

import com.nourishnet.model.DemandUrgency;
import com.nourishnet.model.FoodCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class DemandCreateRequest {
    @NotNull(message = "Food category is required")
    private FoodCategory category;

    @Positive(message = "Required quantity must be positive")
    private double requiredQuantity;

    @NotBlank(message = "Quantity unit is required")
    private String unit;

    @NotBlank(message = "Location is required")
    private String location;

    private DemandUrgency urgency = DemandUrgency.MEDIUM;
    private String notes;

    public DemandCreateRequest() {}

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
}
